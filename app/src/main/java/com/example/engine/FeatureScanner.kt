package com.example.engine

import android.content.Context
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import com.example.model.DocumentFeatures
import com.example.model.DocumentFormat
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.zip.ZipInputStream

object FeatureScanner {

    fun scanDocument(context: Context, uri: Uri, format: DocumentFormat, rawBytes: ByteArray): DocumentFeatures {
        return try {
            when (format) {
                DocumentFormat.DOCX -> scanDocxBytes(rawBytes)
                DocumentFormat.ODT -> scanOdtBytes(rawBytes)
                DocumentFormat.PDF -> scanPdf(context, uri, rawBytes)
                DocumentFormat.MD -> scanMarkdownText(String(rawBytes, StandardCharsets.UTF_8), rawBytes)
                DocumentFormat.TXT -> scanPlainText(String(rawBytes, StandardCharsets.UTF_8), rawBytes)
                DocumentFormat.UNKNOWN -> DocumentFeatures(characterCount = rawBytes.size)
            }
        } catch (e: Exception) {
            // Fallback gracefully on parsing errors
            DocumentFeatures(
                characterCount = rawBytes.size,
                encoding = "Binary/Unknown"
            )
        }
    }

    private fun scanDocxBytes(rawBytes: ByteArray): DocumentFeatures {
        var hasTables = false
        var hasImages = false
        var hasHyperlinks = false
        var hasFootnotes = false
        var hasEndnotes = false
        var hasColumns = false
        var hasShapes = false
        var hasComments = false
        var hasTrackingChanges = false
        var estimatedWordCount = 0
        var rawXmlSize = 0L

        ZipInputStream(ByteArrayInputStream(rawBytes)).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                val name = entry.name.lowercase()
                if (name.contains("word/document.xml") || name.contains("word/document2.xml")) {
                    val streamResult = StreamingXmlDocxParser.parseStream(zis)
                    hasTables = hasTables || streamResult.hasTables
                    hasImages = hasImages || streamResult.hasImages
                    hasHyperlinks = hasHyperlinks || streamResult.hasHyperlinks
                    hasFootnotes = hasFootnotes || streamResult.hasFootnotes
                    hasEndnotes = hasEndnotes || streamResult.hasEndnotes
                    hasColumns = hasColumns || streamResult.hasColumns
                    hasShapes = hasShapes || streamResult.hasShapes
                    hasComments = hasComments || streamResult.hasComments
                    hasTrackingChanges = hasTrackingChanges || streamResult.hasTrackingChanges
                    estimatedWordCount += streamResult.wordCount
                    rawXmlSize += streamResult.textSnippet.length * 2L
                } else if (name.contains("word/footnotes.xml")) {
                    hasFootnotes = true
                } else if (name.contains("word/endnotes.xml")) {
                    hasEndnotes = true
                } else if (name.contains("word/comments.xml")) {
                    hasComments = true
                } else if (name.contains("word/media/")) {
                    hasImages = true
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }

        return DocumentFeatures(
            hasHyperlinks = hasHyperlinks,
            hasFootnotes = hasFootnotes,
            hasEndnotes = hasEndnotes,
            hasTables = hasTables,
            hasImages = hasImages,
            hasColumns = hasColumns,
            hasShapes = hasShapes,
            hasTrackingChanges = hasTrackingChanges,
            hasComments = hasComments,
            wordCount = estimatedWordCount,
            rawXmlSize = rawXmlSize,
            encoding = "UTF-8 (OOXML ZIP)"
        )
    }

    private fun scanOdtBytes(rawBytes: ByteArray): DocumentFeatures {
        var hasTables = false
        var hasImages = false
        var hasHyperlinks = false
        var estimatedWordCount = 0

        ZipInputStream(ByteArrayInputStream(rawBytes)).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                if (entry.name == "content.xml") {
                    val xml = String(zis.readBytes(), StandardCharsets.UTF_8)
                    if (xml.contains("<table:table")) hasTables = true
                    if (xml.contains("<draw:frame") || xml.contains("<draw:image")) hasImages = true
                    if (xml.contains("<text:a")) hasHyperlinks = true
                    val stripped = xml.replace(Regex("<[^>]*>"), " ")
                    estimatedWordCount = stripped.split(Regex("\\s+")).count { it.isNotBlank() }
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }

        return DocumentFeatures(
            hasTables = hasTables,
            hasImages = hasImages,
            hasHyperlinks = hasHyperlinks,
            wordCount = estimatedWordCount,
            encoding = "UTF-8 (ODF ZIP)"
        )
    }

    private fun scanPdf(context: Context, uri: Uri, rawBytes: ByteArray): DocumentFeatures {
        var pageCount = 1
        var tempFile: java.io.File? = null
        try {
            tempFile = java.io.File.createTempFile("pdf_scan_", ".pdf", context.cacheDir)
            tempFile.writeBytes(rawBytes)
            val pfd = ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY)
            val renderer = PdfRenderer(pfd)
            pageCount = renderer.pageCount
            renderer.close()
            pfd.close()
        } catch (e: Exception) {
            // If PdfRenderer fails, parse /Count manually from raw bytes
            val rawStr = String(rawBytes.take(16384).toByteArray(), StandardCharsets.ISO_8859_1)
            val match = Regex("/Count\\s+(\\d+)").find(rawStr)
            pageCount = match?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 1
        } finally {
            tempFile?.delete()
        }

        val hasAnnots = String(rawBytes, StandardCharsets.ISO_8859_1).contains("/Annots")
        val hasImages = String(rawBytes, StandardCharsets.ISO_8859_1).contains("/Image")

        return DocumentFeatures(
            pageCount = pageCount,
            hasHyperlinks = hasAnnots,
            hasImages = hasImages,
            encoding = "PDF 1.4 - 1.7"
        )
    }

    private fun scanMarkdownText(text: String, rawBytes: ByteArray): DocumentFeatures {
        val hasBom = rawBytes.size >= 3 &&
                rawBytes[0] == 0xEF.toByte() &&
                rawBytes[1] == 0xBB.toByte() &&
                rawBytes[2] == 0xBF.toByte()

        val lines = text.lines()
        val words = text.split(Regex("\\s+")).filter { it.isNotBlank() }
        val hasTables = lines.any { it.contains("|") && it.contains("-") }
        val hasImages = text.contains("![") && text.contains("](")
        val hasLinks = text.contains("[") && text.contains("](")
        val hasFootnotes = text.contains("[^")

        return DocumentFeatures(
            hasTables = hasTables,
            hasImages = hasImages,
            hasHyperlinks = hasLinks,
            hasFootnotes = hasFootnotes,
            wordCount = words.size,
            characterCount = text.length,
            lineCount = lines.size,
            encoding = "UTF-8",
            hasBom = hasBom
        )
    }

    private fun scanPlainText(text: String, rawBytes: ByteArray): DocumentFeatures {
        val hasBom = rawBytes.size >= 3 &&
                rawBytes[0] == 0xEF.toByte() &&
                rawBytes[1] == 0xBB.toByte() &&
                rawBytes[2] == 0xBF.toByte()

        val lines = text.lines()
        val words = text.split(Regex("\\s+")).filter { it.isNotBlank() }

        return DocumentFeatures(
            wordCount = words.size,
            characterCount = text.length,
            lineCount = lines.size,
            encoding = "UTF-8",
            hasBom = hasBom
        )
    }
}
