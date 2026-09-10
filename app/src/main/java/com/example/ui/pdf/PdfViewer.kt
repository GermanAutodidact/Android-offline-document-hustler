package com.example.ui.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.concurrent.Executors

// Single thread dispatcher for PDF rendering as specified in design section 4.1
private val pdfDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

@Composable
fun PdfViewer(
    fileBytes: ByteArray,
    onPageRotationChanged: () -> Unit,
    onShowInfo: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var tempFile by remember { mutableStateOf<File?>(null) }
    var pdfRenderer by remember { mutableStateOf<PdfRenderer?>(null) }
    var pageCount by remember { mutableIntStateOf(0) }
    var currentPageIndex by remember { mutableIntStateOf(0) }
    var currentPageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var zoomScale by remember { mutableFloatStateOf(1.0f) }
    var rotationAngle by remember { mutableIntStateOf(0) }

    // Initialize PdfRenderer from cached file
    DisposableEffect(fileBytes) {
        val temp = File.createTempFile("view_doc_", ".pdf", context.cacheDir)
        temp.writeBytes(fileBytes)
        tempFile = temp

        try {
            val pfd = ParcelFileDescriptor.open(temp, ParcelFileDescriptor.MODE_READ_ONLY)
            val renderer = PdfRenderer(pfd)
            pdfRenderer = renderer
            pageCount = renderer.pageCount
            currentPageIndex = 0
        } catch (e: Exception) {
            e.printStackTrace()
        }

        onDispose {
            try {
                pdfRenderer?.close()
            } catch (_: Exception) {}
            temp.delete()
        }
    }

    // Render active page
    LaunchedEffect(pdfRenderer, currentPageIndex, rotationAngle) {
        val renderer = pdfRenderer ?: return@LaunchedEffect
        if (pageCount == 0) return@LaunchedEffect
        isLoading = true

        withContext(pdfDispatcher) {
            try {
                val page = renderer.openPage(currentPageIndex)
                val baseWidth = page.width * 2
                val baseHeight = page.height * 2
                val bitmap = Bitmap.createBitmap(baseWidth, baseHeight, Bitmap.Config.ARGB_8888)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()

                val finalBitmap = if (rotationAngle % 360 != 0) {
                    val matrix = Matrix().apply { postRotate(rotationAngle.toFloat()) }
                    Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                } else {
                    bitmap
                }

                currentPageBitmap = finalBitmap
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    fun printPdf() {
        val file = tempFile ?: return
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager ?: return
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
                val info = PrintDocumentInfo.Builder("AllDocsOff_Document.pdf")
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(pageCount)
                    .build()
                callback?.onLayoutFinished(info, true)
            }

            override fun onWrite(
                pages: Array<out PageRange>?,
                destination: ParcelFileDescriptor?,
                cancellationSignal: CancellationSignal?,
                callback: WriteResultCallback?
            ) {
                try {
                    val input = FileInputStream(file)
                    val output = FileOutputStream(destination?.fileDescriptor)
                    input.copyTo(output)
                    callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                } catch (e: Exception) {
                    callback?.onWriteFailed(e.message)
                }
            }
        }
        printManager.print("AllDocsOff PDF", adapter, null)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
    ) {
        // PDF Sub-toolbar: Zoom controls, rotate, print, page info
        Surface(
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Page indicator
                Text(
                    text = if (pageCount > 0) "Seite ${currentPageIndex + 1} / $pageCount" else "Lädt...",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Zoom Out
                    IconButton(
                        onClick = { zoomScale = (zoomScale - 0.25f).coerceAtLeast(0.75f) },
                        modifier = Modifier.size(36.dp).testTag("btn_pdf_zoom_out")
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_zoom_out),
                            contentDescription = "Verkleinern",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        text = "${(zoomScale * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )

                    // Zoom In
                    IconButton(
                        onClick = { zoomScale = (zoomScale + 0.25f).coerceAtMost(3.0f) },
                        modifier = Modifier.size(36.dp).testTag("btn_pdf_zoom_in")
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_zoom_in),
                            contentDescription = "Vergrößern",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Rotate 90°
                    IconButton(
                        onClick = {
                            rotationAngle = (rotationAngle + 90) % 360
                            onPageRotationChanged()
                        },
                        modifier = Modifier.size(36.dp).testTag("btn_pdf_rotate")
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_rotate_right),
                            contentDescription = "Seite drehen",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Print via Android PrintManager
                    IconButton(
                        onClick = { printPdf() },
                        modifier = Modifier.size(36.dp).testTag("btn_pdf_print")
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_print),
                            contentDescription = "Drucken (Android PrintManager)",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Info / Feature details
                    IconButton(
                        onClick = onShowInfo,
                        modifier = Modifier.size(36.dp).testTag("btn_pdf_info")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Dokumentdetails",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // PDF Canvas with 2-stage pinch-to-zoom gesture
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectTransformGestures { _, _, zoom, _ ->
                        zoomScale = (zoomScale * zoom).coerceIn(0.75f, 3.5f)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            val bitmap = currentPageBitmap
            if (isLoading && bitmap == null) {
                CircularProgressIndicator(modifier = Modifier.size(48.dp))
            } else if (bitmap != null) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    shadowElevation = 4.dp,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "PDF Seite ${currentPageIndex + 1}",
                        modifier = Modifier
                            .fillMaxWidth(zoomScale.coerceAtMost(2.5f))
                            .clip(RoundedCornerShape(4.dp)),
                        contentScale = ContentScale.FillWidth
                    )
                }
            } else {
                Text(
                    text = "Keine PDF-Seite verfügbar",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        // Page Navigation Slider / Buttons
        if (pageCount > 1) {
            Surface(
                tonalElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { if (currentPageIndex > 0) currentPageIndex-- },
                        enabled = currentPageIndex > 0,
                        modifier = Modifier.size(40.dp).testTag("btn_pdf_prev")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Vorherige Seite"
                        )
                    }

                    Slider(
                        value = currentPageIndex.toFloat(),
                        onValueChange = { currentPageIndex = it.toInt() },
                        valueRange = 0f..(pageCount - 1).toFloat(),
                        steps = (pageCount - 2).coerceAtLeast(0),
                        modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                    )

                    IconButton(
                        onClick = { if (currentPageIndex < pageCount - 1) currentPageIndex++ },
                        enabled = currentPageIndex < pageCount - 1,
                        modifier = Modifier.size(40.dp).testTag("btn_pdf_next")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Nächste Seite"
                        )
                    }
                }
            }
        }
    }
}
