package com.example.engine

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object SampleDocumentProvider {

    data class SampleDoc(
        val name: String,
        val file: File,
        val uri: Uri,
        val description: String,
        val formatTag: String
    )

    fun initializeSampleDocuments(context: Context): List<SampleDoc> {
        val samplesDir = File(context.filesDir, "samples").apply { mkdirs() }

        val mdFile = File(samplesDir, "architecture_v1.md")
        if (!mdFile.exists()) {
            mdFile.writeText(SAMPLE_MARKDOWN, StandardCharsets.UTF_8)
        }

        val txtFile = File(samplesDir, "byte_preservation_manifesto.txt")
        if (!txtFile.exists()) {
            txtFile.writeText(SAMPLE_TEXT, StandardCharsets.UTF_8)
        }

        val pdfFile = File(samplesDir, "system_overview.pdf")
        if (!pdfFile.exists()) {
            createSamplePdf(pdfFile)
        }

        val docxFile = File(samplesDir, "audit_protocol.docx")
        if (!docxFile.exists()) {
            createSampleDocx(docxFile)
        }

        val getUri = { file: File ->
            Uri.fromFile(file)
        }

        return listOf(
            SampleDoc(
                name = mdFile.name,
                file = mdFile,
                uri = getUri(mdFile),
                description = "Markdown mit Tabellen, Checklisten & Formatierung",
                formatTag = "MD"
            ),
            SampleDoc(
                name = pdfFile.name,
                file = pdfFile,
                uri = getUri(pdfFile),
                description = "Mehrseitiges PDF gerendert mit Android PdfRenderer",
                formatTag = "PDF"
            ),
            SampleDoc(
                name = docxFile.name,
                file = docxFile,
                uri = getUri(docxFile),
                description = "Gültiges Word OOXML ZIP-Archiv mit Tabellen & XML-Struktur",
                formatTag = "DOCX"
            ),
            SampleDoc(
                name = txtFile.name,
                file = txtFile,
                uri = getUri(txtFile),
                description = "Reiner Text mit Umlauten zur No-Op-Save Verifikation",
                formatTag = "TXT"
            )
        )
    }

    private fun createSamplePdf(targetFile: File) {
        val doc = PdfDocument()
        val paint = Paint().apply { isAntiAlias = true }

        // Page 1
        val pageInfo1 = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 at 72dpi
        val page1 = doc.startPage(pageInfo1)
        val canvas1: Canvas = page1.canvas

        // Header background banner
        paint.color = Color.parseColor("#1E3A8A")
        canvas1.drawRect(0f, 0f, 595f, 120f, paint)

        // Title
        paint.color = Color.WHITE
        paint.textSize = 24f
        paint.isFakeBoldText = true
        canvas1.drawText("DocPreserve System Specification", 40f, 60f, paint)

        paint.textSize = 14f
        paint.isFakeBoldText = false
        paint.color = Color.parseColor("#93C5FD")
        canvas1.drawText("Phase 0: Native Android PdfRenderer & Byte-Preserving Save", 40f, 90f, paint)

        // Content
        paint.color = Color.parseColor("#0F172A")
        paint.textSize = 14f
        paint.isFakeBoldText = true
        canvas1.drawText("1. Grundsatz: Unveränderte Byte-Treue (No-Op-Save)", 40f, 160f, paint)

        paint.isFakeBoldText = false
        paint.color = Color.parseColor("#334155")
        paint.textSize = 11f
        canvas1.drawText("Wird ein Dokument geöffnet und ohne Editieren erneut gespeichert,", 40f, 185f, paint)
        canvas1.drawText("wird die Export-Engine komplett umgangen und die Datei Byte für Byte kopiert.", 40f, 202f, paint)
        canvas1.drawText("Damit ist garantiert, dass interne ZIP-Offsets, Zeitstempel und Metadaten identisch bleiben.", 40f, 219f, paint)

        // Table Box
        paint.color = Color.parseColor("#F1F5F9")
        canvas1.drawRoundRect(40f, 250f, 555f, 380f, 8f, 8f, paint)

        paint.color = Color.parseColor("#2563EB")
        paint.textSize = 12f
        paint.isFakeBoldText = true
        canvas1.drawText("PDF-Architektur Vergleich", 55f, 280f, paint)

        paint.isFakeBoldText = false
        paint.color = Color.parseColor("#1E293B")
        paint.textSize = 10f
        canvas1.drawText("• View / Zoom / Print: Android PdfRenderer (0 KB APK-Overhead)", 55f, 310f, paint)
        canvas1.drawText("• Page-Ops (Rotate, Split, Merge): qpdf (modular)", 55f, 330f, paint)
        canvas1.drawText("• Direkte Text-Chirurgie: PDFium (Lazy Dynamic Feature)", 55f, 350f, paint)

        // Page number
        paint.color = Color.GRAY
        paint.textSize = 10f
        canvas1.drawText("Seite 1 von 2", 260f, 810f, paint)
        doc.finishPage(page1)

        // Page 2
        val pageInfo2 = PdfDocument.PageInfo.Builder(595, 842, 2).create()
        val page2 = doc.startPage(pageInfo2)
        val canvas2: Canvas = page2.canvas

        paint.color = Color.parseColor("#0F172A")
        paint.textSize = 18f
        paint.isFakeBoldText = true
        canvas2.drawText("Pre-Flight Conversion & Speichersicherheit", 40f, 60f, paint)

        paint.color = Color.parseColor("#334155")
        paint.textSize = 11f
        paint.isFakeBoldText = false
        canvas2.drawText("Jede Datei wird vor der Konvertierung durch den FeatureScanner geprüft.", 40f, 90f, paint)
        canvas2.drawText("Mögliche Verluste (Formatierungen, Tabellen, Bilder) werden vorab signalisiert.", 40f, 107f, paint)

        paint.color = Color.GRAY
        paint.textSize = 10f
        canvas2.drawText("Seite 2 von 2", 260f, 810f, paint)
        doc.finishPage(page2)

        FileOutputStream(targetFile).use { doc.writeTo(it) }
        doc.close()
    }

    private fun createSampleDocx(targetFile: File) {
        ZipOutputStream(FileOutputStream(targetFile)).use { zos ->
            // [Content_Types].xml
            zos.putNextEntry(ZipEntry("[Content_Types].xml"))
            zos.write(DOCX_CONTENT_TYPES.toByteArray(StandardCharsets.UTF_8))
            zos.closeEntry()

            // _rels/.rels
            zos.putNextEntry(ZipEntry("_rels/.rels"))
            zos.write(DOCX_RELS.toByteArray(StandardCharsets.UTF_8))
            zos.closeEntry()

            // word/_rels/document.xml.rels
            zos.putNextEntry(ZipEntry("word/_rels/document.xml.rels"))
            zos.write(DOCX_DOCUMENT_RELS.toByteArray(StandardCharsets.UTF_8))
            zos.closeEntry()

            // word/document.xml
            zos.putNextEntry(ZipEntry("word/document.xml"))
            zos.write(DOCX_DOCUMENT_XML.toByteArray(StandardCharsets.UTF_8))
            zos.closeEntry()
        }
    }

    private val SAMPLE_MARKDOWN = """
# DocPreserve Entwurf: Phase 0

## 1. Wichtigste Neuerung: Byte-Preserving Save (No-Op-Save)
Wenn ein Dokument geöffnet und **nichts verändert** wird, kopiert die Engine die Originalbytes direkt:

- Keine Neu-Serialisierung durch LibreOffice oder Fremd-Engines
- SHA-256 Hash bleibt zu 100% identisch
- Erhält auch BOM, Zeilenumbrüche und ZIP-Strukturen

## 2. Komponenten-Übersicht

| Komponente | Engine | Speichergröße |
|---|---|---|
| PDF-Viewer | Android `PdfRenderer` | 0 KB |
| TXT/MD-Editor | Compose Text Buffer | 0 KB |
| Pre-Flight Scanner | XML / Header Parser | 0 KB |
| Sicherheit | Atomares Temp-File + Rollback | Integriert |

## 3. Pre-Flight Conversion Matrix
- **DOCX → TXT**: Warnung vor Verlust von Schriftarten, Tabellen und Grafiken.
- **PDF → MD**: Warnung vor Layout-Rekonstruktion.
- **MD → TXT**: Reine Textausgabe, Markdown-Tags bleiben erhalten.

---
*Erstellt mit DocPreserve Studio — Offline, bytegenau & sicher.*
""".trimIndent()

    private val SAMPLE_TEXT = """DocPreserve - Byte-Preservation Manifesto
=========================================

Problem in Standard-Office-Suiten:
Selbst wenn du nichts änderst, schreibt ein Office-Paket beim erneuten
Speichern eine neue Datei aus seinem internen Objektmodell. Dadurch ändern sich:
- ZIP-Kompressionslevel & Reihenfolge
- Interne Element-IDs und Timestamps
- Marginale Formatierungs- und Metadaten-Details

Lösung: Byte-Preserving Save
Wenn isDirty == false und targetFormat == sourceFormat:
Reine Byte-Kopie durchführen!

Damit bleibt:
- Originale Byte-Folge zu 100% erhalten (SHA-256 Match)
- Kein Informationsverlust
- Absolut risikofrei
""".trimIndent()

    private val DOCX_CONTENT_TYPES = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>
</Types>""".trimIndent()

    private val DOCX_RELS = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/>
</Relationships>""".trimIndent()

    private val DOCX_DOCUMENT_RELS = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
</Relationships>""".trimIndent()

    private val DOCX_DOCUMENT_XML = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
  <w:body>
    <w:p>
      <w:r>
        <w:t>DocPreserve Audit Protokoll</w:t>
      </w:r>
    </w:p>
    <w:p>
      <w:r>
        <w:t>Dieses Dokument dient als Byte-Prüfmuster für OOXML-Archive.</w:t>
      </w:r>
    </w:p>
    <w:tbl>
      <w:tr>
        <w:tc><w:p><w:r><w:t>Eigenschaft</w:t></w:r></w:p></w:tc>
        <w:tc><w:p><w:r><w:t>Status</w:t></w:r></w:p></w:tc>
      </w:tr>
      <w:tr>
        <w:tc><w:p><w:r><w:t>Byte-Preserving</w:t></w:r></w:p></w:tc>
        <w:tc><w:p><w:r><w:t>Aktiv</w:t></w:r></w:p></w:tc>
      </w:tr>
    </w:tbl>
    <w:p>
      <w:hyperlink w:id="rId2">
        <w:r><w:t>https://ai.studio</w:t></w:r>
      </w:hyperlink>
    </w:p>
  </w:body>
</w:document>""".trimIndent()
}
