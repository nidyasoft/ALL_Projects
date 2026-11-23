package com.example.settings

import androidx.compose.ui.graphics.Color

enum class KeyboardTheme(
    val id: String,
    val displayNamePersian: String,
    val displayNameEnglish: String,
    val isDark: Boolean,
    val defaultBg: Long,
    val defaultKeyBg: Long,
    val defaultSpecialKeyBg: Long,
    val defaultTextColor: Long,
    val defaultAccent: Long
) {
    // 3 Light Themes
    LIGHT_CLEAN(
        id = "light_clean",
        displayNamePersian = "سفید مینیمال مدرن",
        displayNameEnglish = "Clean Minimal White",
        isDark = false,
        defaultBg = 0xFFF3F4F8,
        defaultKeyBg = 0xFFFFFFFF,
        defaultSpecialKeyBg = 0xFFE2E4EC,
        defaultTextColor = 0xFF1C1B1F,
        defaultAccent = 0xFF1E5BB0
    ),
    LIGHT_SKY(
        id = "light_sky",
        displayNamePersian = "آبی آسمانی روشن",
        displayNameEnglish = "Sky Blue Light",
        isDark = false,
        defaultBg = 0xFFE0F2FE,
        defaultKeyBg = 0xFFFFFFFF,
        defaultSpecialKeyBg = 0xFFBAE6FD,
        defaultTextColor = 0xFF0369A1,
        defaultAccent = 0xFF0284C7
    ),
    LIGHT_WARM(
        id = "light_warm",
        displayNamePersian = "کرم و شنی ملایم",
        displayNameEnglish = "Warm Sand Cream",
        isDark = false,
        defaultBg = 0xFFF5EBE1,
        defaultKeyBg = 0xFFFFFBF7,
        defaultSpecialKeyBg = 0xFFE6D7C8,
        defaultTextColor = 0xFF433422,
        defaultAccent = 0xFFC26E28
    ),

    // 3 Dark Themes
    DARK_EPIC_AMOLED(
        id = "dark_epic_amoled",
        displayNamePersian = "مشکی عمیق اَمولد (AMOLED)",
        displayNameEnglish = "Epic AMOLED Black",
        isDark = true,
        defaultBg = 0xFF000000,
        defaultKeyBg = 0xFF141416,
        defaultSpecialKeyBg = 0xFF222228,
        defaultTextColor = 0xFFEDEDED,
        defaultAccent = 0xFF38BDF8
    ),
    DARK_CYBER_INDIGO(
        id = "dark_cyber_indigo",
        displayNamePersian = "آبی کاربنی حماسی (لوگوی Epic)",
        displayNameEnglish = "Cyber Deep Indigo",
        isDark = true,
        defaultBg = 0xFF0F0F1A,
        defaultKeyBg = 0xFF1E1B4B,
        defaultSpecialKeyBg = 0xFF2E2A72,
        defaultTextColor = 0xFFF1F5F9,
        defaultAccent = 0xFF38BDF8
    ),
    DARK_CHARCOAL(
        id = "dark_charcoal",
        displayNamePersian = "خاکستری تیره مات",
        displayNameEnglish = "Matte Charcoal Slate",
        isDark = true,
        defaultBg = 0xFF18191E,
        defaultKeyBg = 0xFF282A32,
        defaultSpecialKeyBg = 0xFF383B46,
        defaultTextColor = 0xFFE2E8F0,
        defaultAccent = 0xFFA78BFA
    ),

    // 1 Custom Theme
    CUSTOM(
        id = "custom",
        displayNamePersian = "تم سفارشی با پالت رنگی",
        displayNameEnglish = "Custom Color Palette",
        isDark = true,
        defaultBg = 0xFF111827,
        defaultKeyBg = 0xFF1F2937,
        defaultSpecialKeyBg = 0xFF374151,
        defaultTextColor = 0xFFF9FAFB,
        defaultAccent = 0xFF10B981
    );

    companion object {
        fun fromId(id: String): KeyboardTheme {
            return entries.firstOrNull { it.id == id } ?: DARK_CYBER_INDIGO
        }
    }
}

data class KeyboardColorTokens(
    val background: Color,
    val keyBackground: Color,
    val specialKeyBackground: Color,
    val textColor: Color,
    val accentColor: Color
)
