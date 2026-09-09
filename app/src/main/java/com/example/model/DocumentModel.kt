package com.example.model

import android.net.Uri

enum class DocumentFormat(val extension: String, val mimeType: String, val displayName: String) {
    PDF("pdf", "application/pdf", "Portable Document Format (.pdf)"),
    DOCX("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "Word Dokument (.docx)"),
    ODT("odt", "application/vnd.oasis.opendocument.text", "OpenDocument Text (.odt)"),
    MD("md", "text/markdown", "Markdown (.md)"),
    TXT("txt", "text/plain", "Textdokument (.txt)"),
    UNKNOWN("bin", "application/octet-stream", "Unbekanntes Format");

    companion object {
        fun fromFileNameOrMime(name: String?, mime: String?): DocumentFormat {
            val lowerName = name?.lowercase() ?: ""
            return when {
                lowerName.endsWith(".pdf") || mime == "application/pdf" -> PDF
                lowerName.endsWith(".docx") || mime?.contains("wordprocessingml") == true -> DOCX
                lowerName.endsWith(".odt") || mime?.contains("opendocument.text") == true -> ODT
                lowerName.endsWith(".md") || lowerName.endsWith(".markdown") || mime == "text/markdown" -> MD
                lowerName.endsWith(".txt") || mime?.startsWith("text/") == true -> TXT
                else -> UNKNOWN
            }
        }
    }
}

data class DocumentMetadata(
    val uri: Uri,
    val name: String,
    val sizeBytes: Long,
    val lastModified: Long,
    val format: DocumentFormat,
    val originalSha256: String,
    val currentSha256: String = originalSha256,
    val isDirty: Boolean = false,
    val isReadOnly: Boolean = false
)

data class DocumentFeatures(
    val hasHyperlinks: Boolean = false,
    val hasFootnotes: Boolean = false,
    val hasEndnotes: Boolean = false,
    val hasTables: Boolean = false,
    val hasImages: Boolean = false,
    val hasColumns: Boolean = false,
    val hasShapes: Boolean = false,
    val hasWatermark: Boolean = false,
    val hasTrackingChanges: Boolean = false,
    val hasComments: Boolean = false,
    val pageCount: Int = 1,
    val wordCount: Int = 0,
    val characterCount: Int = 0,
    val lineCount: Int = 0,
    val encoding: String = "UTF-8",
    val hasBom: Boolean = false,
    val rawXmlSize: Long = 0L
) {
    val totalRichFeatures: Int
        get() = listOf(
            hasHyperlinks, hasFootnotes, hasEndnotes, hasTables,
            hasImages, hasColumns, hasShapes, hasWatermark,
            hasTrackingChanges, hasComments
        ).count { it }
}

data class ConversionWarning(
    val title: String,
    val description: String,
    val severity: WarningSeverity
)

enum class WarningSeverity {
    INFO,
    WARNING,
    CRITICAL_DATA_LOSS
}

data class ConversionReport(
    val fromFormat: DocumentFormat,
    val toFormat: DocumentFormat,
    val isLossless: Boolean,
    val warnings: List<ConversionWarning>,
    val summary: String,
    val canConvertDirectly: Boolean
)

sealed class SaveResult {
    data class Success(
        val isBytePreserved: Boolean,
        val targetUri: Uri,
        val sha256Original: String,
        val sha256Saved: String,
        val savedSizeBytes: Long,
        val message: String
    ) : SaveResult()

    data class Failure(
        val reason: String,
        val exception: Throwable? = null
    ) : SaveResult()
}
