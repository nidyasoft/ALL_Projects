package com.example.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class KeyboardSettingsState(
    val currentLanguage: KeyboardLanguage = KeyboardLanguage.PERSIAN,
    val previousLanguage: KeyboardLanguage = KeyboardLanguage.ENGLISH,
    val languageOrder: List<KeyboardLanguage> = listOf(
        KeyboardLanguage.PERSIAN,
        KeyboardLanguage.ENGLISH,
        KeyboardLanguage.TURKISH,
        KeyboardLanguage.AZERBAIJANI_LATIN,
        KeyboardLanguage.AZERBAIJANI_ARABIC,
        KeyboardLanguage.ARABIC
    ),
    val mode: KeyboardMode = KeyboardMode.STANDARD,
    val theme: KeyboardTheme = KeyboardTheme.DARK_CYBER_INDIGO,
    // Custom Palette Colors
    val customBgColor: Long = 0xFF0F0F1A,
    val customKeyColor: Long = 0xFF1E1B4B,
    val customSpecialKeyColor: Long = 0xFF2E2A72,
    val customTextColor: Long = 0xFFF1F5F9,
    val customAccentColor: Long = 0xFF38BDF8,
    // Fonts per language
    val fontForPersian: KeyboardFont = KeyboardFont.VAZIRMATN,
    val fontForEnglish: KeyboardFont = KeyboardFont.POPPINS,
    val fontForTurkish: KeyboardFont = KeyboardFont.POPPINS,
    val fontForAzLatin: KeyboardFont = KeyboardFont.VAZIRMATN,
    val fontForAzArabic: KeyboardFont = KeyboardFont.VAZIRMATN,
    val fontForArabic: KeyboardFont = KeyboardFont.VAZIRMATN,
    // Independent Font Sizes (14sp to 40sp for keys, 10sp to 35sp for toolbar)
    val fontSizeLettersSp: Float = 21f,
    val fontSizeNumbersSp: Float = 19f,
    val fontSizeHarakatSp: Float = 22f,
    val fontSizeToolbarSp: Float = 13f,
    // Dimensions
    val keyboardHeightScale: Float = 1.0f, // 0.75f to 1.4f
    val bottomOffsetDp: Int = 0, // 0 to 64dp
    val oneHandedCompressionPercent: Int = 15, // 10, 15, 20, 30
    val floatingWidthScale: Float = 0.88f,
    val floatingHeightScale: Float = 0.88f,
    // Haptics & Sound
    val hapticEnabled: Boolean = true,
    val hapticDurationMs: Long = 20L, // 5 to 60ms
    val soundEnabled: Boolean = false,
    // Suggestions & Prediction
    val suggestionsEnabled: Boolean = true,
    val clipboardEnabled: Boolean = true,
    val emojiEnabled: Boolean = true,
    val undoEnabled: Boolean = true,
    val languageSwitchEnabled: Boolean = true,
    // Diacritics Rows
    val harakatTopRowEnabled: Boolean = true,
    val harakatBottomRowEnabled: Boolean = true,
    val numberRowEnabled: Boolean = true,
    // Action Keys
    val copyKeyEnabled: Boolean = true,
    val copyPinKeyEnabled: Boolean = true,
    val pasteKeyEnabled: Boolean = true,
    val cutKeyEnabled: Boolean = true,
    val selectAllKeyEnabled: Boolean = true,
    val delKeyEnabled: Boolean = true,
    val homeEndKeysEnabled: Boolean = true,
    val numPadKeyEnabled: Boolean = true,
    val repeatInitialDelayMs: Long = 300L, // 150ms to 600ms
    val repeatIntervalMs: Long = 50L, // 25ms to 100ms
    val floatingOffsetX: Float = 0f,
    val floatingOffsetY: Float = 0f,
    val rtlMirrorBrackets: Boolean = true,
    val forceLtrKeyboardLayout: Boolean = true,
    val smartTouchCorrectionEnabled: Boolean = true,
    val onboardingCompleted: Boolean = false,
    val darkTheme: Boolean? = null
) {
    fun getActiveFont(): KeyboardFont {
        return when (currentLanguage) {
            KeyboardLanguage.PERSIAN -> fontForPersian
            KeyboardLanguage.ENGLISH -> fontForEnglish
            KeyboardLanguage.TURKISH -> fontForTurkish
            KeyboardLanguage.AZERBAIJANI_LATIN -> fontForAzLatin
            KeyboardLanguage.AZERBAIJANI_ARABIC -> fontForAzArabic
            KeyboardLanguage.ARABIC -> fontForArabic
        }
    }

    fun getColorTokens(): KeyboardColorTokens {
        return if (theme == KeyboardTheme.CUSTOM) {
            KeyboardColorTokens(
                background = Color(customBgColor),
                keyBackground = Color(customKeyColor),
                specialKeyBackground = Color(customSpecialKeyColor),
                textColor = Color(customTextColor),
                accentColor = Color(customAccentColor)
            )
        } else {
            KeyboardColorTokens(
                background = Color(theme.defaultBg),
                keyBackground = Color(theme.defaultKeyBg),
                specialKeyBackground = Color(theme.defaultSpecialKeyBg),
                textColor = Color(theme.defaultTextColor),
                accentColor = Color(theme.defaultAccent)
            )
        }
    }
}

class KeyboardPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("smart_keyboard_prefs", Context.MODE_PRIVATE)

    private val _settingsState = MutableStateFlow(loadSettings())
    val settingsState: StateFlow<KeyboardSettingsState> = _settingsState.asStateFlow()

    private fun loadSettings(): KeyboardSettingsState {
        val currentLangId = prefs.getString("current_language", KeyboardLanguage.PERSIAN.id) ?: KeyboardLanguage.PERSIAN.id
        val prevLangId = prefs.getString("previous_language", KeyboardLanguage.ENGLISH.id) ?: KeyboardLanguage.ENGLISH.id
        val modeId = prefs.getString("keyboard_mode", KeyboardMode.STANDARD.id) ?: KeyboardMode.STANDARD.id
        val themeId = prefs.getString("keyboard_theme", KeyboardTheme.DARK_CYBER_INDIGO.id) ?: KeyboardTheme.DARK_CYBER_INDIGO.id

        val orderStr = prefs.getString("language_order", null)
        val order = if (!orderStr.isNullOrEmpty()) {
            orderStr.split(",").map { KeyboardLanguage.fromId(it) }.distinct()
        } else {
            KeyboardLanguage.entries.sortedByDescending { lang ->
                val defCount = when (lang) {
                    KeyboardLanguage.PERSIAN -> 100L
                    KeyboardLanguage.ENGLISH -> 80L
                    KeyboardLanguage.TURKISH -> 60L
                    KeyboardLanguage.AZERBAIJANI_ARABIC -> 50L
                    KeyboardLanguage.AZERBAIJANI_LATIN -> 40L
                    KeyboardLanguage.ARABIC -> 30L
                }
                prefs.getLong("lang_usage_${lang.id}", defCount)
            }
        }

        return KeyboardSettingsState(
            currentLanguage = KeyboardLanguage.fromId(currentLangId),
            previousLanguage = KeyboardLanguage.fromId(prevLangId),
            languageOrder = order,
            mode = KeyboardMode.fromId(modeId),
            theme = KeyboardTheme.fromId(themeId),
            customBgColor = prefs.getLong("custom_bg_color", 0xFF0F0F1AL),
            customKeyColor = prefs.getLong("custom_key_color", 0xFF1E1B4BL),
            customSpecialKeyColor = prefs.getLong("custom_special_key_color", 0xFF2E2A72L),
            customTextColor = prefs.getLong("custom_text_color", 0xFFF1F5F9L),
            customAccentColor = prefs.getLong("custom_accent_color", 0xFF38BDF8L),
            fontForPersian = KeyboardFont.fromId(prefs.getString("font_fa", KeyboardFont.VAZIRMATN.id) ?: KeyboardFont.VAZIRMATN.id),
            fontForEnglish = KeyboardFont.fromId(prefs.getString("font_en", KeyboardFont.POPPINS.id) ?: KeyboardFont.POPPINS.id),
            fontForTurkish = KeyboardFont.fromId(prefs.getString("font_tr", KeyboardFont.POPPINS.id) ?: KeyboardFont.POPPINS.id),
            fontForAzLatin = KeyboardFont.fromId(prefs.getString("font_az_latn", KeyboardFont.VAZIRMATN.id) ?: KeyboardFont.VAZIRMATN.id),
            fontForAzArabic = KeyboardFont.fromId(prefs.getString("font_az_arab", KeyboardFont.VAZIRMATN.id) ?: KeyboardFont.VAZIRMATN.id),
            fontForArabic = KeyboardFont.fromId(prefs.getString("font_ar", KeyboardFont.VAZIRMATN.id) ?: KeyboardFont.VAZIRMATN.id),
            fontSizeLettersSp = prefs.getFloat("font_size_letters", 21f).coerceIn(14f, 40f),
            fontSizeNumbersSp = prefs.getFloat("font_size_numbers", 19f).coerceIn(14f, 40f),
            fontSizeHarakatSp = prefs.getFloat("font_size_harakat", 22f).coerceIn(14f, 40f),
            fontSizeToolbarSp = prefs.getFloat("font_size_toolbar", 13f).coerceIn(10f, 35f),
            keyboardHeightScale = prefs.getFloat("keyboard_height_scale", 1.0f).coerceIn(0.75f, 1.4f),
            bottomOffsetDp = prefs.getInt("bottom_offset_dp", 0).coerceIn(0, 64),
            oneHandedCompressionPercent = prefs.getInt("one_handed_compression_percent", 15),
            floatingWidthScale = prefs.getFloat("floating_width_scale", 0.88f),
            floatingHeightScale = prefs.getFloat("floating_height_scale", 0.88f),
            hapticEnabled = prefs.getBoolean("haptic_enabled", true),
            hapticDurationMs = prefs.getLong("haptic_duration_ms", 20L).coerceIn(5L, 60L),
            soundEnabled = prefs.getBoolean("sound_enabled", false),
            suggestionsEnabled = prefs.getBoolean("suggestions_enabled", true),
            clipboardEnabled = prefs.getBoolean("clipboard_enabled", true),
            emojiEnabled = prefs.getBoolean("emoji_enabled", true),
            undoEnabled = prefs.getBoolean("undo_enabled", true),
            languageSwitchEnabled = prefs.getBoolean("language_switch_enabled", true),
            harakatTopRowEnabled = prefs.getBoolean("harakat_top_row_enabled", true),
            harakatBottomRowEnabled = prefs.getBoolean("harakat_bottom_row_enabled", true),
            numberRowEnabled = prefs.getBoolean("number_row_enabled", true),
            copyKeyEnabled = prefs.getBoolean("copy_key_enabled", true),
            copyPinKeyEnabled = prefs.getBoolean("copy_pin_key_enabled", true),
            pasteKeyEnabled = prefs.getBoolean("paste_key_enabled", true),
            cutKeyEnabled = prefs.getBoolean("cut_key_enabled", true),
            selectAllKeyEnabled = prefs.getBoolean("select_all_key_enabled", true),
            delKeyEnabled = prefs.getBoolean("del_key_enabled", true),
            homeEndKeysEnabled = prefs.getBoolean("home_end_keys_enabled", true),
            numPadKeyEnabled = prefs.getBoolean("numpad_key_enabled", true),
            repeatInitialDelayMs = prefs.getLong("repeat_initial_delay_ms", 300L).coerceIn(150L, 600L),
            repeatIntervalMs = prefs.getLong("repeat_interval_ms", 50L).coerceIn(25L, 100L),
            floatingOffsetX = prefs.getFloat("floating_offset_x", 0f),
            floatingOffsetY = prefs.getFloat("floating_offset_y", 0f),
            rtlMirrorBrackets = prefs.getBoolean("rtl_mirror_brackets", true),
            forceLtrKeyboardLayout = prefs.getBoolean("force_ltr_keyboard_layout", true),
            smartTouchCorrectionEnabled = prefs.getBoolean("smart_touch_correction_enabled", true),
            onboardingCompleted = prefs.getBoolean("onboarding_completed", false),
            darkTheme = if (prefs.contains("dark_theme")) prefs.getBoolean("dark_theme", false) else null
        )
    }

    fun updateFloatingPosition(x: Float, y: Float) {
        prefs.edit()
            .putFloat("floating_offset_x", x)
            .putFloat("floating_offset_y", y)
            .apply()
        _settingsState.value = _settingsState.value.copy(
            floatingOffsetX = x,
            floatingOffsetY = y
        )
    }

    fun updateKeyRepeat(initialDelayMs: Long, intervalMs: Long) {
        val init = initialDelayMs.coerceIn(150L, 600L)
        val inter = intervalMs.coerceIn(25L, 100L)
        prefs.edit()
            .putLong("repeat_initial_delay_ms", init)
            .putLong("repeat_interval_ms", inter)
            .apply()
        _settingsState.value = _settingsState.value.copy(
            repeatInitialDelayMs = init,
            repeatIntervalMs = inter
        )
    }

    fun getLanguageUsageCount(lang: KeyboardLanguage): Long {
        val defaultCount = when (lang) {
            KeyboardLanguage.PERSIAN -> 100L
            KeyboardLanguage.ENGLISH -> 80L
            KeyboardLanguage.TURKISH -> 60L
            KeyboardLanguage.AZERBAIJANI_ARABIC -> 50L
            KeyboardLanguage.AZERBAIJANI_LATIN -> 40L
            KeyboardLanguage.ARABIC -> 30L
        }
        return prefs.getLong("lang_usage_${lang.id}", defaultCount)
    }

    fun getLanguagesByUsage(): List<KeyboardLanguage> {
        return KeyboardLanguage.entries.sortedByDescending { getLanguageUsageCount(it) }
    }

    fun recordLanguageUsage(lang: KeyboardLanguage, increment: Long = 1L) {
        val currentCount = getLanguageUsageCount(lang)
        val newCount = currentCount + increment
        prefs.edit().putLong("lang_usage_${lang.id}", newCount).apply()

        val sortedOrder = getLanguagesByUsage()
        _settingsState.value = _settingsState.value.copy(languageOrder = sortedOrder)
    }

    fun updateLanguage(newLang: KeyboardLanguage) {
        val current = _settingsState.value.currentLanguage
        recordLanguageUsage(newLang, 5L) // Boost selected language
        if (current == newLang) return

        val prev = current
        val sortedOrder = getLanguagesByUsage()

        prefs.edit()
            .putString("current_language", newLang.id)
            .putString("previous_language", prev.id)
            .putString("language_order", sortedOrder.joinToString(",") { it.id })
            .apply()

        _settingsState.value = _settingsState.value.copy(
            currentLanguage = newLang,
            previousLanguage = prev,
            languageOrder = sortedOrder
        )
    }

    fun toggleToPreviousLanguage() {
        val current = _settingsState.value.currentLanguage
        val prev = _settingsState.value.previousLanguage
        val target = if (prev != current) prev else {
            KeyboardLanguage.entries.firstOrNull { it != current } ?: KeyboardLanguage.ENGLISH
        }
        updateLanguage(target)
    }

    fun updateMode(mode: KeyboardMode) {
        prefs.edit().putString("keyboard_mode", mode.id).apply()
        _settingsState.value = _settingsState.value.copy(mode = mode)
    }

    fun updateTheme(theme: KeyboardTheme) {
        prefs.edit().putString("keyboard_theme", theme.id).apply()
        _settingsState.value = _settingsState.value.copy(theme = theme)
    }

    fun updateCustomColors(bg: Long, keyBg: Long, specialKeyBg: Long, text: Long, accent: Long) {
        prefs.edit()
            .putLong("custom_bg_color", bg)
            .putLong("custom_key_color", keyBg)
            .putLong("custom_special_key_color", specialKeyBg)
            .putLong("custom_text_color", text)
            .putLong("custom_accent_color", accent)
            .putString("keyboard_theme", KeyboardTheme.CUSTOM.id)
            .apply()

        _settingsState.value = _settingsState.value.copy(
            theme = KeyboardTheme.CUSTOM,
            customBgColor = bg,
            customKeyColor = keyBg,
            customSpecialKeyColor = specialKeyBg,
            customTextColor = text,
            customAccentColor = accent
        )
    }

    fun updateFontForLanguage(language: KeyboardLanguage, font: KeyboardFont) {
        val key = "font_${language.id}"
        prefs.edit().putString(key, font.id).apply()

        _settingsState.value = when (language) {
            KeyboardLanguage.PERSIAN -> _settingsState.value.copy(fontForPersian = font)
            KeyboardLanguage.ENGLISH -> _settingsState.value.copy(fontForEnglish = font)
            KeyboardLanguage.TURKISH -> _settingsState.value.copy(fontForTurkish = font)
            KeyboardLanguage.AZERBAIJANI_LATIN -> _settingsState.value.copy(fontForAzLatin = font)
            KeyboardLanguage.AZERBAIJANI_ARABIC -> _settingsState.value.copy(fontForAzArabic = font)
            KeyboardLanguage.ARABIC -> _settingsState.value.copy(fontForArabic = font)
        }
    }

    fun updateFontSizes(lettersSp: Float, numbersSp: Float, harakatSp: Float, toolbarSp: Float) {
        val l = lettersSp.coerceIn(14f, 40f)
        val n = numbersSp.coerceIn(14f, 40f)
        val h = harakatSp.coerceIn(14f, 40f)
        val t = toolbarSp.coerceIn(10f, 35f)
        prefs.edit()
            .putFloat("font_size_letters", l)
            .putFloat("font_size_numbers", n)
            .putFloat("font_size_harakat", h)
            .putFloat("font_size_toolbar", t)
            .apply()
        _settingsState.value = _settingsState.value.copy(
            fontSizeLettersSp = l,
            fontSizeNumbersSp = n,
            fontSizeHarakatSp = h,
            fontSizeToolbarSp = t
        )
    }

    fun updateKeyboardDimensions(heightScale: Float, bottomOffsetDp: Int) {
        val hs = heightScale.coerceIn(0.75f, 1.4f)
        val bo = bottomOffsetDp.coerceIn(0, 64)
        prefs.edit()
            .putFloat("keyboard_height_scale", hs)
            .putInt("bottom_offset_dp", bo)
            .apply()
        _settingsState.value = _settingsState.value.copy(
            keyboardHeightScale = hs,
            bottomOffsetDp = bo
        )
    }

    fun updateOneHandedCompression(percent: Int) {
        val valid = if (percent in listOf(10, 15, 20, 30)) percent else 15
        prefs.edit().putInt("one_handed_compression_percent", valid).apply()
        _settingsState.value = _settingsState.value.copy(oneHandedCompressionPercent = valid)
    }

    fun updateHaptic(enabled: Boolean, durationMs: Long) {
        val d = durationMs.coerceIn(5L, 60L)
        prefs.edit()
            .putBoolean("haptic_enabled", enabled)
            .putLong("haptic_duration_ms", d)
            .apply()
        _settingsState.value = _settingsState.value.copy(
            hapticEnabled = enabled,
            hapticDurationMs = d
        )
    }

    fun updateSound(enabled: Boolean) {
        prefs.edit().putBoolean("sound_enabled", enabled).apply()
        _settingsState.value = _settingsState.value.copy(soundEnabled = enabled)
    }

    fun updateForceLtrKeyboardLayout(enabled: Boolean) {
        prefs.edit().putBoolean("force_ltr_keyboard_layout", enabled).apply()
        _settingsState.value = _settingsState.value.copy(forceLtrKeyboardLayout = enabled)
    }

    fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean("onboarding_completed", completed).apply()
        _settingsState.value = _settingsState.value.copy(onboardingCompleted = completed)
    }

    fun isOnboardingCompleted(): Boolean {
        return prefs.getBoolean("onboarding_completed", false)
    }

    fun updateFeatureToggle(
        key: String,
        enabled: Boolean,
        copyBlock: KeyboardSettingsState.(Boolean) -> KeyboardSettingsState
    ) {
        prefs.edit().putBoolean(key, enabled).apply()
        _settingsState.value = _settingsState.value.copyBlock(enabled)
    }
}
