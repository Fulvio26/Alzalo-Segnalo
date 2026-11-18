package com.example.spingiesegna

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.RevokeAccessRequest
import com.google.android.gms.common.api.Scope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.net.URL
import java.net.URLEncoder
import java.security.MessageDigest
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

private const val driveAppDataScope = "https://www.googleapis.com/auth/drive.appdata"
private const val backupFileName = "spingi-e-segna-backup.zip"
private const val backupSchemaVersion = 2
private const val maxBackupBytes = 100 * 1024 * 1024
private const val maxPhotoBytes = 20 * 1024 * 1024

@Composable
actual fun rememberDriveAuthorizationLauncher(
    onAuthorized: (String) -> Unit,
    onError: (String) -> Unit
): () -> Unit {
    val context = LocalContext.current
    val client = remember(context) { Identity.getAuthorizationClient(context) }
    val currentOnAuthorized by rememberUpdatedState(onAuthorized)
    val currentOnError by rememberUpdatedState(onError)
    val resultLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        if (result.resultCode != Activity.RESULT_OK) {
            currentOnError("Collegamento Google Drive annullato.")
        } else {
            val authorizationResult = runCatching {
                client.getAuthorizationResultFromIntent(result.data ?: Intent())
            }.getOrNull()
            val token = authorizationResult?.accessToken
            if (token.isNullOrBlank()) currentOnError("Google non ha restituito l’autorizzazione. Riprova.")
            else currentOnAuthorized(token)
        }
    }

    return remember(context, client, resultLauncher) {
        {
            val request = AuthorizationRequest.builder()
                .setRequestedScopes(listOf(Scope(driveAppDataScope)))
                .build()
            client.authorize(request)
                .addOnSuccessListener { result ->
                    if (result.hasResolution()) {
                        val pendingIntent = result.pendingIntent
                        if (pendingIntent == null) currentOnError("Google non ha avviato la richiesta di accesso. Riprova.")
                        else resultLauncher.launch(IntentSenderRequest.Builder(pendingIntent).build())
                    } else {
                        val token = result.accessToken
                        if (token.isNullOrBlank()) currentOnError("Google non ha restituito l’autorizzazione. Riprova.")
                        else currentOnAuthorized(token)
                    }
                }
                .addOnFailureListener {
                    currentOnError("Non è stato possibile collegare Google Drive. Controlla l’account e riprova.")
                }
        }
    }
}

actual suspend fun disconnectDriveAuthorization(context: Any) {
    val client = Identity.getAuthorizationClient(context as Context)
    suspendCancellableCoroutine { continuation ->
        client.revokeAccess(RevokeAccessRequest.builder().build())
            .addOnSuccessListener { if (continuation.isActive) continuation.resume(Unit) }
            .addOnFailureListener { error -> if (continuation.isActive) continuation.resumeWithException(error) }
    }
}

actual suspend fun createAppBackupArchive(context: Any, snapshot: AppBackupSnapshot): ByteArray =
    withContext(Dispatchers.IO) { createAppBackupArchiveBlocking(context as Context, snapshot) }

