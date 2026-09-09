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
        assertEquals("DocPreserve", appName)
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
}
