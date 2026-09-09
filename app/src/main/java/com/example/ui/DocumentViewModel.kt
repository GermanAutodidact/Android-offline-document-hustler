package com.example.ui

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.engine.BytePreservingStorageEngine
import com.example.engine.ConversionMatrix
import com.example.engine.FeatureScanner
import com.example.engine.SampleDocumentProvider
import com.example.model.ConversionReport
import com.example.model.DocumentFeatures
import com.example.model.DocumentFormat
import com.example.model.DocumentMetadata
import com.example.model.SaveResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.nio.charset.StandardCharsets

data class DocumentUiState(
    val metadata: DocumentMetadata? = null,
    val rawBytes: ByteArray? = null,
    val textContent: String = "",
    val features: DocumentFeatures = DocumentFeatures(),
    val isSaving: Boolean = false,
    val saveResult: SaveResult? = null,
    val preFlightReport: ConversionReport? = null,
    val showIntegrityDialog: Boolean = false,
    val showFormatPicker: Boolean = false,
    val showKnoxVaultDialog: Boolean = false,
    val isAmoledBlackMode: Boolean = false,
    val sampleDocs: List<SampleDocumentProvider.SampleDoc> = emptyList(),
    val statusMessage: String? = null
)

class DocumentViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(DocumentUiState())
    val uiState: StateFlow<DocumentUiState> = _uiState.asStateFlow()

    fun loadSamples(context: Context) {
        viewModelScope.launch {
            val samples = SampleDocumentProvider.initializeSampleDocuments(context)
            _uiState.value = _uiState.value.copy(sampleDocs = samples)
        }
    }

    fun openDocument(context: Context, uri: Uri, explicitName: String? = null) {
        viewModelScope.launch {
            try {
                val bytes = BytePreservingStorageEngine.readUriBytes(context, uri)
                val resolvedName = explicitName ?: (uri.lastPathSegment ?: "document")
                val format = DocumentFormat.fromFileNameOrMime(resolvedName, context.contentResolver.getType(uri))
                val sha256 = BytePreservingStorageEngine.computeSha256(bytes)
                val features = FeatureScanner.scanDocument(context, uri, format, bytes)

                val text = if (format == DocumentFormat.TXT || format == DocumentFormat.MD) {
                    String(bytes, StandardCharsets.UTF_8)
                } else {
                    ""
                }

                val metadata = DocumentMetadata(
                    uri = uri,
                    name = resolvedName,
                    sizeBytes = bytes.size.toLong(),
                    lastModified = System.currentTimeMillis(),
                    format = format,
                    originalSha256 = sha256,
                    currentSha256 = sha256,
                    isDirty = false
                )

                _uiState.value = _uiState.value.copy(
                    metadata = metadata,
                    rawBytes = bytes,
                    textContent = text,
                    features = features,
                    saveResult = null,
                    statusMessage = "Dokument geöffnet: $resolvedName (${format.name})"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    statusMessage = "Fehler beim Öffnen: ${e.localizedMessage}"
                )
            }
        }
    }

    fun onTextChanged(newText: String) {
        val currentMeta = _uiState.value.metadata ?: return
        val currentBytes = _uiState.value.rawBytes ?: return

        val newBytes = newText.toByteArray(StandardCharsets.UTF_8)
        val newHash = BytePreservingStorageEngine.computeSha256(newBytes)
        val isModified = !newHash.equals(currentMeta.originalSha256, ignoreCase = true)

        _uiState.value = _uiState.value.copy(
            textContent = newText,
            metadata = currentMeta.copy(
                isDirty = isModified,
                currentSha256 = newHash
            )
        )
    }

    fun onPageRotated() {
        val currentMeta = _uiState.value.metadata ?: return
        _uiState.value = _uiState.value.copy(
            metadata = currentMeta.copy(isDirty = true)
        )
    }

    fun requestSaveOrPreFlight(targetFormat: DocumentFormat) {
        val meta = _uiState.value.metadata ?: return
        val features = _uiState.value.features
        val report = ConversionMatrix.checkConversion(meta.format, targetFormat, features)

        if (report.warnings.isNotEmpty() && !report.isLossless) {
            _uiState.value = _uiState.value.copy(preFlightReport = report)
        } else {
            // Direct save allowed
            _uiState.value = _uiState.value.copy(preFlightReport = report)
        }
    }

    fun dismissPreFlight() {
        _uiState.value = _uiState.value.copy(preFlightReport = null)
    }

    fun executeSave(context: Context, targetUri: Uri, targetFormat: DocumentFormat) {
        val state = _uiState.value
        val meta = state.metadata ?: return
        val rawBytes = state.rawBytes ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, preFlightReport = null)

            val result = BytePreservingStorageEngine.saveDocument(
                context = context,
                metadata = meta,
                targetUri = targetUri,
                targetFormat = targetFormat,
                isDirty = meta.isDirty,
                originalBytes = rawBytes,
                editedTextContent = if (targetFormat == DocumentFormat.TXT || targetFormat == DocumentFormat.MD) state.textContent else null
            )

            val updatedMeta = if (result is SaveResult.Success) {
                meta.copy(
                    isDirty = false,
                    originalSha256 = result.sha256Saved,
                    currentSha256 = result.sha256Saved,
                    sizeBytes = result.savedSizeBytes
                )
            } else {
                meta
            }

            _uiState.value = _uiState.value.copy(
                isSaving = false,
                saveResult = result,
                metadata = updatedMeta,
                showIntegrityDialog = result is SaveResult.Success,
                statusMessage = when (result) {
                    is SaveResult.Success -> result.message
                    is SaveResult.Failure -> "Speichern fehlgeschlagen: ${result.reason}"
                }
            )
        }
    }

    fun setIntegrityDialogVisible(visible: Boolean) {
        _uiState.value = _uiState.value.copy(showIntegrityDialog = visible)
    }

    fun setFormatPickerVisible(visible: Boolean) {
        _uiState.value = _uiState.value.copy(showFormatPicker = visible)
    }

    fun setKnoxVaultDialogVisible(visible: Boolean) {
        _uiState.value = _uiState.value.copy(showKnoxVaultDialog = visible)
    }

    fun toggleAmoledMode() {
        val current = _uiState.value.isAmoledBlackMode
        _uiState.value = _uiState.value.copy(
            isAmoledBlackMode = !current,
            statusMessage = if (!current) "Super-AMOLED True-Black aktiv (#000000 · 0 mA)" else "Standard-Farbschema aktiv"
        )
    }

    fun encryptCurrentDocumentWithKnox() {
        val bytes = _uiState.value.rawBytes ?: return
        try {
            val result = com.example.engine.security.KnoxHardwareVault.encryptDocument(bytes)
            _uiState.value = _uiState.value.copy(
                rawBytes = result.encryptedBytes,
                statusMessage = "Dokument im Samsung Knox Hardware-Tresor (AES-256-GCM) verschlüsselt! 🔒",
                showKnoxVaultDialog = false
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                statusMessage = "Knox-Verschlüsselung fehlgeschlagen: ${e.localizedMessage}"
            )
        }
    }

    fun decryptCurrentDocumentWithKnox() {
        val bytes = _uiState.value.rawBytes ?: return
        try {
            val decryptedBytes = com.example.engine.security.KnoxHardwareVault.decryptDocument(bytes)
            val newHash = BytePreservingStorageEngine.computeSha256(decryptedBytes)
            val meta = _uiState.value.metadata
            val text = if (meta?.format == DocumentFormat.TXT || meta?.format == DocumentFormat.MD) {
                String(decryptedBytes, StandardCharsets.UTF_8)
            } else _uiState.value.textContent

            _uiState.value = _uiState.value.copy(
                rawBytes = decryptedBytes,
                textContent = text,
                metadata = meta?.copy(currentSha256 = newHash),
                statusMessage = "Dokument erfolgreich mit Knox-Hardware-Schlüssel entsperrt! 🔓",
                showKnoxVaultDialog = false
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                statusMessage = "Knox-Entschlüsselung fehlgeschlagen: ${e.localizedMessage}"
            )
        }
    }

    fun printCurrentDocument(context: Context) {
        val bytes = _uiState.value.rawBytes ?: return
        val meta = _uiState.value.metadata ?: return
        viewModelScope.launch {
            try {
                val tempPdf = java.io.File.createTempFile("print_job_", ".pdf", context.cacheDir)
                if (meta.format == DocumentFormat.PDF) {
                    tempPdf.writeBytes(bytes)
                } else {
                    // Generate basic printable PDF via PdfDocument if text format
                    val pdfDoc = android.graphics.pdf.PdfDocument()
                    val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
                    val page = pdfDoc.startPage(pageInfo)
                    val paint = android.graphics.Paint().apply {
                        textSize = 12f
                        color = android.graphics.Color.BLACK
                    }
                    val textToPrint = if (_uiState.value.textContent.isNotBlank()) _uiState.value.textContent else meta.name
                    var y = 50f
                    for (line in textToPrint.lines().take(50)) {
                        page.canvas.drawText(line, 40f, y, paint)
                        y += 16f
                    }
                    pdfDoc.finishPage(page)
                    java.io.FileOutputStream(tempPdf).use { pdfDoc.writeTo(it) }
                    pdfDoc.close()
                }

                com.example.engine.SystemPdfPrinter.printPdfDocument(
                    context = context,
                    pdfFile = tempPdf,
                    jobName = meta.name
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    statusMessage = "Drucken fehlgeschlagen: ${e.localizedMessage}"
                )
            }
        }
    }

    fun closeDocument() {
        _uiState.value = _uiState.value.copy(
            metadata = null,
            rawBytes = null,
            textContent = "",
            features = DocumentFeatures(),
            saveResult = null,
            preFlightReport = null
        )
    }

    fun createNewDocument(format: DocumentFormat) {
        val name = when (format) {
            DocumentFormat.MD -> "neues_dokument.md"
            DocumentFormat.TXT -> "notizen.txt"
            else -> "dokument.${format.extension}"
        }
        val emptyBytes = ByteArray(0)
        val sha = BytePreservingStorageEngine.computeSha256(emptyBytes)
        val metadata = DocumentMetadata(
            uri = Uri.parse("memory://$name"),
            name = name,
            sizeBytes = 0,
            lastModified = System.currentTimeMillis(),
            format = format,
            originalSha256 = sha,
            currentSha256 = sha,
            isDirty = true
        )
        _uiState.value = _uiState.value.copy(
            metadata = metadata,
            rawBytes = emptyBytes,
            textContent = "",
            features = DocumentFeatures(),
            saveResult = null
        )
    }
}
