package com.example.ime

import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.inputmethodservice.InputMethodService
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.example.SmartKeyboardApp
import com.example.settings.KeyboardMode
import com.example.ui.keyboard.KeyboardLayout
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SmartKeyboardService : InputMethodService() {

    private val lifecycleHelper = ImeLifecycleHelper()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var inputAdapter: InputConnectionAdapter

    private var currentWord by mutableStateOf("")
    private var contextText by mutableStateOf("")
    private var suggestions by mutableStateOf<List<String>>(emptyList())
    private var nextCharProbs by mutableStateOf<Map<Char, Float>>(emptyMap())
    private var isUnknownWord by mutableStateOf(false)
    private var autoNumPadSignal by mutableIntStateOf(0)
    private var shouldAutoOpenNumPad by mutableStateOf(false)
    private var floatingCardRect = Rect()

    override fun onCreate() {
        super.onCreate()
        lifecycleHelper.onCreate()

        inputAdapter = InputConnectionAdapter(
            context = this,
            inputConnectionProvider = { currentInputConnection },
            onWordChanged = { word, ctx ->
                currentWord = word
                contextText = ctx
                updateSuggestions()
            }
        )

        // Keep predictions automatically synchronized when user edits Personal Dictionary or imports CSV
        serviceScope.launch {
            SmartKeyboardApp.instance.personalDictionaryRepository.allWordsFlow.collectLatest {
                SmartKeyboardApp.instance.predictionEngine.reloadPersonalDictionarySync()
                updateSuggestions()
            }
        }
    }

    override fun onCreateInputView(): View {
        lifecycleHelper.onStart()
        lifecycleHelper.onResume()

        val composeView = ComposeView(this).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool)
            setContent {
                val settings by SmartKeyboardApp.instance.preferences.settingsState.collectAsState()

                // Dynamically update window attributes when mode changes
                LaunchedEffect(settings.mode) {
                    applyWindowMode(settings.mode)
                }

                MyApplicationTheme(darkTheme = settings.darkTheme ?: androidx.compose.foundation.isSystemInDarkTheme()) {
                    KeyboardLayout(
                        settings = settings,
                        suggestions = suggestions,
                        currentWord = currentWord,
                        isUnknownWord = isUnknownWord,
                        nextCharProbs = nextCharProbs,
                        autoNumPadSignal = autoNumPadSignal,
                        shouldAutoOpenNumPad = shouldAutoOpenNumPad,
                        modifier = if (settings.mode == KeyboardMode.FLOATING) Modifier.fillMaxSize() else Modifier.fillMaxWidth(),
                        onFloatingBoundsChanged = { rect ->
                            updateFloatingBounds(rect)
                        },
                        onAddPersonalWord = { word ->
                            serviceScope.launch {
                                SmartKeyboardApp.instance.personalDictionaryRepository.addOrBoostWord(
                                    language = settings.currentLanguage,
                                    word = word,
                                    frequencyIncrement = 15
                                )
                                SmartKeyboardApp.instance.predictionEngine.reloadPersonalDictionarySync()
                                withContext(Dispatchers.Main) {
                                    inputAdapter.commitSuggestion(word, saveToPersonal = false)
                                    updateSuggestions()
                                }
                            }
                        },
                        onKeyOutput = {
                            inputAdapter.commitText(it)
                            SmartKeyboardApp.instance.preferences.recordLanguageUsage(settings.currentLanguage, 1L)
                        },
                        onBackspace = { inputAdapter.deleteBackward() },
                        onDeleteForward = { inputAdapter.deleteForward() },
                        onEnter = { inputAdapter.sendEnter() },
                        onSpace = { inputAdapter.commitText(" ") },
                        onHalfSpace = { inputAdapter.commitText("\u200c") },
                        onSuggestionClick = { inputAdapter.commitSuggestion(it) },
                        onUndo = { inputAdapter.undo() },
                        onCopy = { inputAdapter.copy(pinImmediately = false) },
                        onCopyAndPin = { inputAdapter.copy(pinImmediately = true) },
                        onPaste = { inputAdapter.paste() },
                        onCut = { inputAdapter.cut() },
                        onSelectAll = { inputAdapter.selectAll() },
                        onHome = { inputAdapter.moveHome() },
                        onEnd = { inputAdapter.moveEnd() },
                        onHomeAll = { inputAdapter.moveHomeAll() },
                        onEndAll = { inputAdapter.moveEndAll() },
                        onToggleLanguage = {
                            SmartKeyboardApp.instance.preferences.toggleToPreviousLanguage()
                            updateSuggestions()
                        },
                        onSelectLanguage = {
                            SmartKeyboardApp.instance.preferences.updateLanguage(it)
                            updateSuggestions()
                        },
                        onToggleKeyboardMode = {
                            val nextMode = when (settings.mode) {
                                KeyboardMode.STANDARD -> KeyboardMode.ONE_HANDED_RIGHT
                                KeyboardMode.ONE_HANDED_RIGHT -> KeyboardMode.ONE_HANDED_LEFT
                                KeyboardMode.ONE_HANDED_LEFT -> KeyboardMode.FLOATING
                                KeyboardMode.FLOATING -> KeyboardMode.STANDARD
                            }
                            SmartKeyboardApp.instance.preferences.updateMode(nextMode)
                        },
                        onOpenSettings = {
                            val intent = android.content.Intent(this@SmartKeyboardService, com.example.MainActivity::class.java).apply {
                                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            startActivity(intent)
                        }
                    )
                }
            }
        }

        // CRITICAL FIX: Bind lifecycle owners to decor view to prevent IllegalStateException
        window?.window?.let { win ->
            win.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            win.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            lifecycleHelper.bindToView(win.decorView)
        }
        lifecycleHelper.bindToView(composeView)

        return composeView
    }

    fun updateFloatingBounds(rect: Rect) {
        if (floatingCardRect != rect) {
            floatingCardRect = rect
            window?.window?.decorView?.post {
                window?.window?.decorView?.requestApplyInsets()
            }
        }
    }

    private fun applyWindowMode(mode: KeyboardMode) {
        val win = window?.window ?: return
        if (mode == KeyboardMode.FLOATING) {
            win.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            )
            win.setGravity(Gravity.TOP or Gravity.START)
            win.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)
            win.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
            win.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)
            win.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            win.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        } else {
            win.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
            win.setGravity(Gravity.BOTTOM)
            win.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
            win.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)
            win.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            win.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        win.decorView.post {
            win.decorView.requestApplyInsets()
        }
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        lifecycleHelper.onResume()

        // Sync system clipboard whenever IME becomes active (captures copies from browsers/messengers on Android 10+)
        SmartKeyboardApp.instance.syncSystemClipboard()

        // Auto-switch to NumPad when focused field is numeric-only (Number / Phone / DateTime)
        val inputClass = (info?.inputType ?: 0) and InputType.TYPE_MASK_CLASS
        val isNumericField = inputClass == InputType.TYPE_CLASS_NUMBER ||
            inputClass == InputType.TYPE_CLASS_PHONE ||
            inputClass == InputType.TYPE_CLASS_DATETIME
        shouldAutoOpenNumPad = isNumericField
        autoNumPadSignal++

        inputAdapter.notifyContextChanged()

        val settings = SmartKeyboardApp.instance.preferences.settingsState.value
        applyWindowMode(settings.mode)
    }

    override fun onWindowShown() {
        super.onWindowShown()
        SmartKeyboardApp.instance.syncSystemClipboard()
    }

    override fun onComputeInsets(outInsets: Insets) {
        super.onComputeInsets(outInsets)
        val mode = SmartKeyboardApp.instance.preferences.settingsState.value.mode
        if (mode == KeyboardMode.FLOATING) {
            val dm = resources.displayMetrics
            val screenHeight = dm.heightPixels
            val screenWidth = dm.widthPixels

            outInsets.contentTopInsets = screenHeight
            outInsets.visibleTopInsets = screenHeight

            outInsets.touchableInsets = Insets.TOUCHABLE_INSETS_REGION
            if (!floatingCardRect.isEmpty) {
                outInsets.touchableRegion.set(floatingCardRect)
            } else {
                val defaultTop = (screenHeight * 0.45f).toInt()
                outInsets.touchableRegion.set(0, defaultTop, screenWidth, screenHeight)
            }
        }
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        lifecycleHelper.onPause()
    }

    override fun onUpdateSelection(
        oldSelStart: Int,
        oldSelEnd: Int,
        newSelStart: Int,
        newSelEnd: Int,
        candidatesStart: Int,
        candidatesEnd: Int
    ) {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd)
        inputAdapter.notifyContextChanged()
    }

    private fun updateSuggestions() {
        val currentLang = SmartKeyboardApp.instance.preferences.settingsState.value.currentLanguage
        val engine = SmartKeyboardApp.instance.predictionEngine
        suggestions = engine.getSuggestions(
            currentWord = currentWord,
            previousText = contextText,
            language = currentLang
        )
        nextCharProbs = engine.getNextCharacterProbabilities(
            currentWord = currentWord,
            previousText = contextText,
            language = currentLang
        )
        isUnknownWord = currentWord.trim().length >= 2 && !engine.isKnownWord(currentWord, currentLang)
    }

    override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
        super.onConfigurationChanged(newConfig)
        val settings = SmartKeyboardApp.instance.preferences.settingsState.value
        applyWindowMode(settings.mode)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        lifecycleHelper.onStop()
        lifecycleHelper.onDestroy()
    }
}