private fun createAppBackupArchiveBlocking(context: Context, snapshot: AppBackupSnapshot): ByteArray {
    require(snapshot.photoFileNames.distinct().size == snapshot.photoFileNames.size) { "Foto duplicate nell’archivio locale" }
    val photos = linkedMapOf<String, ByteArray>()
    var photoTotal = 0L
    snapshot.photoFileNames.forEach { name ->
        require(isSafePhotoFileName(name)) { "Nome foto non valido" }
        val file = workoutPhotoFile(context, name) ?: error("Archivio delle foto non disponibile")
        require(file.isFile && file.length() in 1..maxPhotoBytes.toLong()) { "Foto mancante o troppo grande: $name" }
        val bytes = file.readBytes()
        require(bytes.size <= maxPhotoBytes) { "Foto troppo grande: $name" }
        photoTotal += bytes.size
        require(photoTotal <= maxBackupBytes) { "Le foto superano la dimensione massima del backup" }
        photos[name] = bytes
    }

    val createdAt = System.currentTimeMillis()
    val manifest = JSONObject()
        .put("schemaVersion", backupSchemaVersion)
        .put("createdAtMillis", createdAt)
        .put("darkTheme", snapshot.darkTheme)
        .put("templates", JSONArray().apply { snapshot.templateRecords.forEach(::put) })
        .put("folders", JSONArray().apply { snapshot.folderRecords.forEach(::put) })
        .put("workouts", JSONArray().apply { snapshot.workoutRecords.forEach(::put) })
        .put("customExercises", JSONArray().apply { snapshot.customExerciseRecords.forEach(::put) })
        .put("photos", JSONArray().apply {
            photos.forEach { (name, bytes) ->
                put(JSONObject()
                    .put("fileName", name)
                    .put("entry", "photos/$name")
                    .put("sha256", sha256(bytes)))
            }
        })

    val output = ByteArrayOutputStream()
    ZipOutputStream(output).use { zip ->
        zip.putNextEntry(ZipEntry("manifest.json"))
        zip.write(manifest.toString().toByteArray(Charsets.UTF_8))
        zip.closeEntry()
        photos.forEach { (name, bytes) ->
            zip.putNextEntry(ZipEntry("photos/$name"))
            zip.write(bytes)
            zip.closeEntry()
        }
    }
    return output.toByteArray().also { require(it.size <= maxBackupBytes) { "Backup troppo grande" } }
}

actual suspend fun readAppBackupArchive(context: Any, zipBytes: ByteArray): AppBackupArchive? =
    withContext(Dispatchers.IO) { decodeAppBackupArchive(context as Context, zipBytes) }

private fun decodeAppBackupArchive(context: Context, zipBytes: ByteArray): AppBackupArchive? = runCatching {
    require(zipBytes.isNotEmpty() && zipBytes.size <= maxBackupBytes)
    val entries = linkedMapOf<String, ByteArray>()
    var totalUncompressed = 0L
    ZipInputStream(ByteArrayInputStream(zipBytes)).use { zip ->
        var entryCount = 0
        while (true) {
            val entry = zip.nextEntry ?: break
            entryCount++
            require(entryCount in 1..5_010)
            require(!entry.isDirectory && entry.name !in entries)
            require(entry.name == "manifest.json" || entry.name.startsWith("photos/"))
            val maxEntrySize = if (entry.name == "manifest.json") 20 * 1024 * 1024 else maxPhotoBytes
            val bytes = readZipEntryLimited(zip, maxEntrySize)
            totalUncompressed += bytes.size
            require(totalUncompressed <= maxBackupBytes)
            entries[entry.name] = bytes
            zip.closeEntry()
        }
    }

    val manifestBytes = entries["manifest.json"] ?: error("Manifest mancante")
    val manifest = JSONObject(manifestBytes.toString(Charsets.UTF_8))
    val version = manifest.getInt("schemaVersion")
    require(version in 1..backupSchemaVersion)
    val createdAt = manifest.getLong("createdAtMillis")
    require(createdAt > 0)
    fun strings(key: String, maxCount: Int): List<String> {
        val array = manifest.getJSONArray(key)
        require(array.length() <= maxCount)
        return List(array.length()) { index -> array.getString(index) }
    }
    val templateRecords = strings("templates", 500)
    val workoutRecords = strings("workouts", 10_000)
    val customExerciseRecords = strings("customExercises", 10_000)
    val folderRecords = if (version >= 2 && manifest.has("folders")) strings("folders", 1_000) else emptyList()
    val photoMetadata = manifest.getJSONArray("photos")
    require(photoMetadata.length() <= 10_000)
    val photos = linkedMapOf<String, ByteArray>()
    val expectedEntries = mutableSetOf("manifest.json")
    var totalPhotos = 0L
    repeat(photoMetadata.length()) { index ->
        val item = photoMetadata.getJSONObject(index)
        val fileName = item.getString("fileName")
        val entryName = item.getString("entry")
        require(isSafePhotoFileName(fileName) && entryName == "photos/$fileName")
        val bytes = entries[entryName] ?: error("Foto mancante")
        require(bytes.isNotEmpty() && bytes.size <= maxPhotoBytes)
        require(item.getString("sha256") == sha256(bytes))
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        require(bounds.outWidth in 1..10_000 && bounds.outHeight in 1..10_000)
        require(photos.put(fileName, bytes) == null)
        require(expectedEntries.add(entryName))
        totalPhotos += bytes.size
        require(totalPhotos <= maxBackupBytes)
    }
    require(entries.keys == expectedEntries)
    AppBackupArchive(
        schemaVersion = version,
        createdAtMillis = createdAt,
        templateRecords = templateRecords,
        workoutRecords = workoutRecords,
        customExerciseRecords = customExerciseRecords,
        darkTheme = manifest.getBoolean("darkTheme"),
        photos = photos,
        folderRecords = folderRecords
    )
}.getOrNull()

