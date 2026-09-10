package com.example

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.example.engine.BytePreservingStorageEngine
import com.example.engine.ConversionMatrix
import com.example.model.DocumentFeatures
import com.example.model.DocumentFormat
import com.example.model.DocumentMetadata
import com.example.model.SaveResult
import com.example.model.TextCommand
import com.example.model.UndoRedoManager
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.nio.charset.StandardCharsets

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun testReadAppNameFromContext() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("AllDocsOff", appName)
    }

    @Test
    fun testBytePreservingSaveCopiesExactBytesWhenNotDirty() {
        runBlocking {
            val context = ApplicationProvider.getApplicationContext<Context>()
            val originalContent = "Header\r\nSpecial Bytes: ÄÖÜß\tTest 123456\nFooter"
            val originalBytes = originalContent.toByteArray(StandardCharsets.UTF_8)
            val originalSha = BytePreservingStorageEngine.computeSha256(originalBytes)

            val tempTarget = File.createTempFile("test_target_", ".txt", context.cacheDir)
            val targetUri = Uri.fromFile(tempTarget)

            val meta = DocumentMetadata(
                uri = Uri.parse("file://sample.txt"),
                name = "sample.txt",
                sizeBytes = originalBytes.size.toLong(),
                lastModified = 1000L,
                format = DocumentFormat.TXT,
                originalSha256 = originalSha,
                currentSha256 = originalSha,
                isDirty = false
            )

            val result = BytePreservingStorageEngine.saveDocument(
                context = context,
                metadata = meta,
                targetUri = targetUri,
                targetFormat = DocumentFormat.TXT,
                isDirty = false,
                originalBytes = originalBytes
            )

            assertTrue(result is SaveResult.Success)
            val success = result as SaveResult.Success
            assertTrue("Expected isBytePreserved to be true", success.isBytePreserved)
            assertEquals(originalSha, success.sha256Saved)
            assertEquals(originalBytes.size.toLong(), success.savedSizeBytes)

            val readBack = tempTarget.readBytes()
            assertEquals(originalBytes.size, readBack.size)
            assertTrue(originalBytes.contentEquals(readBack))

            tempTarget.delete()
        }
    }

    @Test
    fun testConversionMatrixWarnsOnDocxToTxtLoss() {
        val features = DocumentFeatures(
            hasTables = true,
            hasImages = true,
            hasHyperlinks = true
        )
        val report = ConversionMatrix.checkConversion(
            from = DocumentFormat.DOCX,
            to = DocumentFormat.TXT,
            features = features
        )

        assertFalse("DOCX to TXT should not be lossless", report.isLossless)
        assertTrue("Should contain warnings", report.warnings.isNotEmpty())
        assertTrue("Should warn about tables", report.warnings.any { it.title.contains("Tabelle") })
        assertTrue("Should warn about images", report.warnings.any { it.title.contains("Bilder") })
    }

    @Test
    fun testConversionMatrixConsidersSameFormatLossless() {
        val features = DocumentFeatures()
        val report = ConversionMatrix.checkConversion(
            from = DocumentFormat.DOCX,
            to = DocumentFormat.DOCX,
            features = features
        )

        assertTrue("Same format must be lossless", report.isLossless)
        assertTrue("Same format should have 0 warnings", report.warnings.isEmpty())
    }

    @Test
    fun testUndoRedoCommandManagerOperatesCorrectly() {
        val manager = UndoRedoManager()
        var text = "Hello"

        val cmd1 = TextCommand.Insert(5, " World")
        manager.pushCommand(cmd1)
        text = cmd1.apply(text)
        assertEquals("Hello World", text)

        assertTrue(manager.canUndo)
        text = manager.undo(text)
        assertEquals("Hello", text)

        assertTrue(manager.canRedo)
        text = manager.redo(text)
        assertEquals("Hello World", text)
    }

    @Test
    fun testLOKitTwipsAndPixelConversions() {
        // 160 dpi => 1 inch = 160 pixels = 1440 twips
        val twips = com.example.engine.lokit.LOKitNativeWindowRenderer.pixelsToTwips(160f, 160f)
        assertEquals(1440, twips)

        val pixels = com.example.engine.lokit.LOKitNativeWindowRenderer.twipsToPixels(1440, 160f)
        assertEquals(160f, pixels, 0.01f)
    }

    @Test
    fun testLOKitDefaultDocumentDimensions() {
        val dims = com.example.engine.lokit.LOKitNativeWindowRenderer.getDocumentSize(0L)
        assertTrue("Width in twips should be > 0", dims.widthTwips > 0)
        assertTrue("Height in twips should be > 0", dims.heightTwips > 0)
    }

    @Test
    fun testKernelZeroCopyTransferCopiesFileCorrectly() {
        runBlocking {
            val context = ApplicationProvider.getApplicationContext<Context>()
            val sourceFile = File.createTempFile("zero_copy_src_", ".bin", context.cacheDir)
            val targetFile = File.createTempFile("zero_copy_dest_", ".bin", context.cacheDir)

            val testData = "Zero-Copy Kernel transferTo test data for Samsung A25 Exynos".toByteArray(StandardCharsets.UTF_8)
            sourceFile.writeBytes(testData)

            val stats = com.example.engine.KernelZeroCopyTransfer.transfer(sourceFile, targetFile)
            assertEquals(testData.size.toLong(), stats.bytesTransferred)
            assertEquals(testData.size.toLong(), targetFile.length())
            assertTrue(testData.contentEquals(targetFile.readBytes()))

            sourceFile.delete()
            targetFile.delete()
        }
    }

    @Test
    fun testStreamingXmlDocxParserExtractsTextCorrectly() {
        val sampleXml = """
            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
            <w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
                <w:body>
                    <w:p><w:r><w:t>Hallo</w:t></w:r><w:r><w:t> </w:t></w:r><w:r><w:t>Samsung A25!</w:t></w:r></w:p>
                    <w:p><w:r><w:t>Zeile zwei mit Expat Parser.</w:t></w:r></w:p>
                </w:body>
            </w:document>
        """.trimIndent()

        val parseResult = com.example.engine.StreamingXmlDocxParser.parseStream(sampleXml.byteInputStream())
        assertTrue(parseResult.textSnippet.contains("Hallo Samsung A25!"))
        assertTrue(parseResult.textSnippet.contains("Zeile zwei mit Expat Parser."))
    }

    @Test
    fun testKnoxVaultHeaderDetection() {
        val plainBytes = "Plain text document content".toByteArray(StandardCharsets.UTF_8)
        assertFalse(com.example.engine.security.KnoxHardwareVault.isKnoxEncrypted(plainBytes))

        val knoxHeader = "KNOX_VAULT_V1".toByteArray(StandardCharsets.UTF_8) + byteArrayOf(0, 0, 0, 12) + ByteArray(20)
        assertTrue(com.example.engine.security.KnoxHardwareVault.isKnoxEncrypted(knoxHeader))
    }
}
