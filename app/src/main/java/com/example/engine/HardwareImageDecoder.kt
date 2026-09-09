package com.example.engine

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ColorSpace
import android.graphics.ImageDecoder
import android.os.Build
import android.util.Log
import java.io.File
import java.nio.ByteBuffer

/**
 * Hardware-Accelerated Image Decoder utilizing Android's built-in ImageDecoder.
 *
 * Direct features on modern Android / Samsung devices:
 * - Allocator: ALLOCATOR_HARDWARE (creates GraphicBuffers directly in GPU VRAM, 0 JVM heap bloat)
 * - Hardware downsampling: decode directly at target resolution
 * - Super-AMOLED color gamut: DISPLAY_P3 wide color space targeting
 */
object HardwareImageDecoder {

    private const val TAG = "HardwareImageDecoder"

    /**
     * Decodes a byte array into a Hardware-backed Bitmap with hardware downsampling.
     */
    fun decodeByteArray(
        bytes: ByteArray,
        maxDimension: Int = 1080
    ): Bitmap? {
        if (bytes.isEmpty()) return null

        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val buffer = ByteBuffer.wrap(bytes)
                val source = ImageDecoder.createSource(buffer)
                ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                    // 1. Allocate directly into hardware GPU surface
                    decoder.allocator = ImageDecoder.ALLOCATOR_HARDWARE

                    // 2. Hardware downsampling to save memory and scale for Samsung Galaxy A25 FHD+ screen
                    val srcW = info.size.width
                    val srcH = info.size.height
                    if (maxDimension > 0 && (srcW > maxDimension || srcH > maxDimension)) {
                        val sampleFactor = (srcW.coerceAtLeast(srcH) / maxDimension).coerceAtLeast(1)
                        decoder.setTargetSize(srcW / sampleFactor, srcH / sampleFactor)
                    }

                    // 3. Wide color gamut for Super AMOLED display
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        try {
                            decoder.setTargetColorSpace(ColorSpace.get(ColorSpace.Named.DISPLAY_P3))
                        } catch (_: Exception) {}
                    }
                }
            } else {
                // Fallback for older Android APIs
                val options = BitmapFactory.Options().apply {
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                }
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Hardware image decode failed, fallback to software decode", e)
            try {
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            } catch (_: Exception) {
                null
            }
        }
    }

    /**
     * Decodes an image file into a Hardware Bitmap.
     */
    fun decodeFile(
        file: File,
        maxDimension: Int = 1080
    ): Bitmap? {
        if (!file.exists() || file.length() == 0L) return null

        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(file)
                ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                    decoder.allocator = ImageDecoder.ALLOCATOR_HARDWARE
                    val srcW = info.size.width
                    val srcH = info.size.height
                    if (maxDimension > 0 && (srcW > maxDimension || srcH > maxDimension)) {
                        val sampleFactor = (srcW.coerceAtLeast(srcH) / maxDimension).coerceAtLeast(1)
                        decoder.setTargetSize(srcW / sampleFactor, srcH / sampleFactor)
                    }
                }
            } else {
                BitmapFactory.decodeFile(file.absolutePath)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Hardware file decode failed", e)
            null
        }
    }
}
