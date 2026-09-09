package com.example.engine

import android.content.Context
import android.util.Log

/**
 * Global Crash-Shield and Panic Guardian.
 * Intercepts uncaught thread exceptions, immediately flushes active document buffers
 * to atomic emergency storage, and prevents total data loss.
 */
object AppCrashShield {
    private const val TAG = "AppCrashShield"
    private var isInitialized = false

    fun install(context: Context, activeTextProvider: () -> Pair<String?, String?>) {
        if (isInitialized) return
        isInitialized = true

        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                Log.e(TAG, "Uncaught exception in thread ${thread.name}: ${throwable.localizedMessage}", throwable)

                // Emergency save of current in-memory text buffer
                val (docName, text) = activeTextProvider()
                if (!text.isNullOrBlank()) {
                    DraftManager.saveDraftAtomic(
                        context = context.applicationContext,
                        uriString = "emergency://crash_recovery",
                        docName = docName ?: "Unbenanntes Dokument",
                        text = text
                    )
                    Log.i(TAG, "Emergency atomic crash backup saved successfully.")
                }
            } catch (e: Throwable) {
                Log.e(TAG, "Failed during crash shield emergency flush", e)
            } finally {
                // Delegate to default Android runtime handler
                defaultHandler?.uncaughtException(thread, throwable)
            }
        }
    }
}
