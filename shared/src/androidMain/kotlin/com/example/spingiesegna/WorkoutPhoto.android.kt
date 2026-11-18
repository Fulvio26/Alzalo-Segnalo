package com.example.spingiesegna

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.media.ExifInterface
import android.graphics.ImageDecoder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import android.system.Os
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

private const val maxPhotoDimension = 1600

@Composable
actual fun WorkoutPhotoAttachment(
    dateKey: String,
    photoFileName: String?,
    onPhotoChanged: (String) -> Unit,
    onPhotoRemoved: () -> Unit
) {
    val context = LocalContext.current.applicationContext
    val scope = rememberCoroutineScope()
    var preview by remember(photoFileName) { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var showRemoveConfirmation by remember { mutableStateOf(false) }
    var isMenuExpanded by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }
    var cameraCaptureFile by remember { mutableStateOf<File?>(null) }
    var cameraCaptureUri by remember { mutableStateOf<Uri?>(null) }
    var showPhotoViewer by remember { mutableStateOf(false) }
    var photoScale by remember(photoFileName) { mutableFloatStateOf(1f) }
    var photoOffsetX by remember(photoFileName) { mutableFloatStateOf(0f) }
    var photoOffsetY by remember(photoFileName) { mutableFloatStateOf(0f) }

    LaunchedEffect(photoFileName) {
        preview = if (photoFileName == null) null else withContext(Dispatchers.IO) {
            workoutPhotoFile(context, photoFileName)?.let { BitmapFactory.decodeFile(it.absolutePath) }
        }
    }

    fun savePhotoFromUri(uri: Uri) {
        val temporaryCameraFile = cameraCaptureFile.takeIf { uri == cameraCaptureUri }
        scope.launch {
            isLoading = true
            errorText = null
            val newPhotoName = withContext(Dispatchers.IO) { saveWorkoutPhoto(context, uri, dateKey) }
            if (newPhotoName == null) {
                errorText = "Non sono riuscito a salvare questa foto. Prova con un’altra immagine."
            } else {
                if (photoFileName != null && photoFileName != newPhotoName) {
                    removeWorkoutPhoto(context, photoFileName)
                }
                onPhotoChanged(newPhotoName)
            }
            temporaryCameraFile?.delete()
            cameraCaptureFile = null
            cameraCaptureUri = null
            isLoading = false
        }
    }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        if (uri != null) savePhotoFromUri(uri)
    }
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { captured ->
        val uri = cameraCaptureUri
        if (captured && uri != null) savePhotoFromUri(uri)
        else {
            cameraCaptureFile?.delete()
            cameraCaptureFile = null
            cameraCaptureUri = null
        }
    }

    fun openCamera() {
        runCatching {
            val directory = File(context.cacheDir, "workout_photo_capture").apply { check(exists() || mkdirs()) }
            val file = File(directory, "scatto-${dateKey}-${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            cameraCaptureFile = file
            cameraCaptureUri = uri
            camera.launch(uri)
        }.onFailure {
            cameraCaptureFile?.delete()
            cameraCaptureFile = null
            cameraCaptureUri = null
            errorText = "Non è stato possibile aprire la fotocamera."
        }
    }

    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Button(
            onClick = { isMenuExpanded = true },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            )
        ) {
            Text(when {
                isLoading -> "Sto salvando la foto…"
                photoFileName == null -> "Aggiungi foto"
                else -> "Cambia foto"
            })
        }
        if (preview != null) {
            Image(
                bitmap = preview!!.asImageBitmap(),
                contentDescription = "Foto dell’allenamento del $dateKey",
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 360.dp)
                    .clickable { showPhotoViewer = true },
                contentScale = ContentScale.Fit
            )
            Text(
                "Tocca la foto per aprirla e ingrandirla",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(onClick = { showRemoveConfirmation = true }) {
                Text("Rimuovi foto", color = MaterialTheme.colorScheme.error)
            }
        } else if (photoFileName != null && !isLoading) {
            Text("La foto non è disponibile sul dispositivo.", color = MaterialTheme.colorScheme.error)
            TextButton(onClick = { showRemoveConfirmation = true }) {
                Text("Rimuovi foto", color = MaterialTheme.colorScheme.error)
            }
        }
        if (isLoading) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CircularProgressIndicator()
                Text("Sto salvando la foto…", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        errorText?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }

    if (isMenuExpanded) {
        Dialog(onDismissRequest = { isMenuExpanded = false }) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                tonalElevation = 8.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Aggiungi una foto",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Scegli come vuoi aggiungerla",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FilledTonalButton(
                        onClick = {
                            isMenuExpanded = false
                            picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Text("Dalla galleria", modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                    FilledTonalButton(
                        onClick = {
                            isMenuExpanded = false
                            openCamera()
                        },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Text("Scatta una foto", modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                    TextButton(onClick = { isMenuExpanded = false }) {
                        Text("Annulla", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }

    if (showPhotoViewer && preview != null) {
        val transformState = rememberTransformableState { zoomChange, panChange, _ ->
            photoScale = (photoScale * zoomChange).coerceIn(1f, 5f)
            photoOffsetX += panChange.x
            photoOffsetY += panChange.y
        }
        Dialog(
            onDismissRequest = { showPhotoViewer = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth().fillMaxHeight(0.94f).padding(12.dp),
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                tonalElevation = 8.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Foto dell’allenamento", style = MaterialTheme.typography.titleMedium)
                        TextButton(onClick = { showPhotoViewer = false }) { Text("Chiudi") }
                    }
                    Box(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = preview!!.asImageBitmap(),
                            contentDescription = "Foto ingrandibile dell’allenamento del $dateKey",
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight()
                                .graphicsLayer {
                                    scaleX = photoScale
                                    scaleY = photoScale
                                    translationX = photoOffsetX
                                    translationY = photoOffsetY
                                }
                                .transformable(transformState)
                                .pointerInput(Unit) {
                                    detectTapGestures(onDoubleTap = {
                                        if (photoScale > 1f) {
                                            photoScale = 1f
                                            photoOffsetX = 0f
                                            photoOffsetY = 0f
                                        } else {
                                            photoScale = 2f
                                        }
                                    })
                                },
                            contentScale = ContentScale.Fit
                        )
                    }
                    Text(
                        "Pizzica per ingrandire · trascina per spostare · doppio tocco per ripristinare",
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    if (showRemoveConfirmation && photoFileName != null) {
        AlertDialog(
            onDismissRequest = { showRemoveConfirmation = false },
            title = { Text("Rimuovere la foto?") },
            text = { Text("La foto verrà eliminata da questo allenamento e dal telefono.") },
            confirmButton = {
                TextButton(onClick = {
                    if (removeWorkoutPhoto(context, photoFileName)) {
                        preview?.recycle()
                        preview = null
                        onPhotoRemoved()
                        errorText = null
                    } else {
                        errorText = "Non sono riuscito a rimuovere la foto."
                    }
                    showRemoveConfirmation = false
                }) { Text("Rimuovi", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showRemoveConfirmation = false }) { Text("Annulla") }
            }
        )
    }
}

actual fun removeWorkoutPhoto(context: Any, photoFileName: String): Boolean = runCatching {
    val androidContext = context as Context
    workoutPhotoFile(androidContext, photoFileName)?.let { !it.exists() || it.delete() } ?: false
}.getOrDefault(false)

internal fun workoutPhotoFile(context: Context, photoFileName: String): File? {
    if (!isSafePhotoFileName(photoFileName)) return null
    val directory = File(context.filesDir, "workout_photos")
    if (!directory.exists() && !directory.mkdirs()) return null
    val file = File(directory, photoFileName)
    return file.takeIf { it.parentFile?.canonicalPath == directory.canonicalPath }
}

private fun saveWorkoutPhoto(context: Context, uri: Uri, dateKey: String): String? = runCatching {
    val safeDate = dateKey.replace(Regex("[^0-9-]"), "")
    require(safeDate.matches(Regex("[0-9-]{8,10}")))
    val fileName = "workout-$safeDate.jpg"
    val destination = workoutPhotoFile(context, fileName) ?: error("Archivio foto non disponibile")
    val temporary = File(destination.parentFile, "$fileName.tmp")
    val bitmap = decodeScaledBitmap(context, uri) ?: error("Formato immagine non supportato")
    try {
        FileOutputStream(temporary).use { output ->
            check(bitmap.compress(Bitmap.CompressFormat.JPEG, 86, output))
            output.flush()
            output.fd.sync()
        }
        Os.rename(temporary.absolutePath, destination.absolutePath)
        fileName
    } finally {
        bitmap.recycle()
        temporary.delete()
    }
}.getOrNull()

private fun decodeScaledBitmap(context: Context, uri: Uri): Bitmap? {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        return runCatching {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                val width = info.size.width
                val height = info.size.height
                val longestSide = maxOf(width, height)
                if (longestSide > maxPhotoDimension) {
                    val ratio = maxPhotoDimension.toFloat() / longestSide
                    decoder.setTargetSize(
                        (width * ratio).toInt().coerceAtLeast(1),
                        (height * ratio).toInt().coerceAtLeast(1)
                    )
                }
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            }
        }.getOrNull()
    }

    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
    var sampleSize = 1
    while (bounds.outWidth / (sampleSize * 2) >= maxPhotoDimension &&
        bounds.outHeight / (sampleSize * 2) >= maxPhotoDimension) {
        sampleSize *= 2
    }
    val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
    val bitmap = context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) } ?: return null
    val largestSide = maxOf(bitmap.width, bitmap.height)
    val scaled = if (largestSide <= maxPhotoDimension) bitmap else {
        val ratio = maxPhotoDimension.toFloat() / largestSide
        Bitmap.createScaledBitmap(
            bitmap,
            (bitmap.width * ratio).toInt().coerceAtLeast(1),
            (bitmap.height * ratio).toInt().coerceAtLeast(1),
            true
        ).also { bitmap.recycle() }
    }
    val orientation = context.contentResolver.openInputStream(uri)?.use { input ->
        ExifInterface(input).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
    } ?: ExifInterface.ORIENTATION_NORMAL
    val matrix = Matrix()
    when (orientation) {
        ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.setScale(-1f, 1f)
        ExifInterface.ORIENTATION_ROTATE_180 -> matrix.setRotate(180f)
        ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.setScale(1f, -1f)
        ExifInterface.ORIENTATION_TRANSPOSE -> { matrix.setRotate(90f); matrix.postScale(-1f, 1f) }
        ExifInterface.ORIENTATION_ROTATE_90 -> matrix.setRotate(90f)
        ExifInterface.ORIENTATION_TRANSVERSE -> { matrix.setRotate(-90f); matrix.postScale(-1f, 1f) }
        ExifInterface.ORIENTATION_ROTATE_270 -> matrix.setRotate(-90f)
        else -> return scaled
    }
    return runCatching {
        Bitmap.createBitmap(scaled, 0, 0, scaled.width, scaled.height, matrix, true).also {
            if (it !== scaled) scaled.recycle()
        }
    }.getOrDefault(scaled)
}
