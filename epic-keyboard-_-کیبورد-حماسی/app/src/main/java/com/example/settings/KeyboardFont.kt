package com.example.settings

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.R

enum class KeyboardFont(
    val id: String,
    val displayNamePersian: String,
    val displayNameEnglish: String,
    val fontResId: Int?
) {
    VAZIRMATN(
        id = "vazirmatn",
        displayNamePersian = "وزیرمتن (سنس استاندارد و مدرن)",
        displayNameEnglish = "Vazirmatn (Modern Sans)",
        fontResId = R.font.vazirmatn
    ),
    NOTO_SANS_ARABIC(
        id = "noto_sans",
        displayNamePersian = "نوتو سنس (استاندارد گوگل)",
        displayNameEnglish = "Noto Sans",
        fontResId = R.font.noto_sans_arabic
    ),
    NOTO_NASKH_ARABIC(
        id = "noto_naskh",
        displayNamePersian = "نوتو نسخ (خط اصیل و خوانا)",
        displayNameEnglish = "Noto Naskh",
        fontResId = R.font.noto_naskh_arabic
    ),
    POPPINS(
        id = "poppins",
        displayNamePersian = "پاپینز (هندسی و شیک)",
        displayNameEnglish = "Poppins",
        fontResId = R.font.poppins
    ),
    SYSTEM_DEFAULT(
        id = "system",
        displayNamePersian = "سنس پیش‌فرض سیستم",
        displayNameEnglish = "System Default",
        fontResId = null
    );

    fun toFontFamily(): FontFamily {
        return if (fontResId != null) {
            FontFamily(Font(fontResId, FontWeight.Normal))
        } else {
            FontFamily.Default
        }
    }

    companion object {
        fun fromId(id: String): KeyboardFont {
            return entries.firstOrNull { it.id == id } ?: VAZIRMATN
        }
    }
}
