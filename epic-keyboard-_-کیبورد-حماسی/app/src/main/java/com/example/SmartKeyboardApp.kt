package com.example

import android.app.Application
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import com.example.data.AppDatabase
import com.example.data.ClipboardRepository
import com.example.data.PersonalDictionaryRepository
import com.example.nlp.PredictionEngine
import com.example.settings.KeyboardPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class SmartKeyboardApp : Application() {

    lateinit var database: AppDatabase
        private set
    lateinit var clipboardRepository: ClipboardRepository
        private set
    lateinit var personalDictionaryRepository: PersonalDictionaryRepository
        private set
    lateinit var preferences: KeyboardPreferences
        private set
    lateinit var predictionEngine: PredictionEngine
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Volatile
    private var lastCapturedClipText: String? = null

    @Volatile
    private var lastCapturedClipTimestamp: Long = -1L

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = AppDatabase.getInstance(this)
        clipboardRepository = ClipboardRepository(database.clipboardDao())
        personalDictionaryRepository = PersonalDictionaryRepository(database.personalDictionaryDao())
        preferences = KeyboardPreferences(this)
        predictionEngine = PredictionEngine(this, personalDictionaryRepository)

        setupGlobalClipboardWatcher()
    }

    private fun setupGlobalClipboardWatcher() {
        val cm = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return
        cm.addPrimaryClipChangedListener {
            captureFromClipboardManager(cm)
        }
        // Also capture any existing system clipboard item at startup if permitted
        captureFromClipboardManager(cm)
    }

    fun syncSystemClipboard() {
        val cm = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return
        captureFromClipboardManager(cm)
    }

    private fun captureFromClipboardManager(cm: ClipboardManager) {
        try {
            val clip = cm.primaryClip ?: return
            if (clip.itemCount <= 0) return

            val item = clip.getItemAt(0) ?: return
            val extracted = item.coerceToText(this)?.toString()?.trim().orEmpty()
            if (extracted.isEmpty()) return

            val timestamp = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                cm.primaryClipDescription?.timestamp ?: System.currentTimeMillis()
            } else {
                System.currentTimeMillis()
            }

            // Avoid re-inserting exact same clip event if timestamp didn't change
            if (extracted == lastCapturedClipText && timestamp == lastCapturedClipTimestamp && timestamp > 0L) {
                return
            }

            lastCapturedClipText = extracted
            lastCapturedClipTimestamp = timestamp

            appScope.launch {
                clipboardRepository.addClip(extracted, pinImmediately = false)
            }
        } catch (_: SecurityException) {
            // Android 10+ blocks background clipboard read until IME/app gains focus;
            // SmartKeyboardService.onStartInputView & onWindowShown & ClipboardDialog will sync immediately.
        } catch (_: Exception) {
        }
    }

    companion object {
        lateinit var instance: SmartKeyboardApp
            private set
    }
}
