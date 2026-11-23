package com.example.ui.keyboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.Dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import com.example.SmartKeyboardApp
import com.example.nlp.KeyHitRect
import com.example.nlp.SpatialTapDecoder
import com.example.settings.KeyboardLanguage
import com.example.settings.KeyboardMode
import com.example.settings.KeyboardSettingsState
import kotlin.math.roundToInt

@Composable
fun KeyboardLayout(
    settings: KeyboardSettingsState,
    suggestions: List<String>,
    currentWord: String = "",
    isUnknownWord: Boolean = false,
    nextCharProbs: Map<Char, Float> = emptyMap(),
    autoNumPadSignal: Int = 0,
    shouldAutoOpenNumPad: Boolean = false,
    onAddPersonalWord: ((String) -> Unit)? = null,
    onKeyOutput: (String) -> Unit,
    onBackspace: () -> Unit,
    onDeleteForward: () -> Unit,
    onEnter: () -> Unit,
    onSpace: () -> Unit,
    onHalfSpace: () -> Unit,
    onSuggestionClick: (String) -> Unit,
    onUndo: () -> Unit,
    onCopy: () -> Unit,
    onCopyAndPin: () -> Unit,
    onPaste: () -> Unit,
    onCut: () -> Unit,
    onSelectAll: () -> Unit,
    onHome: () -> Unit,
    onEnd: () -> Unit,
    onHomeAll: () -> Unit,
    onEndAll: () -> Unit,
    onToggleLanguage: () -> Unit,
    onSelectLanguage: (KeyboardLanguage) -> Unit,
    onToggleKeyboardMode: () -> Unit,
    onOpenSettings: () -> Unit,
    onFloatingBoundsChanged: ((android.graphics.Rect) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var isShifted by remember { mutableStateOf(false) }
    var isCapsLock by remember { mutableStateOf(false) }
    var isSymbolsMode by remember { mutableStateOf(false) }
    var isNumPadMode by remember { mutableStateOf(shouldAutoOpenNumPad) }
    var isEmojiMode by remember { mutableStateOf(false) }
    var isClipboardOpen by remember { mutableStateOf(false) }
    var isLanguageDialogOpen by remember { mutableStateOf(false) }
    var isModeDialogOpen by remember { mutableStateOf(false) }

    LaunchedEffect(autoNumPadSignal, shouldAutoOpenNumPad) {
        if (autoNumPadSignal > 0) {
            isNumPadMode = shouldAutoOpenNumPad
            if (shouldAutoOpenNumPad) {
                isSymbolsMode = false
                isEmojiMode = false
                isClipboardOpen = false
                isLanguageDialogOpen = false
            }
        }
    }

    val latestNextCharProbs by rememberUpdatedState(nextCharProbs)
    val latestCurrentWord by rememberUpdatedState(currentWord)
    val latestSmartTouch by rememberUpdatedState(settings.smartTouchCorrectionEnabled)
    val latestOnKeyOutput by rememberUpdatedState(onKeyOutput)
    val latestIsShifted by rememberUpdatedState(isShifted)
    val latestIsCapsLock by rememberUpdatedState(isCapsLock)

    val keyHitRects = remember { mutableStateMapOf<String, KeyHitRect>() }
    var matrixRootPos by remember { mutableStateOf(Offset.Zero) }

    LaunchedEffect(settings.currentLanguage, isSymbolsMode, isShifted, isCapsLock, settings.forceLtrKeyboardLayout) {
        keyHitRects.clear()
    }

    // Floating drag offset initialized from persisted settings
    var floatingOffsetX by remember(settings.floatingOffsetX) { mutableFloatStateOf(settings.floatingOffsetX) }
    var floatingOffsetY by remember(settings.floatingOffsetY) { mutableFloatStateOf(settings.floatingOffsetY) }

    val isFloating = settings.mode == KeyboardMode.FLOATING
    val isOneHanded = settings.mode == KeyboardMode.ONE_HANDED_LEFT || settings.mode == KeyboardMode.ONE_HANDED_RIGHT

    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val context = androidx.compose.ui.platform.LocalContext.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    val baseRowHeight = if (isLandscape) {
        (34 * settings.keyboardHeightScale).dp
    } else {
        (46 * settings.keyboardHeightScale).dp
    }
    val colors = settings.getColorTokens()
    val activeFont = settings.getActiveFont().toFontFamily()

    // One-handed keyboard compression ratio: (100 - compressionPercent) / 100
    // E.g. 15% empty space -> keyboard occupies 0.85f of total width, side area occupies 0.15f
    val compressionRatio = (100f - settings.oneHandedCompressionPercent) / 100f

    Box(
        modifier = modifier
            .then(
                if (isFloating) {
                    Modifier.background(Color.Transparent)
                } else {
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = settings.bottomOffsetDp.dp)
                }
            ),
        contentAlignment = if (isFloating) Alignment.BottomCenter else Alignment.TopCenter
    ) {
        // Enforce physical LTR positioning for one-handed left/right alignment so:
        // ONE_HANDED_LEFT always sticks to left edge, leaving empty/action space on right
        // ONE_HANDED_RIGHT always sticks to right edge, leaving empty/action space on left
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Row(
                modifier = Modifier
                    .then(
                        if (isFloating) {
                            Modifier
                                .offset { IntOffset(floatingOffsetX.roundToInt(), floatingOffsetY.roundToInt()) }
                                .padding(horizontal = 10.dp, vertical = 10.dp)
                                .onGloballyPositioned { coordinates ->
                                    val pos = coordinates.positionInWindow()
                                    val sz = coordinates.size
                                    onFloatingBoundsChanged?.invoke(
                                        android.graphics.Rect(
                                            pos.x.toInt(),
                                            pos.y.toInt(),
                                            (pos.x + sz.width).toInt(),
                                            (pos.y + sz.height).toInt()
                                        )
                                    )
                                }
                        } else {
                            Modifier.fillMaxWidth()
                        }
                    ),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = when (settings.mode) {
                    KeyboardMode.ONE_HANDED_LEFT -> Arrangement.Start
                    KeyboardMode.ONE_HANDED_RIGHT -> Arrangement.End
                    else -> Arrangement.Center
                }
            ) {
                // One-handed left spacer / expand buttons bar when keyboard is shifted to right
                if (settings.mode == KeyboardMode.ONE_HANDED_RIGHT) {
                    OneHandedSideBar(
                        isLeft = true,
                        colors = colors,
                        modifier = Modifier
                            .weight(1f - compressionRatio)
                            .height(baseRowHeight * (if (settings.numberRowEnabled) 5.8f else 4.8f)),
                        onExpand = {
                            SmartKeyboardApp.instance.preferences.updateMode(KeyboardMode.STANDARD)
                        },
                        onSwitchSide = {
                            SmartKeyboardApp.instance.preferences.updateMode(KeyboardMode.ONE_HANDED_LEFT)
                        }
                    )
                }

                // Keyboard Container
                Surface(
                    modifier = Modifier
                        .then(
                            when {
                                isFloating -> Modifier.fillMaxWidth(if (isLandscape) settings.floatingWidthScale * 0.72f else settings.floatingWidthScale)
                                isOneHanded -> Modifier.weight(compressionRatio)
                                isLandscape -> Modifier.fillMaxWidth(0.92f)
                                else -> Modifier.fillMaxWidth()
                            }
                        )
                        .widthIn(max = if (isLandscape) 780.dp else androidx.compose.ui.unit.Dp.Unspecified)
                        .then(
                            if (isFloating) {
                                Modifier
                                    .shadow(18.dp, RoundedCornerShape(18.dp))
                                    .border(1.dp, colors.accentColor.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
                                    .clip(RoundedCornerShape(18.dp))
                            } else Modifier
                        ),
                    color = colors.background,
                    tonalElevation = if (isFloating) 12.dp else 2.dp
                ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.background)
                ) {
                    // Floating drag pin handle
                    if (isFloating) {
                        FloatingDragHandle(
                            colors = colors,
                            onDrag = { dx, dy ->
                                floatingOffsetX = (floatingOffsetX + dx).coerceIn(-400f, 400f)
                                floatingOffsetY = (floatingOffsetY + dy).coerceIn(-1200f, 200f)
                                com.example.SmartKeyboardApp.instance.preferences.updateFloatingPosition(floatingOffsetX, floatingOffsetY)
                            },
                            onReset = {
                                floatingOffsetX = 0f
                                floatingOffsetY = 0f
                                com.example.SmartKeyboardApp.instance.preferences.updateFloatingPosition(0f, 0f)
                            },
                            onDockBottom = {
                                com.example.SmartKeyboardApp.instance.preferences.updateMode(KeyboardMode.STANDARD)
                            }
                        )
                    }

                    // Mode Selection Dialog
                    if (isModeDialogOpen) {
                        ModeSelectionDialog(
                            settings = settings,
                            onSelectMode = { mode ->
                                com.example.SmartKeyboardApp.instance.preferences.updateMode(mode)
                                isModeDialogOpen = false
                            },
                            onClose = { isModeDialogOpen = false }
                        )
                    }

                    // 1. Suggestion Bar (4 candidates + "+ Add Personal Word" when typing unknown word)
                    if (settings.suggestionsEnabled && !isNumPadMode && !isEmojiMode && !isClipboardOpen && !isLanguageDialogOpen && !isModeDialogOpen) {
                        SuggestionBar(
                            suggestions = suggestions,
                            unknownWord = if (isUnknownWord) currentWord else null,
                            onSuggestionClick = onSuggestionClick,
                            onAddPersonalWord = onAddPersonalWord
                        )
                    }

                    // 2. Action Toolbar (Undo, Copy, Paste, Del, Home/End, etc.)
                    ActionToolbar(
                        settings = settings,
                        onUndo = onUndo,
                        onCopy = onCopy,
                        onCopyAndPin = onCopyAndPin,
                        onPaste = onPaste,
                        onCut = onCut,
                        onSelectAll = onSelectAll,
                        onDeleteForward = onDeleteForward,
                        onHome = onHome,
                        onEnd = onEnd,
                        onHomeAll = onHomeAll,
                        onEndAll = onEndAll,
                        onToggleNumPad = { isNumPadMode = !isNumPadMode },
                        onOpenClipboard = { isClipboardOpen = !isClipboardOpen },
                        onToggleMode = { isModeDialogOpen = true },
                        onSelectMode = { mode ->
                            com.example.SmartKeyboardApp.instance.preferences.updateMode(mode)
                        },
                        onOpenSettings = onOpenSettings
                    )

                    // Determine key matrix layout direction:
                    // If forceLtrKeyboardLayout is true (default), standard LTR matrix is enforced on all devices (even RTL system language)
                    // If false, it reflects RTL mirrored layout.
                    val keyMatrixDirection = if (settings.forceLtrKeyboardLayout) LayoutDirection.Ltr else LayoutDirection.Rtl

                    CompositionLocalProvider(LocalLayoutDirection provides keyMatrixDirection) {
                        // 3. Conditional Subviews: NumPad, Emoji, Clipboard, Language, or Main Matrix
                        when {
                        isClipboardOpen -> {
                            ClipboardDialog(
                                onPasteText = {
                                    onKeyOutput(it)
                                    isClipboardOpen = false
                                },
                                onClose = { isClipboardOpen = false }
                            )
                        }
                        isLanguageDialogOpen -> {
                            LanguageDialog(
                                settings = settings,
                                onSelectLanguage = {
                                    onSelectLanguage(it)
                                    isLanguageDialogOpen = false
                                },
                                onClose = { isLanguageDialogOpen = false }
                            )
                        }
                        isEmojiMode -> {
                            EmojiPickerView(
                                onEmojiSelected = { onKeyOutput(it) },
                                onCloseEmoji = { isEmojiMode = false }
                            )
                        }
                        isNumPadMode -> {
                            NumPadView(
                                settings = settings,
                                onKeyClick = onKeyOutput,
                                onBackspace = onBackspace,
                                onDeleteForward = onDeleteForward,
                                onEnter = onEnter,
                                onHome = onHome,
                                onEnd = onEnd,
                                onHomeAll = onHomeAll,
                                onEndAll = onEndAll,
                                onCloseNumPad = { isNumPadMode = false }
                            )
                        }
                        else -> {
                            // 3a. Harakat diacritics bar (2 rows of 8 keys)
                            if (settings.currentLanguage.hasHarakat && !isSymbolsMode) {
                                HarakatBar(
                                    settings = settings,
                                    onKeyClick = onKeyOutput
                                )
                            }

                            // 3b. Number Row (۱۲۳۴۵۶۷۸۹۰ or 1234567890)
                            if (settings.numberRowEnabled && !isSymbolsMode) {
                                val numberRowKeys = if (settings.currentLanguage.isRtl) {
                                    KeyboardLayouts.PERSIAN_NUMBER_ROW
                                } else {
                                    KeyboardLayouts.LATIN_NUMBER_ROW
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(baseRowHeight * 0.85f)
                                        .padding(horizontal = 2.dp)
                                ) {
                                    numberRowKeys.forEach { key ->
                                        KeyButton(
                                            key = key,
                                            modifier = Modifier.weight(1f),
                                            fontSizeSp = settings.fontSizeNumbersSp,
                                            fontFamily = activeFont,
                                            isRtl = settings.currentLanguage.isRtl,
                                            rtlMirrorBrackets = settings.rtlMirrorBrackets,
                                            hapticEnabled = settings.hapticEnabled,
                                            hapticDurationMs = settings.hapticDurationMs,
                                            soundEnabled = settings.soundEnabled,
                                            customKeyBg = colors.keyBackground,
                                            customSpecialKeyBg = colors.specialKeyBackground,
                                            customTextColor = colors.textColor,
                                            customAccent = colors.accentColor,
                                            onClick = onKeyOutput
                                        )
                                    }
                                }
                            }

                            // 3c. Alphabet or Symbol rows
                            val activeRows = if (isSymbolsMode) {
                                KeyboardLayouts.getSymbolRows(settings.currentLanguage.isRtl)
                            } else {
                                KeyboardLayouts.getAlphabetRows(
                                    language = settings.currentLanguage,
                                    isShifted = isShifted,
                                    isCapsLock = isCapsLock
                                )
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .onGloballyPositioned { coords ->
                                        matrixRootPos = coords.positionInRoot()
                                    }
                                    .pointerInput(isSymbolsMode, settings.hapticEnabled, settings.hapticDurationMs, settings.soundEnabled) {
                                        if (!isSymbolsMode) {
                                            awaitEachGesture {
                                                // Only fires if tap landed in a GAP between keys (not consumed by any KeyButton)
                                                val down = awaitFirstDown(requireUnconsumed = true)
                                                var lastLocal = down.position
                                                var released = false
                                                while (true) {
                                                    val ev = awaitPointerEvent()
                                                    val ch = ev.changes.firstOrNull { it.id == down.id } ?: break
                                                    lastLocal = ch.position
                                                    if (!ch.pressed) {
                                                        released = true
                                                        break
                                                    }
                                                }
                                                if (released && latestSmartTouch) {
                                                    val rootTap = Offset(
                                                        x = matrixRootPos.x + lastLocal.x,
                                                        y = matrixRootPos.y + lastLocal.y
                                                    )
                                                    val resolved = SpatialTapDecoder.decodeTap(
                                                        tapX = rootTap.x,
                                                        tapY = rootTap.y,
                                                        directKeyId = null,
                                                        directKeyChar = null,
                                                        keyRects = keyHitRects.values.toList(),
                                                        nextCharProbs = latestNextCharProbs,
                                                        hasContextOrWord = latestCurrentWord.isNotEmpty() || latestNextCharProbs.isNotEmpty(),
                                                        smartTouchEnabled = true
                                                    )
                                                    if (!resolved.isNullOrEmpty()) {
                                                        HapticFeedbackHelper.performHaptic(context, settings.hapticEnabled, settings.hapticDurationMs)
                                                        HapticFeedbackHelper.playSound(context, settings.soundEnabled)
                                                        latestOnKeyOutput(resolved)
                                                        if (latestIsShifted && !latestIsCapsLock) {
                                                            isShifted = false
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                            ) {
                                activeRows.forEachIndexed { rowIdx, rowKeys ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(baseRowHeight)
                                            .padding(horizontal = 2.dp)
                                    ) {
                                        rowKeys.forEachIndexed { colIdx, key ->
                                            val spatialId = if (!isSymbolsMode && key.type == KeyType.CHAR) "r${rowIdx}_c${colIdx}" else null
                                            KeyButton(
                                                key = key,
                                                modifier = Modifier.weight(key.weight),
                                                fontSizeSp = settings.fontSizeLettersSp,
                                                fontFamily = activeFont,
                                                isRtl = settings.currentLanguage.isRtl,
                                                rtlMirrorBrackets = settings.rtlMirrorBrackets,
                                                hapticEnabled = settings.hapticEnabled,
                                                hapticDurationMs = settings.hapticDurationMs,
                                                repeatInitialDelayMs = settings.repeatInitialDelayMs,
                                                repeatIntervalMs = settings.repeatIntervalMs,
                                                soundEnabled = settings.soundEnabled,
                                                customKeyBg = colors.keyBackground,
                                                customSpecialKeyBg = colors.specialKeyBackground,
                                                customTextColor = colors.textColor,
                                                customAccent = colors.accentColor,
                                                spatialKeyId = spatialId,
                                                onKeyBoundsPositioned = if (spatialId != null) {
                                                    { rect -> keyHitRects[rect.id] = rect }
                                                } else null,
                                                onTapCharacterResolved = if (spatialId != null) {
                                                    { dId, dOutput, tapRoot ->
                                                        val resolved = SpatialTapDecoder.decodeTap(
                                                            tapX = tapRoot.x,
                                                            tapY = tapRoot.y,
                                                            directKeyId = dId,
                                                            directKeyChar = dOutput,
                                                            keyRects = keyHitRects.values.toList(),
                                                            nextCharProbs = latestNextCharProbs,
                                                            hasContextOrWord = latestCurrentWord.isNotEmpty() || latestNextCharProbs.isNotEmpty(),
                                                            smartTouchEnabled = latestSmartTouch
                                                        ) ?: dOutput
                                                        latestOnKeyOutput(resolved)
                                                        if (latestIsShifted && !latestIsCapsLock) {
                                                            isShifted = false
                                                        }
                                                    }
                                                } else null,
                                                onClick = { output ->
                                                    onKeyOutput(output)
                                                    if (isShifted && !isCapsLock) {
                                                        isShifted = false
                                                    }
                                                },
                                                onSpecialAction = { type ->
                                                    when (type) {
                                                        KeyType.BACKSPACE -> onBackspace()
                                                        KeyType.SHIFT -> {
                                                            if (isShifted && !isCapsLock) {
                                                                isCapsLock = true
                                                            } else if (isCapsLock) {
                                                                isCapsLock = false
                                                                isShifted = false
                                                            } else {
                                                                isShifted = true
                                                            }
                                                        }
                                                        else -> {}
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            // 3d. Bottom Row (Symbols toggle, Emoji, Language, Space, Semi-space, Enter)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(baseRowHeight)
                                    .padding(horizontal = 2.dp, vertical = 2.dp)
                            ) {
                                // ?123 / ABC Toggle
                                KeyButton(
                                    key = KeyModel(
                                        label = if (isSymbolsMode) "ABC" else if (settings.currentLanguage.isRtl) "!۱۲۳؟" else "?123",
                                        type = KeyType.SYMBOLS_TOGGLE,
                                        weight = 1.3f
                                    ),
                                    modifier = Modifier.weight(1.3f),
                                    fontSizeSp = 14f,
                                    fontFamily = activeFont,
                                    hapticEnabled = settings.hapticEnabled,
                                    hapticDurationMs = settings.hapticDurationMs,
                                    soundEnabled = settings.soundEnabled,
                                    customKeyBg = colors.keyBackground,
                                    customSpecialKeyBg = colors.specialKeyBackground,
                                    customTextColor = colors.textColor,
                                    customAccent = colors.accentColor,
                                    onClick = {},
                                    onSpecialAction = { isSymbolsMode = !isSymbolsMode }
                                )

                                // Emoji Toggle
                                if (settings.emojiEnabled) {
                                    KeyButton(
                                        key = KeyModel(label = "😀", type = KeyType.EMOJI_TOGGLE, weight = 1.0f),
                                        modifier = Modifier.weight(1.0f),
                                        fontSizeSp = 18f,
                                        fontFamily = activeFont,
                                        hapticEnabled = settings.hapticEnabled,
                                        hapticDurationMs = settings.hapticDurationMs,
                                        soundEnabled = settings.soundEnabled,
                                        customKeyBg = colors.keyBackground,
                                        customSpecialKeyBg = colors.specialKeyBackground,
                                        customTextColor = colors.textColor,
                                        customAccent = colors.accentColor,
                                        onClick = {},
                                        onSpecialAction = { isEmojiMode = true }
                                    )
                                }

                                // Language Toggle
                                if (settings.languageSwitchEnabled) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1.0f)
                                            .fillMaxHeight()
                                            .padding(horizontal = 2.dp, vertical = 3.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(colors.specialKeyBackground)
                                            .pointerInput(Unit) {
                                                detectTapGestures(
                                                    onTap = {
                                                        HapticFeedbackHelper.performHaptic(
                                                            context = context,
                                                            enabled = settings.hapticEnabled,
                                                            durationMs = settings.hapticDurationMs
                                                        )
                                                        onToggleLanguage()
                                                    },
                                                    onLongPress = {
                                                        HapticFeedbackHelper.performHaptic(
                                                            context = context,
                                                            enabled = settings.hapticEnabled,
                                                            durationMs = settings.hapticDurationMs * 2
                                                        )
                                                        isLanguageDialogOpen = true
                                                    }
                                                )
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Language,
                                            contentDescription = "Language",
                                            tint = colors.textColor,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                // Dedicated Comma Key: « ، » for RTL (Persian, Arabic, Azerbaijani Arabic) and « , » for LTR (English, Turkish, Azerbaijani Latin)
                                val commaChar = if (settings.currentLanguage.isRtl) "،" else ","
                                KeyButton(
                                    key = KeyModel(
                                        label = commaChar,
                                        output = commaChar,
                                        variants = if (settings.currentLanguage.isRtl) listOf(",", "؛", ";") else listOf("،", ";"),
                                        weight = 0.85f
                                    ),
                                    modifier = Modifier.weight(0.85f),
                                    fontSizeSp = 20f,
                                    fontFamily = activeFont,
                                    hapticEnabled = settings.hapticEnabled,
                                    hapticDurationMs = settings.hapticDurationMs,
                                    repeatInitialDelayMs = settings.repeatInitialDelayMs,
                                    repeatIntervalMs = settings.repeatIntervalMs,
                                    soundEnabled = settings.soundEnabled,
                                    customKeyBg = colors.keyBackground,
                                    customSpecialKeyBg = colors.specialKeyBackground,
                                    customTextColor = colors.textColor,
                                    customAccent = colors.accentColor,
                                    onClick = { onKeyOutput(commaChar) }
                                )

                                // Space Bar with current language label
                                Box(
                                    modifier = Modifier
                                        .weight(if (settings.currentLanguage.hasHarakat) 3.1f else 3.8f)
                                        .fillMaxHeight()
                                        .padding(horizontal = 2.dp, vertical = 3.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(colors.keyBackground)
                                        .pointerInput(Unit) {
                                            detectTapGestures(
                                                onTap = {
                                                    onSpace()
                                                }
                                            )
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = settings.currentLanguage.nativeName,
                                        fontSize = 12.sp,
                                        fontFamily = activeFont,
                                        color = colors.textColor.copy(alpha = 0.5f)
                                    )
                                }

                                // Dedicated Period / Dot « . » Key directly next to Space bar for ALL languages
                                KeyButton(
                                    key = KeyModel(
                                        label = ".",
                                        output = ".",
                                        variants = if (settings.currentLanguage.isRtl) {
                                            listOf("!", ":", "/", "…", "٫")
                                        } else {
                                            listOf("!", ":", "/", "…")
                                        },
                                        weight = 0.9f
                                    ),
                                    modifier = Modifier.weight(0.9f),
                                    fontSizeSp = 20f,
                                    fontFamily = activeFont,
                                    hapticEnabled = settings.hapticEnabled,
                                    hapticDurationMs = settings.hapticDurationMs,
                                    repeatInitialDelayMs = settings.repeatInitialDelayMs,
                                    repeatIntervalMs = settings.repeatIntervalMs,
                                    soundEnabled = settings.soundEnabled,
                                    customKeyBg = colors.keyBackground,
                                    customSpecialKeyBg = colors.specialKeyBackground,
                                    customTextColor = colors.textColor,
                                    customAccent = colors.accentColor,
                                    onClick = { onKeyOutput(".") }
                                )

                                // Semi-space (نیم‌فاصله) for Persian, Arabic and Azerbaijani Arabic
                                if (settings.currentLanguage.hasHarakat) {
                                    KeyButton(
                                        key = KeyModel(label = "نیم‌فاصله", output = "\u200c", weight = 1.15f),
                                        modifier = Modifier.weight(1.15f),
                                        fontSizeSp = 11.sp.value,
                                        fontFamily = activeFont,
                                        hapticEnabled = settings.hapticEnabled,
                                        hapticDurationMs = settings.hapticDurationMs,
                                        repeatInitialDelayMs = settings.repeatInitialDelayMs,
                                        repeatIntervalMs = settings.repeatIntervalMs,
                                        soundEnabled = settings.soundEnabled,
                                        customKeyBg = colors.keyBackground,
                                        customSpecialKeyBg = colors.specialKeyBackground,
                                        customTextColor = colors.textColor,
                                        customAccent = colors.accentColor,
                                        onClick = { onHalfSpace() }
                                    )
                                }

                                // Enter Key
                                KeyButton(
                                    key = KeyModel(label = "↵", type = KeyType.ENTER, weight = 1.3f),
                                    modifier = Modifier.weight(1.3f),
                                    fontSizeSp = 20f,
                                    fontFamily = activeFont,
                                    isHighlighted = true,
                                    hapticEnabled = settings.hapticEnabled,
                                    hapticDurationMs = settings.hapticDurationMs,
                                    soundEnabled = settings.soundEnabled,
                                    customKeyBg = colors.keyBackground,
                                    customSpecialKeyBg = colors.specialKeyBackground,
                                    customTextColor = colors.textColor,
                                    customAccent = colors.accentColor,
                                    onClick = {},
                                    onSpecialAction = { onEnter() }
                                )
                            }
                        }
                    }
                }
            }
            }

            // One-handed right spacer / expand button when keyboard is shifted to left
            if (settings.mode == KeyboardMode.ONE_HANDED_LEFT) {
                OneHandedSideBar(
                    isLeft = false,
                    colors = colors,
                    modifier = Modifier
                        .weight(1f - compressionRatio)
                        .height(baseRowHeight * (if (settings.numberRowEnabled) 5.8f else 4.8f)),
                    onExpand = {
                        SmartKeyboardApp.instance.preferences.updateMode(KeyboardMode.STANDARD)
                    },
                    onSwitchSide = {
                        SmartKeyboardApp.instance.preferences.updateMode(KeyboardMode.ONE_HANDED_RIGHT)
                    }
                )
            }
        }
        }
    }
}

@Composable
private fun FloatingDragHandle(
    colors: com.example.settings.KeyboardColorTokens,
    onDrag: (Float, Float) -> Unit,
    onReset: () -> Unit,
    onDockBottom: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp)
            .background(colors.specialKeyBackground.copy(alpha = 0.92f))
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onDrag(dragAmount.x, dragAmount.y)
                }
            }
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.PushPin,
                contentDescription = "Pin Handle",
                tint = colors.accentColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.DragHandle,
                contentDescription = "Drag to Move",
                tint = colors.textColor.copy(alpha = 0.7f),
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "کیبورد شناور معلق روی همه برنامه‌ها",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textColor.copy(alpha = 0.85f),
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onReset, modifier = Modifier.size(28.dp)) {
                Icon(
                    imageVector = Icons.Default.RestartAlt,
                    contentDescription = "مرکز صفحه",
                    tint = colors.textColor.copy(alpha = 0.75f),
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            IconButton(onClick = onDockBottom, modifier = Modifier.size(28.dp)) {
                Icon(
                    imageVector = Icons.Default.Fullscreen,
                    contentDescription = "اتصال به پایین صفحه",
                    tint = colors.accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun OneHandedSideBar(
    isLeft: Boolean,
    colors: com.example.settings.KeyboardColorTokens,
    modifier: Modifier = Modifier,
    onExpand: () -> Unit,
    onSwitchSide: () -> Unit
) {
    Column(
        modifier = modifier
            .background(colors.background.copy(alpha = 0.5f))
            .padding(vertical = 12.dp, horizontal = 2.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(
            onClick = onExpand,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(colors.specialKeyBackground)
        ) {
            Icon(
                imageVector = Icons.Default.Fullscreen,
                contentDescription = "Expand Full Width",
                tint = colors.accentColor
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        IconButton(
            onClick = onSwitchSide,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(colors.specialKeyBackground)
        ) {
            Icon(
                imageVector = if (isLeft) Icons.AutoMirrored.Filled.ArrowForward else Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Switch Side",
                tint = colors.textColor
            )
        }
    }
}
