package com.example.ui.keyboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun KeyButton(
    key: KeyModel,
    modifier: Modifier = Modifier,
    fontSizeSp: Float = 20f,
    fontFamily: FontFamily? = null,
    isRtl: Boolean = false,
    rtlMirrorBrackets: Boolean = true,
    hapticEnabled: Boolean = true,
    hapticDurationMs: Long = 20L,
    repeatInitialDelayMs: Long = 300L,
    repeatIntervalMs: Long = 50L,
    soundEnabled: Boolean = false,
    isHighlighted: Boolean = false,
    customKeyBg: Color? = null,
    customSpecialKeyBg: Color? = null,
    customTextColor: Color? = null,
    customAccent: Color? = null,
    spatialKeyId: String? = null,
    onKeyBoundsPositioned: ((com.example.nlp.KeyHitRect) -> Unit)? = null,
    onTapCharacterResolved: ((directKeyId: String?, directOutput: String, rootTapOffset: androidx.compose.ui.geometry.Offset) -> Unit)? = null,
    onClick: (output: String) -> Unit,
    onSpecialAction: ((KeyType) -> Unit)? = null
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()

    var isPressed by remember { mutableStateOf(false) }
    var showPopup by remember { mutableStateOf(false) }
    var selectedVariantIndex by remember { mutableIntStateOf(0) }
    var keyRootPosition by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }

    // RTL bracket inversion
    val effectiveOutput = remember(key.output, isRtl, rtlMirrorBrackets) {
        if (isRtl && rtlMirrorBrackets) {
            when (key.output) {
                "(" -> ")"
                ")" -> "("
                "{" -> "}"
                "}" -> "{"
                "[" -> "]"
                "]" -> "["
                "<" -> ">"
                ">" -> "<"
                "«" -> "»"
                "»" -> "«"
                else -> key.output
            }
        } else {
            key.output
        }
    }

    val effectiveLabel = remember(key.label, isRtl, rtlMirrorBrackets) {
        if (isRtl && rtlMirrorBrackets) {
            when (key.label) {
                "(" -> ")"
                ")" -> "("
                "{" -> "}"
                "}" -> "{"
                "[" -> "]"
                "]" -> "["
                "<" -> ">"
                ">" -> "<"
                "«" -> "»"
                "»" -> "«"
                else -> key.label
            }
        } else {
            key.label
        }
    }

    val allChoices = remember(effectiveOutput, key.variants) {
        listOf(effectiveOutput) + key.variants
    }

    val isSpecial = key.type != KeyType.CHAR && key.type != KeyType.HARAKAT
    val isActionKey = when (key.type) {
        KeyType.BACKSPACE,
        KeyType.SHIFT,
        KeyType.ENTER,
        KeyType.LANGUAGE_TOGGLE,
        KeyType.SYMBOLS_TOGGLE,
        KeyType.NUMPAD_TOGGLE,
        KeyType.EMOJI_TOGGLE,
        KeyType.CLIPBOARD_DIALOG,
        KeyType.UNDO -> true
        else -> false
    }
    val isBackspace = key.type == KeyType.BACKSPACE

    val baseBgColor = when {
        isHighlighted -> customAccent ?: MaterialTheme.colorScheme.primaryContainer
        isSpecial -> customSpecialKeyBg ?: MaterialTheme.colorScheme.surfaceVariant
        else -> customKeyBg ?: MaterialTheme.colorScheme.surface
    }

    val pressedBgColor = when {
        isHighlighted -> (customAccent ?: MaterialTheme.colorScheme.primary).copy(alpha = 0.8f)
        isSpecial -> (customSpecialKeyBg ?: MaterialTheme.colorScheme.secondaryContainer).copy(alpha = 0.7f)
        else -> (customAccent ?: MaterialTheme.colorScheme.primary).copy(alpha = 0.3f)
    }

    val textColor = when {
        isHighlighted -> Color.White
        isSpecial -> customTextColor?.copy(alpha = 0.9f) ?: MaterialTheme.colorScheme.onSurfaceVariant
        else -> customTextColor ?: MaterialTheme.colorScheme.onSurface
    }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else if (isHighlighted) 1.05f else 1.0f,
        label = "key_scale"
    )

    Box(
        modifier = modifier
            .fillMaxHeight()
            .padding(horizontal = 2.dp, vertical = if (key.type == KeyType.HARAKAT) 1.5.dp else 3.dp)
            .scale(scale)
            .onGloballyPositioned { coordinates ->
                val pos = coordinates.positionInRoot()
                keyRootPosition = pos
                if (spatialKeyId != null && key.type == KeyType.CHAR && onKeyBoundsPositioned != null) {
                    onKeyBoundsPositioned(
                        com.example.nlp.KeyHitRect(
                            id = spatialKeyId,
                            char = effectiveOutput,
                            left = pos.x,
                            top = pos.y,
                            right = pos.x + coordinates.size.width,
                            bottom = pos.y + coordinates.size.height
                        )
                    )
                }
            }
            .pointerInput(key, allChoices, repeatInitialDelayMs, repeatIntervalMs, hapticEnabled, hapticDurationMs, soundEnabled, spatialKeyId) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    down.consume()
                    var lastLocalPos = down.position
                    isPressed = true
                    HapticFeedbackHelper.performHaptic(context, hapticEnabled, hapticDurationMs)
                    HapticFeedbackHelper.playSound(context, soundEnabled)

                    var repeatJob: Job? = null
                    var holdJob: Job? = null

                    if (isBackspace) {
                        // Immediately delete first character
                        onSpecialAction?.invoke(KeyType.BACKSPACE)

                        // Launch repeating backspace job if held
                        repeatJob = coroutineScope.launch {
                            delay(repeatInitialDelayMs)
                            while (isActive) {
                                onSpecialAction?.invoke(KeyType.BACKSPACE)
                                HapticFeedbackHelper.performHaptic(
                                    context,
                                    hapticEnabled,
                                    (hapticDurationMs * 0.75f).toLong().coerceAtLeast(8L)
                                )
                                delay(repeatIntervalMs)
                            }
                        }
                    } else if (allChoices.size > 1) {
                        // Show elevated variants popup after brief hold: In RTL, automatically select the 2nd shape (e.g. « آ » for « ا »)
                        val initialIdx = if (isRtl && allChoices.size > 1) 1 else 0
                        selectedVariantIndex = initialIdx
                        holdJob = coroutineScope.launch {
                            delay(180L)
                            showPopup = true
                            HapticFeedbackHelper.performHaptic(context, hapticEnabled, hapticDurationMs * 2)
                        }
                    }

                    // Track movement / drag across variants (Standard left-to-right gesture tracking in all languages)
                    val startX = down.position.x
                    val stepPx = with(density) { 34.dp.toPx() }
                    val defaultVariantIdx = if (isRtl && allChoices.size > 1) 1 else 0

                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        lastLocalPos = change.position
                        if (!change.pressed) {
                            change.consume()
                            break
                        }

                        // Finger is moving while pressed: dragging right selects next variant, dragging left selects previous variant
                        if (showPopup && allChoices.size > 1) {
                            val diffX = change.position.x - startX
                            val offsetSteps = (diffX / stepPx).roundToInt()
                            val newIdx = (defaultVariantIdx + offsetSteps).coerceIn(0, allChoices.lastIndex)
                            if (newIdx != selectedVariantIndex) {
                                selectedVariantIndex = newIdx
                                HapticFeedbackHelper.performHaptic(context, hapticEnabled, 10L)
                            }
                        }
                    }

                    // Pointer released
                    holdJob?.cancel()
                    repeatJob?.cancel()
                    isPressed = false

                    if (isBackspace) {
                        // Already handled on down & repeat loop
                    } else if (showPopup && allChoices.size > 1) {
                        val chosen = allChoices[selectedVariantIndex.coerceIn(0, allChoices.lastIndex)]
                        onClick(chosen)
                        showPopup = false
                    } else {
                        // Regular tap release: true action keys trigger onSpecialAction, character & harakat keys trigger spatial resolution or onClick
                        if (isActionKey && onSpecialAction != null) {
                            onSpecialAction.invoke(key.type)
                        } else if (key.type == KeyType.CHAR && onTapCharacterResolved != null && spatialKeyId != null) {
                            val tapRoot = androidx.compose.ui.geometry.Offset(
                                x = keyRootPosition.x + lastLocalPos.x,
                                y = keyRootPosition.y + lastLocalPos.y
                            )
                            onTapCharacterResolved.invoke(spatialKeyId, effectiveOutput, tapRoot)
                        } else {
                            onClick(effectiveOutput)
                        }
                        showPopup = false
                    }
                }
            }
            .clip(RoundedCornerShape(8.dp))
            .background(if (isPressed) pressedBgColor else baseBgColor)
            .testTag("key_${key.label}"),
        contentAlignment = Alignment.Center
    ) {
        if (key.type == KeyType.HARAKAT && key.output.isNotEmpty()) {
            val mark = key.output
            val annotated = remember(mark, textColor, customAccent) {
                buildAnnotatedString {
                    withStyle(
                        SpanStyle(
                            color = textColor.copy(alpha = 0.35f),
                            fontWeight = FontWeight.Normal
                        )
                    ) {
                        append("ـ")
                    }
                    withStyle(
                        SpanStyle(
                            color = customAccent ?: textColor,
                            fontWeight = FontWeight.ExtraBold
                        )
                    ) {
                        append(mark)
                    }
                }
            }
            Text(
                text = annotated,
                fontSize = (fontSizeSp * 1.15f).sp,
                fontFamily = fontFamily,
                textAlign = TextAlign.Center,
                softWrap = false,
                style = TextStyle(
                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                    lineHeight = (fontSizeSp * 1.35f).sp
                )
            )
        } else {
            Text(
                text = effectiveLabel,
                fontSize = fontSizeSp.sp,
                fontFamily = fontFamily,
                fontWeight = if (isSpecial || isHighlighted) FontWeight.Bold else FontWeight.Medium,
                color = textColor,
                textAlign = TextAlign.Center
            )
        }

        // Small indicator if variants are available
        if (key.variants.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(3.dp)
                    .size(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(textColor.copy(alpha = 0.4f))
            )
        }
    }

    // Elevated Variant Popup (Rendered ABOVE the key so fingers don't cover it!)
    if (showPopup && allChoices.size > 1) {
        Popup(
            alignment = Alignment.TopCenter,
            offset = IntOffset(
                x = 0,
                y = with(density) { (-64).dp.roundToPx() }
            ),
            onDismissRequest = { showPopup = false },
            properties = PopupProperties(focusable = false)
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = customSpecialKeyBg ?: MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 10.dp,
                shadowElevation = 10.dp,
                modifier = Modifier
                    .shadow(12.dp, RoundedCornerShape(14.dp))
                    .clip(RoundedCornerShape(14.dp))
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Row(
                        modifier = Modifier.padding(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        allChoices.forEachIndexed { index, variant ->
                            val isSel = index == selectedVariantIndex
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 3.dp)
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isSel) (customAccent ?: MaterialTheme.colorScheme.primary)
                                        else (customKeyBg ?: MaterialTheme.colorScheme.surface)
                                    )
                                    .pointerInput(variant) {
                                        detectTapGestures(
                                            onTap = {
                                                onClick(variant)
                                                showPopup = false
                                            }
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = variant,
                                    fontSize = (fontSizeSp + 2).sp,
                                    fontFamily = fontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) Color.White
                                    else (customTextColor ?: MaterialTheme.colorScheme.onSurface)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
