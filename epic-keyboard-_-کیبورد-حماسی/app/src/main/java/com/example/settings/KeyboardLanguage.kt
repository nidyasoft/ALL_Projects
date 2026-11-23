package com.example.settings

enum class KeyboardLanguage(
    val id: String,
    val displayName: String,
    val nativeName: String,
    val isRtl: Boolean,
    val hasHarakat: Boolean
) {
    PERSIAN(
        id = "fa",
        displayName = "Persian",
        nativeName = "فارسی",
        isRtl = true,
        hasHarakat = true
    ),
    ENGLISH(
        id = "en",
        displayName = "English",
        nativeName = "English",
        isRtl = false,
        hasHarakat = false
    ),
    TURKISH(
        id = "tr",
        displayName = "Turkish",
        nativeName = "Türkçe",
        isRtl = false,
        hasHarakat = false
    ),
    AZERBAIJANI_LATIN(
        id = "az_latn",
        displayName = "Azerbaijani (Latin)",
        nativeName = "Azərbaycanca",
        isRtl = false,
        hasHarakat = false
    ),
    AZERBAIJANI_ARABIC(
        id = "az_arab",
        displayName = "Azerbaijani (Arabic)",
        nativeName = "تورکجه",
        isRtl = true,
        hasHarakat = true
    ),
    ARABIC(
        id = "ar",
        displayName = "Arabic",
        nativeName = "العربية",
        isRtl = true,
        hasHarakat = true
    );

    companion object {
        fun fromId(id: String): KeyboardLanguage {
            return entries.firstOrNull { it.id == id } ?: PERSIAN
        }
    }
}