private fun readZipEntryLimited(zip: ZipInputStream, maxBytes: Int): ByteArray {
    val output = ByteArrayOutputStream()
    val buffer = ByteArray(8 * 1024)
    var count = 0
    while (true) {
        val read = zip.read(buffer)
        if (read < 0) break
        count += read
        require(count <= maxBytes)
        output.write(buffer, 0, read)
    }
    return output.toByteArray()
}

actual suspend fun restoreAppBackupArchive(
    context: Any,
    archive: AppBackupArchive,
    currentSnapshot: AppBackupSnapshot
): Boolean = withContext(Dispatchers.IO) {
    restoreAppBackupArchiveBlocking(context as Context, archive, currentSnapshot)
}

private fun restoreAppBackupArchiveBlocking(
    context: Context,
    archive: AppBackupArchive,
    currentSnapshot: AppBackupSnapshot
): Boolean {
    val photosDirectory = File(context.filesDir, "workout_photos")
    val stagingDirectory = File(context.filesDir, "workout_photos_restore")
    val oldDirectory = File(context.filesDir, "workout_photos_before_restore")
    var oldDirectoryMoved = false
    var newDirectoryMoved = false
    return try {
        require(archive.schemaVersion in 1..backupSchemaVersion)
        val safetyArchive = createAppBackupArchiveBlocking(context, currentSnapshot)
        val safetyDirectory = File(context.noBackupFilesDir, "restore_safety").apply {
            if (!exists()) check(mkdirs())
        }
        val safetyTemp = File(safetyDirectory, "before-restore.zip.tmp")
        val safetyFile = File(safetyDirectory, "before-restore.zip")
        FileOutputStream(safetyTemp).use { stream ->
            stream.write(safetyArchive)
            stream.flush()
            stream.fd.sync()
        }
        android.system.Os.rename(safetyTemp.absolutePath, safetyFile.absolutePath)

        stagingDirectory.deleteRecursively()
        check(stagingDirectory.mkdirs())
        archive.photos.forEach { (name, bytes) ->
            require(isSafePhotoFileName(name) && bytes.isNotEmpty() && bytes.size <= maxPhotoBytes)
            val imageFile = File(stagingDirectory, name)
            FileOutputStream(imageFile).use { stream ->
                stream.write(bytes)
                stream.flush()
                stream.fd.sync()
            }
        }
        oldDirectory.deleteRecursively()
        if (photosDirectory.exists()) {
            android.system.Os.rename(photosDirectory.absolutePath, oldDirectory.absolutePath)
            oldDirectoryMoved = true
        }
        android.system.Os.rename(stagingDirectory.absolutePath, photosDirectory.absolutePath)
        newDirectoryMoved = true

        val restoredSnapshot = AppBackupSnapshot(
            templateRecords = archive.templateRecords,
            workoutRecords = archive.workoutRecords,
            customExerciseRecords = archive.customExerciseRecords,
            darkTheme = archive.darkTheme,
            photoFileNames = archive.photos.keys.toList(),
            folderRecords = archive.folderRecords
        )
        if (!commitFullBackupSnapshot(restoredSnapshot)) {
            throw IllegalStateException("Non sono riuscito a salvare i nuovi dati")
        }
        oldDirectory.deleteRecursively()
        true
    } catch (_: Exception) {
        if (newDirectoryMoved) photosDirectory.deleteRecursively()
        if (oldDirectoryMoved && oldDirectory.exists()) {
            runCatching { android.system.Os.rename(oldDirectory.absolutePath, photosDirectory.absolutePath) }
        }
        runCatching { commitFullBackupSnapshot(currentSnapshot) }
        stagingDirectory.deleteRecursively()
        false
    }
}

