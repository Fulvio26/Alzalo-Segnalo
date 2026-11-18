package com.example.spingiesegna

import android.content.Context

internal var preferences: android.content.SharedPreferences? = null

actual fun initializeTemplateStorage(context: Any) {
    preferences = (context as Context).getSharedPreferences("spingi_e_segna", Context.MODE_PRIVATE)
}

actual fun loadTemplateRecords(): List<String> {
    val prefs = preferences ?: return emptyList()
    val count = prefs.getInt("template_count", 0)
    return (0 until count).mapNotNull { index -> prefs.getString("template_$index", null) }
}

actual fun replaceTemplateRecords(records: List<String>) {
    val prefs = checkNotNull(preferences) { "Archivio locale non inizializzato" }
    val editor = prefs.edit()
    repeat(prefs.getInt("template_count", 0)) { editor.remove("template_$it") }
    editor.putInt("template_count", records.size)
    records.forEachIndexed { index, record -> editor.putString("template_$index", record) }
    editor.apply()
}

actual fun loadWorkoutFolderRecords(): List<String> {
    val prefs = preferences ?: return emptyList()
    val count = prefs.getInt("workout_folder_count", 0)
    return (0 until count).mapNotNull { index -> prefs.getString("workout_folder_$index", null) }
}

actual fun replaceWorkoutFolderRecords(records: List<String>) {
    val prefs = checkNotNull(preferences) { "Archivio locale non inizializzato" }
    val editor = prefs.edit()
    repeat(prefs.getInt("workout_folder_count", 0)) { editor.remove("workout_folder_$it") }
    editor.putInt("workout_folder_count", records.size)
    records.forEachIndexed { index, record -> editor.putString("workout_folder_$index", record) }
    editor.apply()
}

actual fun loadWorkoutRecords(): List<String> {
    val prefs = preferences ?: return emptyList()
    val count = prefs.getInt("workout_count", 0)
    return (0 until count).mapNotNull { index -> prefs.getString("workout_$index", null) }
}

actual fun replaceWorkoutRecords(records: List<String>) {
    val prefs = checkNotNull(preferences) { "Archivio locale non inizializzato" }
    val editor = prefs.edit()
    repeat(prefs.getInt("workout_count", 0)) { editor.remove("workout_$it") }
    editor.putInt("workout_count", records.size)
    records.forEachIndexed { index, record -> editor.putString("workout_$index", record) }
    editor.apply()
}

actual fun loadCustomExerciseRecords(): List<String> {
    val prefs = preferences ?: return emptyList()
    val count = prefs.getInt("custom_exercise_count", 0)
    return (0 until count).mapNotNull { index -> prefs.getString("custom_exercise_$index", null) }
}

actual fun replaceCustomExerciseRecords(records: List<String>) {
    val prefs = checkNotNull(preferences) { "Archivio locale non inizializzato" }
    val editor = prefs.edit()
    repeat(prefs.getInt("custom_exercise_count", 0)) { editor.remove("custom_exercise_$it") }
    editor.putInt("custom_exercise_count", records.size)
    records.forEachIndexed { index, record -> editor.putString("custom_exercise_$index", record) }
    editor.apply()
}

actual fun loadDarkThemePreference(): Boolean? {
    val prefs = preferences ?: return null
    return if (prefs.contains("dark_theme")) prefs.getBoolean("dark_theme", true) else null
}

actual fun saveDarkThemePreference(isDark: Boolean) {
    checkNotNull(preferences) { "Archivio locale non inizializzato" }
        .edit().putBoolean("dark_theme", isDark).apply()
}

actual fun loadLastDriveBackupTime(): Long? {
    val prefs = preferences ?: return null
    return if (prefs.contains("last_drive_backup_time")) prefs.getLong("last_drive_backup_time", 0L) else null
}

actual fun saveLastDriveBackupTime(epochMillis: Long) {
    checkNotNull(preferences) { "Archivio locale non inizializzato" }
        .edit().putLong("last_drive_backup_time", epochMillis).apply()
}

actual fun formatBackupTime(epochMillis: Long): String =
    java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
        .format(java.util.Date(epochMillis))

internal fun commitFullBackupSnapshot(snapshot: AppBackupSnapshot): Boolean {
    val prefs = checkNotNull(preferences) { "Archivio locale non inizializzato" }
    val editor = prefs.edit()
    fun replaceRecords(prefix: String, countKey: String, records: List<String>) {
        repeat(prefs.getInt(countKey, 0)) { editor.remove("$prefix$it") }
        editor.putInt(countKey, records.size)
        records.forEachIndexed { index, value -> editor.putString("$prefix$index", value) }
    }
    replaceRecords("template_", "template_count", snapshot.templateRecords)
    replaceRecords("workout_folder_", "workout_folder_count", snapshot.folderRecords)
    replaceRecords("workout_", "workout_count", snapshot.workoutRecords)
    replaceRecords("custom_exercise_", "custom_exercise_count", snapshot.customExerciseRecords)
    editor.putBoolean("dark_theme", snapshot.darkTheme)
    return editor.commit()
}
