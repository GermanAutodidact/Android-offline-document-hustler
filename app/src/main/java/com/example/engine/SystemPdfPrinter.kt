package com.example.engine

import android.content.Context
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * Native Android Print & PDF Spooler integration using pure OS Bordmittel.
 *
 * Invokes the Android System Print Spooler:
 * - Allows 1-click "Als PDF drucken / speichern" via the Samsung system print service.
 * - Uses zero-copy kernel streaming from the document cache directly into the print spooler pipe.
 */
object SystemPdfPrinter {

    private const val TAG = "SystemPdfPrinter"

    fun printPdfDocument(
        context: Context,
        pdfFile: File,
        jobName: String = "Dokument_Druck"
    ): Boolean {
        if (!pdfFile.exists() || pdfFile.length() == 0L) {
            Log.e(TAG, "Druckdatei existiert nicht oder ist leer: ${pdfFile.absolutePath}")
            return false
        }

        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
        if (printManager == null) {
            Log.e(TAG, "PrintManager nicht verfügbar auf diesem Gerät")
            return false
        }

        val adapter = object : PrintDocumentAdapter() {
            override fun onLayout(
                oldAttributes: PrintAttributes?,
                newAttributes: PrintAttributes?,
                cancellationSignal: CancellationSignal?,
                callback: LayoutResultCallback?,
                extras: Bundle?
            ) {
                if (cancellationSignal?.isCanceled == true) {
                    callback?.onLayoutCancelled()
                    return
                }

                val info = PrintDocumentInfo.Builder(jobName)
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
                    .build()

                callback?.onLayoutFinished(info, newAttributes != oldAttributes)
            }

            override fun onWrite(
                pages: Array<out PageRange>?,
                destination: ParcelFileDescriptor?,
                cancellationSignal: CancellationSignal?,
                callback: WriteResultCallback?
            ) {
                if (cancellationSignal?.isCanceled == true) {
                    callback?.onWriteCancelled()
                    return
                }

                if (destination == null) {
                    callback?.onWriteFailed("Zielfile-Descriptor ist null")
                    return
                }

                try {
                    // Kernel-level channel transfer into the print spooler file descriptor
                    FileInputStream(pdfFile).channel.use { inChannel ->
                        FileOutputStream(destination.fileDescriptor).channel.use { outChannel ->
                            val size = inChannel.size()
                            var position = 0L
                            while (position < size) {
                                if (cancellationSignal?.isCanceled == true) {
                                    callback?.onWriteCancelled()
                                    return
                                }
                                val transferred = inChannel.transferTo(position, size - position, outChannel)
                                if (transferred <= 0) break
                                position += transferred
                            }
                        }
                    }
                    callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                } catch (e: Exception) {
                    Log.e(TAG, "Fehler beim Schreiben des Druckauftrags", e)
                    callback?.onWriteFailed(e.localizedMessage)
                }
            }
        }

        printManager.print(jobName, adapter, PrintAttributes.Builder().build())
        return true
    }
}