actual suspend fun uploadDriveBackup(accessToken: String, zipBytes: ByteArray) = withContext(Dispatchers.IO) {
    require(zipBytes.isNotEmpty() && zipBytes.size <= maxBackupBytes)
    val existing = findDriveBackup(accessToken)
    val response = if (existing == null) {
        createDriveBackup(accessToken, zipBytes)
    } else {
        updateDriveBackup(accessToken, existing.id, zipBytes)
    }
    val uploadedId = response.optString("id").takeIf(String::isNotBlank)
        ?: existing?.id ?: throw DriveBackupException("Google Drive non ha confermato il file caricato.")
    val verified = driveRequest(
        "GET",
        "https://www.googleapis.com/drive/v3/files/${Uri.encode(uploadedId)}?fields=id,name,size,md5Checksum,modifiedTime",
        accessToken
    ).let { JSONObject(it.toString(Charsets.UTF_8)) }
    val verifiedSize = verified.optString("size").toLongOrNull()
    requireDrive(verified.optString("id") == uploadedId && verifiedSize == zipBytes.size.toLong(),
        "Il caricamento su Google Drive non è stato verificato. I dati locali non sono stati modificati.")
    val remoteMd5 = verified.optString("md5Checksum")
    if (remoteMd5.isNotBlank()) requireDrive(remoteMd5.equals(md5(zipBytes), ignoreCase = true),
        "La verifica del backup su Google Drive non è riuscita. Riprova.")
}

actual suspend fun downloadDriveBackup(accessToken: String): ByteArray = withContext(Dispatchers.IO) {
    val file = findDriveBackup(accessToken) ?: throw DriveBackupException("Non trovo ancora un backup su Google Drive.")
    val remoteSize = file.size ?: throw DriveBackupException("Google Drive non ha restituito la dimensione del backup.")
    requireDrive(remoteSize in 1..maxBackupBytes.toLong(), "Il backup su Google Drive è troppo grande o vuoto.")
    val bytes = driveRequest(
        "GET",
        "https://www.googleapis.com/drive/v3/files/${Uri.encode(file.id)}?alt=media",
        accessToken,
        maxResponseBytes = maxBackupBytes
    )
    requireDrive(bytes.size.toLong() == remoteSize, "Il download del backup è incompleto. Riprova.")
    if (!file.md5Checksum.isNullOrBlank()) requireDrive(
        file.md5Checksum.equals(md5(bytes), ignoreCase = true),
        "La verifica del download non è riuscita. I dati locali non sono stati modificati."
    )
    bytes
}

private data class DriveBackupFile(val id: String, val size: Long?, val md5Checksum: String?, val modifiedTime: String)

private fun findDriveBackup(accessToken: String): DriveBackupFile? {
    val query = URLEncoder.encode("name = '$backupFileName' and trashed = false", "UTF-8")
    val bytes = driveRequest(
        "GET",
        "https://www.googleapis.com/drive/v3/files?spaces=appDataFolder&q=$query&fields=files(id,name,size,md5Checksum,modifiedTime)&pageSize=100",
        accessToken
    )
    val files = JSONObject(bytes.toString(Charsets.UTF_8)).optJSONArray("files") ?: JSONArray()
    return (0 until files.length())
        .map { files.getJSONObject(it) }
        .filter { it.optString("name") == backupFileName && it.optString("id").isNotBlank() }
        .maxByOrNull { it.optString("modifiedTime") }
        ?.let { json ->
            DriveBackupFile(
                id = json.getString("id"),
                size = json.optString("size").toLongOrNull(),
                md5Checksum = json.optString("md5Checksum").takeIf(String::isNotBlank),
                modifiedTime = json.optString("modifiedTime")
            )
        }
}

private fun createDriveBackup(accessToken: String, zipBytes: ByteArray): JSONObject {
    val boundary = "spingiesegna-${System.currentTimeMillis()}"
    val metadata = JSONObject()
        .put("name", backupFileName)
        .put("mimeType", "application/zip")
        .put("parents", JSONArray().put("appDataFolder"))
    val body = ByteArrayOutputStream().apply {
        write("--$boundary\r\nContent-Type: application/json; charset=UTF-8\r\n\r\n".toByteArray())
        write(metadata.toString().toByteArray(Charsets.UTF_8))
        write("\r\n--$boundary\r\nContent-Type: application/zip\r\n\r\n".toByteArray())
        write(zipBytes)
        write("\r\n--$boundary--\r\n".toByteArray())
    }.toByteArray()
    val response = driveRequest(
        "POST",
        "https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart&fields=id,size,md5Checksum",
        accessToken,
        body,
        "multipart/related; boundary=$boundary"
    )
    return JSONObject(response.toString(Charsets.UTF_8))
}

