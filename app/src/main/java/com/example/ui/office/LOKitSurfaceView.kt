package com.example.ui.office

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.example.engine.lokit.LOKitNativeWindowRenderer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * High-performance SurfaceView that displays LibreOfficeKit documents
 * by painting directly into the underlying ANativeWindow hardware buffer.
 *
 * Avoids any intermediate Bitmap allocations or CPU-to-GPU uploads.
 */
class LOKitSurfaceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : SurfaceView(context, attrs, defStyleAttr), SurfaceHolder.Callback {

    private val viewScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var docHandle: Long = 0L

    // Viewport position in twips
    private var scrollXTwips: Int = 0
    private var scrollYTwips: Int = 0
    private var currentZoomFactor: Float = 1.0f

    // Document bounds in twips
    private var docWidthTwips: Long = 11906L
    private var docHeightTwips: Long = 16838L

    private var isSurfaceReady = false

    private val scaleDetector = ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            currentZoomFactor = (currentZoomFactor * detector.scaleFactor).coerceIn(0.5f, 4.0f)
            requestDirectRender()
            return true
        }
    })

    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            val deltaXTwips = LOKitNativeWindowRenderer.pixelsToTwips(distanceX, resources.displayMetrics.densityDpi.toFloat())
            val deltaYTwips = LOKitNativeWindowRenderer.pixelsToTwips(distanceY, resources.displayMetrics.densityDpi.toFloat())

            scrollXTwips = (scrollXTwips + deltaXTwips).coerceIn(0, docWidthTwips.toInt())
            scrollYTwips = (scrollYTwips + deltaYTwips).coerceIn(0, docHeightTwips.toInt())

            requestDirectRender()
            return true
        }
    })

    init {
        holder.addCallback(this)
        setFocusable(true)
    }

    fun setDocument(handle: Long) {
        this.docHandle = handle
        val size = LOKitNativeWindowRenderer.getDocumentSize(handle)
        this.docWidthTwips = size.widthTwips
        this.docHeightTwips = size.heightTwips
        if (isSurfaceReady) {
            requestDirectRender()
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        isSurfaceReady = true
        LOKitNativeWindowRenderer.configureSystemFonts()
        requestDirectRender()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        requestDirectRender()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        isSurfaceReady = false
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        var handled = scaleDetector.onTouchEvent(event)
        handled = gestureDetector.onTouchEvent(event) || handled
        return handled || super.onTouchEvent(event)
    }

    /**
     * Executes direct zero-copy tile rendering into the ANativeWindow.
     */
    fun requestDirectRender() {
        if (!isSurfaceReady) return
        val currentSurface = holder.surface
        if (currentSurface == null || !currentSurface.isValid) return

        val canvasW = width.coerceAtLeast(1)
        val canvasH = height.coerceAtLeast(1)

        val viewportWidthTwips = (LOKitNativeWindowRenderer.pixelsToTwips(
            canvasW.toFloat(),
            resources.displayMetrics.densityDpi.toFloat()
        ) / currentZoomFactor).toInt()

        val viewportHeightTwips = (LOKitNativeWindowRenderer.pixelsToTwips(
            canvasH.toFloat(),
            resources.displayMetrics.densityDpi.toFloat()
        ) / currentZoomFactor).toInt()

        val viewport = LOKitNativeWindowRenderer.TwipsRect(
            x = scrollXTwips,
            y = scrollYTwips,
            width = viewportWidthTwips,
            height = viewportHeightTwips
        )

        viewScope.launch {
            LOKitNativeWindowRenderer.renderTileToSurface(
                docHandle = docHandle,
                surface = currentSurface,
                canvasWidth = canvasW,
                canvasHeight = canvasH,
                viewportTwips = viewport
            )
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        viewScope.cancel()
    }
}
