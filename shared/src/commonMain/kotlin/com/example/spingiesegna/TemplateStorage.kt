package com.example.spingiesegna

import androidx.compose.runtime.Composable

/** Collegamento tra la schermata comune e l'archivio locale Android. */
expect fun initializeTemplateStorage(context: Any)
expect fun loadTemplateRecords(): List<String>
expect fun replaceTemplateRecords(records: List<String>)
expect fun loadWorkoutFolderRecords(): List<String>
expect fun replaceWorkoutFolderRecords(records: List<String>)
expect fun loadWorkoutRecords(): List<String>
expect fun replaceWorkoutRecords(records: List<String>)
expect fun loadCustomExerciseRecords(): List<String>
expect fun replaceCustomExerciseRecords(records: List<String>)
expect fun loadDarkThemePreference(): Boolean?
expect fun saveDarkThemePreference(isDark: Boolean)
expect fun loadLastDriveBackupTime(): Long?
expect fun saveLastDriveBackupTime(epochMillis: Long)
expect fun formatBackupTime(epochMillis: Long): String

/** Record locali da includere nel backup, più i nomi delle foto collegate agli allenamenti. */
data class AppBackupSnapshot(
    val templateRecords: List<String>,
    val workoutRecords: List<String>,
    val customExerciseRecords: List<String>,
    val darkTheme: Boolean,
    val photoFileNames: List<String>,
    val folderRecords: List<String> = emptyList()
)

/** Contenuto letto da un archivio, ancora da validare prima del ripristino. */
data class AppBackupArchive(
    val schemaVersion: Int,
    val createdAtMillis: Long,
    val templateRecords: List<String>,
    val workoutRecords: List<String>,
    val customExerciseRecords: List<String>,
    val darkTheme: Boolean,
    val photos: Map<String, ByteArray>,
    val folderRecords: List<String> = emptyList()
)

class DriveBackupException(
    message: String,
    val requiresAuthorization: Boolean = false
) : Exception(message)

@Composable
expect fun WorkoutPhotoAttachment(
    dateKey: String,
    photoFileName: String?,
    onPhotoChanged: (String) -> Unit,
    onPhotoRemoved: () -> Unit
)

expect fun removeWorkoutPhoto(context: Any, photoFileName: String): Boolean
expect suspend fun createAppBackupArchive(context: Any, snapshot: AppBackupSnapshot): ByteArray
expect suspend fun readAppBackupArchive(context: Any, zipBytes: ByteArray): AppBackupArchive?
expect suspend fun restoreAppBackupArchive(
    context: Any,
    archive: AppBackupArchive,
    currentSnapshot: AppBackupSnapshot
): Boolean

@Composable
expect fun rememberDriveAuthorizationLauncher(
    onAuthorized: (String) -> Unit,
    onError: (String) -> Unit
): () -> Unit

expect suspend fun uploadDriveBackup(accessToken: String, zipBytes: ByteArray)
expect suspend fun downloadDriveBackup(accessToken: String): ByteArray
expect suspend fun disconnectDriveAuthorization(context: Any)
