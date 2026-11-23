package com.example.settings

enum class KeyboardMode(
    val id: String,
    val titlePersian: String,
    val titleEnglish: String,
    val descriptionPersian: String
) {
    STANDARD(
        id = "standard",
        titlePersian = "استاندارد تمام عرض",
        titleEnglish = "Standard (Full Width)",
        descriptionPersian = "حالت کلاسیک و کامل پرکننده پایین صفحه"
    ),
    ONE_HANDED_LEFT(
        id = "one_handed_left",
        titlePersian = "یک دست (چپ)",
        titleEnglish = "One-Handed (Left)",
        descriptionPersian = "چیدمان بهینه شده برای تایپ آسان با دست چپ"
    ),
    ONE_HANDED_RIGHT(
        id = "one_handed_right",
        titlePersian = "یک دست (راست)",
        titleEnglish = "One-Handed (Right)",
        descriptionPersian = "چیدمان بهینه شده برای تایپ آسان با دست راست"
    ),
    FLOATING(
        id = "floating",
        titlePersian = "شناور معلق روی همه اپلیکیشن‌ها",
        titleEnglish = "Floating Overlay",
        descriptionPersian = "کیبورد پنجره‌ای معلق با پین لمسی و تغییر اندازه"
    );

    companion object {
        fun fromId(id: String): KeyboardMode {
            return when (id) {
                "floating_bottom", "floating_top", "floating" -> FLOATING
                "one_handed_left" -> ONE_HANDED_LEFT
                "one_handed_right" -> ONE_HANDED_RIGHT
                else -> STANDARD
            }
        }
    }
}
