package com.example.ui.playground

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.SmartKeyboardApp
import com.example.settings.KeyboardLanguage
import com.example.settings.KeyboardMode
import com.example.settings.KeyboardSettingsState
import com.example.ui.keyboard.KeyboardLayout

@Composable
fun PlaygroundScreen(
    settings: KeyboardSettingsState,
    onNavigateToSettings: () -> Unit,
    onOpenOnboarding: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isImeEnabled by remember { mutableStateOf(false) }
    var isImeSelected by remember { mutableStateOf(false) }

    fun refreshImeStatus() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        val enabledMethods = imm?.enabledInputMethodList ?: emptyList()
        val pkg = context.packageName
        isImeEnabled = enabledMethods.any { it.packageName == pkg }

        val currentIme = Settings.Secure.getString(context.contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD) ?: ""
        isImeSelected = currentIme.contains(pkg)
    }

    LaunchedEffect(Unit) {
        refreshImeStatus()
    }

    var testText by remember { mutableStateOf("") }
    var cursorPosition by remember { mutableStateOf(0) }
    var inAppKeyboardVisible by remember { mutableStateOf(true) }

    val beforeCursorText = remember(testText, cursorPosition) {
        if (cursorPosition in 0..testText.length) testText.substring(0, cursorPosition) else testText
    }

    val currentWord = remember(beforeCursorText) {
        val sep = charArrayOf(' ', '\n', '\t', '،', '.', ',', '!', '؟', '?', ':', ';', '؛')
        val lastIdx = beforeCursorText.lastIndexOfAny(sep)
        if (lastIdx == -1) beforeCursorText else beforeCursorText.substring(lastIdx + 1)
    }

    val contextBeforeWord = remember(beforeCursorText, currentWord) {
        if (currentWord.isNotEmpty() && beforeCursorText.length >= currentWord.length) {
            beforeCursorText.substring(0, beforeCursorText.length - currentWord.length)
        } else {
            beforeCursorText
        }
    }

    var dictionaryRevision by remember { mutableStateOf(0) }

    val suggestions = remember(currentWord, contextBeforeWord, settings.currentLanguage, dictionaryRevision) {
        SmartKeyboardApp.instance.predictionEngine.getSuggestions(
            currentWord = currentWord,
            previousText = contextBeforeWord,
            language = settings.currentLanguage
        )
    }

    val nextCharProbs = remember(currentWord, contextBeforeWord, settings.currentLanguage, dictionaryRevision) {
        SmartKeyboardApp.instance.predictionEngine.getNextCharacterProbabilities(
            currentWord = currentWord,
            previousText = contextBeforeWord,
            language = settings.currentLanguage
        )
    }

    val isUnknownWord = remember(currentWord, settings.currentLanguage, dictionaryRevision) {
        currentWord.trim().length >= 2 &&
            !SmartKeyboardApp.instance.predictionEngine.isKnownWord(currentWord, settings.currentLanguage)
    }

    var isShareSheetVisible by remember { mutableStateOf(false) }

    if (isShareSheetVisible) {
        com.example.ui.common.ShareAppBottomSheet(
            onDismissRequest = { isShareSheetVisible = false }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Scrollable Top Container: Setup Card & Test Input Field
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Branded Epic Keyboard Header with Distinctive Share Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.epic_keyboard_logo),
                    contentDescription = "Epic Keyboard Logo",
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Epic Keyboard",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "کیبورد حماسی چندزبانه و هوشمند",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Distinctive Share App Button
                com.example.ui.common.DistinctiveShareButton(
                    isCompact = false,
                    text = "اشتراک برنامه",
                    onClick = { isShareSheetVisible = true }
                )

                Spacer(modifier = Modifier.width(6.dp))

                OutlinedButton(
                    onClick = onOpenOnboarding,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Text("راهنما", fontSize = 11.sp)
                }
            }

            // IME Activation Card
            ImeActivationCard(
                isImeEnabled = isImeEnabled,
                isImeSelected = isImeSelected,
                onOpenSettings = {
                    val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                },
                onOpenPicker = {
                    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                    imm?.showInputMethodPicker()
                },
                onRefresh = { refreshImeStatus() }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Testing Text Field
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "محیط تست و تایپ کیبورد",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Row {
                            IconButton(
                                onClick = {
                                    testText = ""
                                    cursorPosition = 0
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                            }

                            IconButton(
                                onClick = {
                                    inAppKeyboardVisible = !inAppKeyboardVisible
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Keyboard,
                                    contentDescription = "Toggle In-App Keyboard",
                                    tint = if (inAppKeyboardVisible) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = testText,
                        onValueChange = {
                            testText = it
                            cursorPosition = it.length
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp),
                        placeholder = { Text("اینجا تایپ کنید تا عملکرد کیبورد، حرکه‌ها، پیشنهادات و کلیپ‌بورد را بررسی کنید...") },
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Quick Sample Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                testText = "کارجانه "
                                cursorPosition = testText.length
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("تست اشتباه", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                testText = "امیدوارم حال شما "
                                cursorPosition = testText.length
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("تست عبارت بلند", fontSize = 11.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Direct Mode Selection Bar (4 Modes)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "حالت نمایش کیبورد (انتخاب سریع):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        KeyboardMode.entries.forEach { mode ->
                            FilterChip(
                                selected = settings.mode == mode,
                                onClick = {
                                    SmartKeyboardApp.instance.preferences.updateMode(mode)
                                },
                                label = { Text(mode.titlePersian, fontSize = 11.sp, fontWeight = if (settings.mode == mode) FontWeight.Bold else FontWeight.Normal) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Language and Settings link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "زبان فعال: ${settings.currentLanguage.nativeName}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                TextButton(onClick = onNavigateToSettings) {
                    Icon(imageVector = Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("تنظیمات کامل", fontSize = 12.sp)
                }
            }
        }

        // Embedded In-App Interactive Keyboard View
        AnimatedVisibility(
            visible = inAppKeyboardVisible,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 6.dp,
                shadowElevation = 8.dp
            ) {
                KeyboardLayout(
                    settings = settings,
                    suggestions = suggestions,
                    currentWord = currentWord,
                    isUnknownWord = isUnknownWord,
                    nextCharProbs = nextCharProbs,
                    onAddPersonalWord = { newWord ->
                        coroutineScope.launch {
                            SmartKeyboardApp.instance.personalDictionaryRepository.addOrBoostWord(
                                language = settings.currentLanguage,
                                word = newWord,
                                frequencyIncrement = 15
                            )
                            SmartKeyboardApp.instance.predictionEngine.reloadPersonalDictionarySync()
                            dictionaryRevision++
                            val before = testText.substring(0, cursorPosition)
                            val after = testText.substring(cursorPosition)
                            val sep = charArrayOf(' ', '\n', '\t', '،', '.', ',', '!', '؟', '?', ':', ';', '؛')
                            val lastIdx = before.lastIndexOfAny(sep)
                            val prefix = if (lastIdx == -1) "" else before.substring(0, lastIdx + 1)
                            testText = prefix + newWord + " " + after
                            cursorPosition = (prefix + newWord + " ").length
                        }
                    },
                    onKeyOutput = { char ->
                        val before = testText.substring(0, cursorPosition)
                        val after = testText.substring(cursorPosition)
                        testText = before + char + after
                        cursorPosition += char.length
                        SmartKeyboardApp.instance.preferences.recordLanguageUsage(settings.currentLanguage, 1L)
                    },
                    onBackspace = {
                        if (cursorPosition > 0 && testText.isNotEmpty()) {
                            val before = testText.substring(0, cursorPosition - 1)
                            val after = testText.substring(cursorPosition)
                            testText = before + after
                            cursorPosition--
                        }
                    },
                    onDeleteForward = {
                        if (cursorPosition < testText.length) {
                            val before = testText.substring(0, cursorPosition)
                            val after = testText.substring(cursorPosition + 1)
                            testText = before + after
                        }
                    },
                    onEnter = {
                        val before = testText.substring(0, cursorPosition)
                        val after = testText.substring(cursorPosition)
                        testText = before + "\n" + after
                        cursorPosition++
                    },
                    onSpace = {
                        if (currentWord.length >= 2) {
                            SmartKeyboardApp.instance.predictionEngine.learnWord(
                                context = context,
                                word = currentWord,
                                language = settings.currentLanguage,
                                previousContextText = contextBeforeWord
                            )
                        }
                        val before = testText.substring(0, cursorPosition)
                        val after = testText.substring(cursorPosition)
                        testText = before + " " + after
                        cursorPosition++
                    },
                    onHalfSpace = {
                        val before = testText.substring(0, cursorPosition)
                        val after = testText.substring(cursorPosition)
                        testText = before + "\u200c" + after
                        cursorPosition++
                    },
                    onSuggestionClick = { word ->
                        val before = testText.substring(0, cursorPosition)
                        val after = testText.substring(cursorPosition)
                        val sep = charArrayOf(' ', '\n', '\t', '،', '.', ',', '!', '؟', '?', ':', ';', '؛')
                        val lastIdx = before.lastIndexOfAny(sep)
                        val prefix = if (lastIdx == -1) "" else before.substring(0, lastIdx + 1)
                        testText = prefix + word + " " + after
                        cursorPosition = (prefix + word + " ").length
                        SmartKeyboardApp.instance.predictionEngine.learnWord(
                            context = context,
                            word = word,
                            language = settings.currentLanguage,
                            previousContextText = contextBeforeWord
                        )
                    },
                    onUndo = {
                        // In playground, simple clear or reset
                        if (testText.isNotEmpty()) {
                            testText = testText.dropLast(1)
                            cursorPosition = testText.length
                        }
                    },
                    onCopy = {
                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                        cm?.setPrimaryClip(android.content.ClipData.newPlainText("Copied", testText))
                        coroutineScope.launch {
                            SmartKeyboardApp.instance.clipboardRepository.addClip(testText, false)
                        }
                    },
                    onCopyAndPin = {
                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                        cm?.setPrimaryClip(android.content.ClipData.newPlainText("Copied", testText))
                        coroutineScope.launch {
                            SmartKeyboardApp.instance.clipboardRepository.addClip(testText, true)
                        }
                    },
                    onPaste = {
                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                        val clip = cm?.primaryClip?.getItemAt(0)?.text?.toString() ?: ""
                        if (clip.isNotEmpty()) {
                            val before = testText.substring(0, cursorPosition)
                            val after = testText.substring(cursorPosition)
                            testText = before + clip + after
                            cursorPosition += clip.length
                        }
                    },
                    onCut = {
                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                        cm?.setPrimaryClip(android.content.ClipData.newPlainText("Copied", testText))
                        coroutineScope.launch {
                            SmartKeyboardApp.instance.clipboardRepository.addClip(testText, false)
                        }
                        testText = ""
                        cursorPosition = 0
                    },
                    onSelectAll = {
                        cursorPosition = testText.length
                    },
                    onHome = { cursorPosition = 0 },
                    onEnd = { cursorPosition = testText.length },
                    onHomeAll = { cursorPosition = 0 },
                    onEndAll = { cursorPosition = testText.length },
                    onToggleLanguage = {
                        SmartKeyboardApp.instance.preferences.toggleToPreviousLanguage()
                    },
                    onSelectLanguage = {
                        SmartKeyboardApp.instance.preferences.updateLanguage(it)
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
                    onOpenSettings = onNavigateToSettings
                )
            }
        }
    }
}

@Composable
private fun ImeActivationCard(
    isImeEnabled: Boolean,
    isImeSelected: Boolean,
    onOpenSettings: () -> Unit,
    onOpenPicker: () -> Unit,
    onRefresh: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isImeEnabled && isImeSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isImeEnabled && isImeSelected) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (isImeEnabled && isImeSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isImeEnabled && isImeSelected) "کیبورد فعال و آماده استفاده است"
                        else "راه‌اندازی کیبورد در سیستم اندروید",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                OutlinedButton(
                    onClick = onRefresh,
                    modifier = Modifier.height(32.dp),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("بروزرسانی", fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isImeEnabled && isImeSelected) "کیبورد هوشمند چندزبانه به عنوان کیبورد پیش‌فرض سیستم انتخاب شده است و در همه برنامه‌ها کار می‌کند."
                else "برای استفاده از این کیبورد در واتساپ، تلگرام و سایر برنامه‌ها، دو مرحله زیر را انجام دهید:",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onOpenSettings,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isImeEnabled) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primary,
                        contentColor = if (isImeEnabled) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(if (isImeEnabled) "۱. فعال شده ✓" else "۱. فعال‌سازی در تنظیمات", fontSize = 12.sp)
                }

                Button(
                    onClick = onOpenPicker,
                    modifier = Modifier.weight(1f),
                    enabled = isImeEnabled,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isImeSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primary,
                        contentColor = if (isImeSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(if (isImeSelected) "۲. انتخاب شده ✓" else "۲. انتخاب کیبورد", fontSize = 12.sp)
                }
            }
        }
    }
}
