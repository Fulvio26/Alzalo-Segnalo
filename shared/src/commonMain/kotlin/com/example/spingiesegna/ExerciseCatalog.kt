package com.example.spingiesegna

/** Nome e identificativo stabile degli esercizi salvati dall'utente. */
internal data class CatalogExercise(
    val id: String,
    val familyId: String,
    val nameEn: String,
    val nameIt: String,
    val isCustom: Boolean = false
) {
    val name: String get() = nameEn
}

internal fun encodeCustomExercise(exercise: CatalogExercise): String = buildString {
    fun addField(value: String) { append(value.length).append(':').append(value) }
    append("CE1|")
    addField(exercise.id)
    addField(exercise.nameEn)
}

/** CE1 is intentionally unchanged; custom names remain in exactly the language entered by the user. */
internal fun decodeCustomExercise(record: String): CatalogExercise? = runCatching {
    require(record.startsWith("CE1|"))
    var position = 4
    fun readField(): String {
        val colon = record.indexOf(':', position)
        require(colon >= position)
        val length = record.substring(position, colon).toInt()
        position = colon + 1
        require(length >= 0 && position + length <= record.length)
        return record.substring(position, position + length).also { position += length }
    }
    val id = readField()
    val name = readField()
    require(position == record.length && id.isNotBlank() && name.isNotBlank())
    CatalogExercise(id, id, name, name, isCustom = true)
}.getOrNull()

