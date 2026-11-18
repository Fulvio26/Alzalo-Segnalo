package com.example.spingiesegna

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import java.io.File

actual fun shareWorkoutPdf(context: Any, fileName: String, title: String, lines: List<String>): Boolean = runCatching {
    val androidContext = context as Context
    val directory = File(androidContext.cacheDir, "workout_exports").apply { mkdirs() }
    val safeName = fileName.replace(Regex("[^A-Za-z0-9._-]"), "_")
    val outputFile = File(directory, safeName)
    val document = PdfDocument()
    try {
        val pageWidth = 595
        val pageHeight = 842
        val margin = 48f
        val normalPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.BLACK
            textSize = 12f
            typeface = Typeface.create("sans-serif", Typeface.NORMAL)
        }
        val headingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.BLACK
            textSize = 18f
            typeface = Typeface.create("sans-serif", Typeface.BOLD)
        }
        val exercisePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.BLACK
            textSize = 14f
            typeface = Typeface.create("sans-serif", Typeface.BOLD)
        }
        var pageNumber = 1
        var page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
        var y = margin + 8f

        fun startNextPage() {
            document.finishPage(page)
            pageNumber++
            page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
            y = margin + 8f
        }

        lines.forEachIndexed { index, line ->
            val paint = when {
                index == 0 -> headingPaint
                line.startsWith("Esercizio ") -> exercisePaint
                else -> normalPaint
            }
            val text = line.ifBlank { " " }
            val wrapped = mutableListOf<String>()
            var currentLine = StringBuilder()
            text.split(Regex("\\s+")).forEach { word ->
                val candidate = if (currentLine.isEmpty()) word else "$currentLine $word"
                if (paint.measureText(candidate) > pageWidth - margin * 2 && currentLine.isNotEmpty()) {
                    wrapped += currentLine.toString()
                    currentLine = StringBuilder(word)
                } else {
                    if (currentLine.isNotEmpty()) currentLine.append(' ')
                    currentLine.append(word)
                }
            }
            if (currentLine.isNotEmpty()) wrapped += currentLine.toString()
            wrapped.forEach { renderedLine ->
                if (y > pageHeight - margin) startNextPage()
                page.canvas.drawText(renderedLine, margin, y, paint)
                y += if (paint === headingPaint || paint === exercisePaint) 24f else 19f
            }
            y += if (paint === headingPaint || paint === exercisePaint) 8f else 2f
        }
        document.finishPage(page)
        outputFile.outputStream().use(document::writeTo)
    } finally {
        document.close()
    }

    val uri = FileProvider.getUriForFile(
        androidContext,
        "${androidContext.packageName}.fileprovider",
        outputFile
    )
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        clipData = ClipData.newUri(androidContext.contentResolver, title, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    val chooser = Intent.createChooser(sendIntent, title)
    if (androidContext !is android.app.Activity) chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    androidContext.startActivity(chooser)
    true
}.getOrDefault(false)
