package com.example.spingiesegna

/** Crea il PDF nel livello Android e apre il menu di condivisione del telefono. */
expect fun shareWorkoutPdf(context: Any, fileName: String, title: String, lines: List<String>): Boolean