private fun updateDriveBackup(accessToken: String, id: String, zipBytes: ByteArray): JSONObject {
    val response = driveRequest(
        "PATCH",
        "https://www.googleapis.com/upload/drive/v3/files/${Uri.encode(id)}?uploadType=media&fields=id,size,md5Checksum",
        accessToken,
        zipBytes,
        "application/zip"
    )
    return JSONObject(response.toString(Charsets.UTF_8))
}

private fun driveRequest(
    method: String,
    url: String,
    accessToken: String,
    body: ByteArray? = null,
    contentType: String? = null,
    maxResponseBytes: Int = 2 * 1024 * 1024
): ByteArray {
    try {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 20_000
            readTimeout = 30_000
            setRequestProperty("Authorization", "Bearer $accessToken")
            setRequestProperty("Accept", "application/json, application/zip")
            if (body != null) {
                doOutput = true
                setRequestProperty("Content-Type", contentType ?: "application/octet-stream")
                setFixedLengthStreamingMode(body.size)
            }
        }
        try {
            if (body != null) connection.outputStream.use { it.write(body) }
            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val response = stream?.use { readLimited(it, maxResponseBytes) } ?: ByteArray(0)
            if (status !in 200..299) throw driveHttpError(status, response)
            return response
        } finally {
            connection.disconnect()
        }
    } catch (error: DriveBackupException) {
        throw error
    } catch (_: UnknownHostException) {
        throw DriveBackupException("Nessuna connessione a Internet. I dati locali sono rimasti invariati.")
    } catch (_: SocketTimeoutException) {
        throw DriveBackupException("Google Drive non ha risposto in tempo. Controlla la connessione e riprova.")
    } catch (_: Exception) {
        throw DriveBackupException("Errore di rete durante il collegamento a Google Drive. I dati locali sono rimasti invariati.")
    }
}

private fun readLimited(input: java.io.InputStream, maxBytes: Int): ByteArray {
    val output = ByteArrayOutputStream()
    val buffer = ByteArray(16 * 1024)
    var total = 0
    while (true) {
        val count = input.read(buffer)
        if (count < 0) break
        total += count
        require(total <= maxBytes) { "Risposta Google Drive troppo grande" }
        output.write(buffer, 0, count)
    }
    return output.toByteArray()
}

private fun driveHttpError(status: Int, response: ByteArray): DriveBackupException {
    val responseText = response.toString(Charsets.UTF_8)
    val authorizationRequired = status == 401 ||
        (status == 403 && (responseText.contains("insufficientPermissions") || responseText.contains("authError")))
    val message = when {
        status == 401 -> "L’accesso a Google Drive è scaduto o è stato revocato. Ricollega l’account."
        status == 403 && responseText.contains("storageQuotaExceeded") -> "Lo spazio disponibile su Google Drive non è sufficiente. I dati sul telefono sono rimasti invariati."
        status == 403 -> "Google Drive ha rifiutato l’operazione. Controlla autorizzazioni e configurazione dell’app."
        status == 404 -> "Il backup non è disponibile su Google Drive."
        else -> "Google Drive ha restituito un errore ($status). I dati sul telefono sono rimasti invariati."
    }
    return DriveBackupException(message, authorizationRequired)
}

private fun requireDrive(condition: Boolean, message: String) {
    if (!condition) throw DriveBackupException(message)
}

private fun sha256(bytes: ByteArray): String = MessageDigest.getInstance("SHA-256")
    .digest(bytes).joinToString("") { byte -> "%02x".format(Locale.ROOT, byte) }

private fun md5(bytes: ByteArray): String = MessageDigest.getInstance("MD5")
    .digest(bytes).joinToString("") { byte -> "%02x".format(Locale.ROOT, byte) }
