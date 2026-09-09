package com.example.engine

import android.content.Context
import android.content.SharedPreferences
import android.util.AtomicFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.nio.charset.StandardCharsets

/**
 * Ultra-lightweight crash-protection & draft cache engine.
 * Features:
 * 1. Debounced auto-save (500ms) to avoid high disk/flash I/O on every single keystroke.
 * 2. AtomicFile transactions for corruption-proof storage even during abrupt power-off or low-memory kills.
 * 3. SharedPreferences metadata indexing for instant app-launch recovery.
 */
object DraftManager {
    private const val PREFS_NAME = "docpreserve_drafts"
    private const val KEY_DRAFT_TEXT = "cached_draft_text"
    private const val KEY_DRAFT_URI = "cached_draft_uri"
    private const val KEY_DRAFT_NAME = "cached_draft_name"
    private const val KEY_DRAFT_TIMESTAMP = "cached_draft_time"
    private const val KEY_AMOLED_MODE = "pref_amoled_black_mode"

    private val draftScope = CoroutineScope(Dispatchers.IO)
    private var debounceJob: Job? = null

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Debounced save: Waits 400ms after the last keystroke before committing to disk,
     * saving battery and flash storage wear.
     */
    fun scheduleDebouncedSave(context: Context, uriString: String, docName: String, text: String) {
        debounceJob?.cancel()
        debounceJob = draftScope.launch {
            delay(400)
            saveDraftAtomic(context, uriString, docName, text)
        }
    }

    /**
     * Immediate atomic save (used on app pause, backgrounding, or explicit flush).
     */
    fun saveDraftAtomic(context: Context, uriString: String, docName: String, text: String) {
        try {
            // Write payload atomically using Android's AtomicFile
            val draftFile = File(context.cacheDir, "emergency_draft.txt")
            val atomicFile = AtomicFile(draftFile)
            val bytes = text.toByteArray(StandardCharsets.UTF_8)

            var fos: java.io.FileOutputStream? = null
            try {
                fos = atomicFile.startWrite()
                fos.write(bytes)
                atomicFile.finishWrite(fos)
            } catch (e: Exception) {
                if (fos != null) {
                    atomicFile.failWrite(fos)
                }
            }

            // Update preferences metadata
            getPrefs(context).edit()
                .putString(KEY_DRAFT_URI, uriString)
                .putString(KEY_DRAFT_NAME, docName)
                .putString(KEY_DRAFT_TEXT, text)
                .putLong(KEY_DRAFT_TIMESTAMP, System.currentTimeMillis())
                .apply()
        } catch (e: Exception) {
            // Failsafe
        }
    }

    fun clearDraft(context: Context) {
        debounceJob?.cancel()
        getPrefs(context).edit()
            .remove(KEY_DRAFT_URI)
            .remove(KEY_DRAFT_NAME)
            .remove(KEY_DRAFT_TEXT)
            .remove(KEY_DRAFT_TIMESTAMP)
            .apply()

        try {
            val draftFile = File(context.cacheDir, "emergency_draft.txt")
            if (draftFile.exists()) {
                draftFile.delete()
            }
        } catch (e: Exception) {
            // Ignore
        }
    }

    fun getDraftText(context: Context): String? {
        // Try atomic file first, fallback to prefs
        try {
            val draftFile = File(context.cacheDir, "emergency_draft.txt")
            if (draftFile.exists() && draftFile.length() > 0) {
                return draftFile.readText(StandardCharsets.UTF_8)
            }
        } catch (e: Exception) {
            // Fallback
        }
        return getPrefs(context).getString(KEY_DRAFT_TEXT, null)
    }

    fun getDraftName(context: Context): String? {
        return getPrefs(context).getString(KEY_DRAFT_NAME, null)
    }

    fun isAmoledModeEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_AMOLED_MODE, false)
    }

    fun setAmoledModeEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_AMOLED_MODE, enabled).apply()
    }
}
