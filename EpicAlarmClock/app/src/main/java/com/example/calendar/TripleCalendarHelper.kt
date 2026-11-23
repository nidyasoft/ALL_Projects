package com.example.calendar

import com.example.i18n.AppLanguage
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlin.math.floor

data class TripleDate(
    val solarYear: Int,
    val solarMonth: Int,
    val solarDay: Int,
    val solarMonthName: String,
    val gregorianYear: Int,
    val gregorianMonth: Int,
    val gregorianDay: Int,
    val gregorianMonthName: String,
    val lunarYear: Int,
    val lunarMonth: Int,
    val lunarDay: Int,
    val lunarMonthName: String,
    val weekdayName: String
) {
    val solarFormatted: String
        get() = "$solarDay $solarMonthName $solarYear"

    val gregorianFormatted: String
        get() = "$gregorianDay $gregorianMonthName $gregorianYear"

    val lunarFormatted: String
        get() = "$lunarDay $lunarMonthName $lunarYear"
}

object TripleCalendarHelper {

    val solarMonthNamesFa = listOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
    )

    val solarMonthNamesEn = listOf(
        "Farvardin", "Ordibehesht", "Khordad", "Tir", "Mordad", "Shahrivar",
        "Mehr", "Aban", "Azar", "Dey", "Bahman", "Esfand"
    )

    val solarMonthNamesAr = listOf(
        "فروردين", "أرديبهشت", "خرداد", "تير", "مرداد", "شهريور",
        "مهر", "آبان", "آذر", "دي", "بهمن", "إسفند"
    )

    val lunarMonthNamesFa = listOf(
        "محرم", "صفر", "ربیع‌الاول", "ربیع‌الثانی", "جمادی‌الاول", "جمادی‌الثانی",
        "رجب", "شعبان", "رمضان", "شوال", "ذی‌القعده", "ذی‌الحجه"
    )

    val lunarMonthNamesAr = listOf(
        "محرم", "صفر", "ربيع الأول", "ربيع الثاني", "جمادى الأولى", "جمادى الآخرة",
        "رجب", "شعبان", "رمضان", "شوال", "ذو القعدة", "ذو الحجة"
    )

    val lunarMonthNamesEn = listOf(
        "Muharram", "Safar", "Rabi' al-Awwal", "Rabi' ath-Thani", "Jumada al-Awwal", "Jumada ath-Thani",
        "Rajab", "Sha'ban", "Ramadan", "Shawwal", "Dhu al-Qi'dah", "Dhu al-Hijjah"
    )

    val lunarMonthNamesAz = listOf(
        "Məhərrəm", "Səfər", "Rəbiüləvvəl", "Rəbiülaxır", "Cəmadiyələvvəl", "Cəmadiyəlaxır",
        "Rəcəb", "Şaban", "Ramazan", "Şəvval", "Zilqədə", "Zilhiccə"
    )

    val gregorianMonthNamesEn = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    val gregorianMonthNamesFa = listOf(
        "ژانویه", "فوریه", "مارس", "آوریل", "مه", "ژوئن",
        "ژوئیه", "اوت", "سپتامبر", "اکتبر", "نوامبر", "دسامبر"
    )

    val gregorianMonthNamesAr = listOf(
        "يناير", "فبراير", "مارس", "أبريل", "مايو", "يونيو",
        "يوليو", "أغسطس", "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر"
    )

    val weekdayNamesFa = listOf(
        "یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "جمعه", "شنبه"
    )

    val weekdayNamesAr = listOf(
        "الأحد", "الإثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة", "السبت"
    )

    val weekdayNamesEn = listOf(
        "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"
    )

    val weekdayNamesAz = listOf(
        "Bazar", "Bazar ertəsi", "Çərşənbə axşamı", "Çərşənbə", "Cümə axşamı", "Cümə", "Şənbə"
    )

    fun getTodayTripleDate(lang: AppLanguage): TripleDate {
        return getTripleDateForMillis(System.currentTimeMillis(), TimeZone.getDefault(), lang)
    }

    fun getTripleDateForMillis(timeMillis: Long, timeZone: TimeZone = TimeZone.getDefault(), lang: AppLanguage): TripleDate {
        val cal = Calendar.getInstance(timeZone).apply {
            this.timeInMillis = timeMillis
        }
        val gYear = cal.get(Calendar.YEAR)
        val gMonth = cal.get(Calendar.MONTH) + 1
        val gDay = cal.get(Calendar.DAY_OF_MONTH)
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 1 = Sunday, 7 = Saturday

        return calculateTripleDate(gYear, gMonth, gDay, dayOfWeek, lang)
    }

    fun calculateTripleDate(gYear: Int, gMonth: Int, gDay: Int, dayOfWeek: Int, lang: AppLanguage): TripleDate {
        val (sYear, sMonth, sDay) = gregorianToSolar(gYear, gMonth, gDay)
        val (lYear, lMonth, lDay) = gregorianToLunar(gYear, gMonth, gDay)

        val sMonthName = when (lang) {
            AppLanguage.ENGLISH -> solarMonthNamesEn.getOrElse(sMonth - 1) { "" }
            AppLanguage.ARABIC -> solarMonthNamesAr.getOrElse(sMonth - 1) { "" }
            else -> solarMonthNamesFa.getOrElse(sMonth - 1) { "" }
        }

        val gMonthName = when (lang) {
            AppLanguage.ENGLISH -> gregorianMonthNamesEn.getOrElse(gMonth - 1) { "" }
            AppLanguage.ARABIC -> gregorianMonthNamesAr.getOrElse(gMonth - 1) { "" }
            else -> gregorianMonthNamesFa.getOrElse(gMonth - 1) { "" }
        }

        val lMonthName = when (lang) {
            AppLanguage.ENGLISH -> lunarMonthNamesEn.getOrElse(lMonth - 1) { "" }
            AppLanguage.ARABIC -> lunarMonthNamesAr.getOrElse(lMonth - 1) { "" }
            AppLanguage.AZERBAIJANI -> lunarMonthNamesAz.getOrElse(lMonth - 1) { "" }
            else -> lunarMonthNamesFa.getOrElse(lMonth - 1) { "" }
        }

        val wName = when (lang) {
            AppLanguage.ENGLISH -> weekdayNamesEn.getOrElse(dayOfWeek - 1) { "" }
            AppLanguage.ARABIC -> weekdayNamesAr.getOrElse(dayOfWeek - 1) { "" }
            AppLanguage.AZERBAIJANI -> weekdayNamesAz.getOrElse(dayOfWeek - 1) { "" }
            else -> weekdayNamesFa.getOrElse(dayOfWeek - 1) { "" }
        }

        return TripleDate(
            solarYear = sYear,
            solarMonth = sMonth,
            solarDay = sDay,
            solarMonthName = sMonthName,
            gregorianYear = gYear,
            gregorianMonth = gMonth,
            gregorianDay = gDay,
            gregorianMonthName = gMonthName,
            lunarYear = lYear,
            lunarMonth = lMonth,
            lunarDay = lDay,
            lunarMonthName = lMonthName,
            weekdayName = wName
        )
    }

    // Algorithmic conversion from Gregorian to Solar Hijri (Jalali)
    fun gregorianToSolar(gy: Int, gm: Int, gd: Int): Triple<Int, Int, Int> {
        val gDaysInMonth = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)
        val gy2 = if (gm > 2) gy + 1 else gy
        var gDayNo = 355666 + (365 * gy) + ((gy2 + 3) / 4) - ((gy2 + 99) / 100) + ((gy2 + 399) / 400) + gd + gDaysInMonth[gm - 1]

        var jy = -1595 + (33 * (gDayNo / 12053))
        gDayNo %= 12053

        jy += 4 * (gDayNo / 1461)
        gDayNo %= 1461

        if (gDayNo > 365) {
            jy += (gDayNo - 1) / 365
            gDayNo = (gDayNo - 1) % 365
        }

        val jm: Int
        val jd: Int
        if (gDayNo < 186) {
            jm = 1 + (gDayNo / 31)
            jd = 1 + (gDayNo % 31)
        } else {
            jm = 7 + ((gDayNo - 186) / 30)
            jd = 1 + ((gDayNo - 186) % 30)
        }
        return Triple(jy, jm, jd)
    }

    // Solar Hijri (Jalali) to Gregorian
    fun solarToGregorian(jy: Int, jm: Int, jd: Int): Triple<Int, Int, Int> {
        val jy2 = jy + 1595
        var days = -355668 + (365 * jy2) + ((jy2 / 33) * 8) + (((jy2 % 33) + 3) / 4) + jd
        days += if (jm < 7) (jm - 1) * 31 else ((jm - 7) * 30) + 186

        var gy = 400 * (days / 146097)
        days %= 146097

        if (days > 36524) {
            days--
            gy += 100 * (days / 36524)
            days %= 36524
            if (days >= 365) days++
        }

        gy += 4 * (days / 1461)
        days %= 1461

        if (days > 365) {
            gy += (days - 1) / 365
            days = (days - 1) % 365
        }

        val gDays = intArrayOf(31, if ((gy % 4 == 0 && gy % 100 != 0) || (gy % 400 == 0)) 29 else 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        var gm = 0
        while (gm < 12 && days >= gDays[gm]) {
            days -= gDays[gm]
            gm++
        }
        return Triple(gy, gm + 1, days + 1)
    }

    // Accurate Gregorian to Islamic Lunar (Hijri Qamari) algorithm
    fun gregorianToLunar(year: Int, month: Int, day: Int): Triple<Int, Int, Int> {
        // Method 1: Android API 26+ built-in Umm al-Qura standard HijrahDate
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                val localDate = java.time.LocalDate.of(year, month, day)
                val hijrahDate = java.time.chrono.HijrahDate.from(localDate)
                val hYear = hijrahDate.get(java.time.temporal.ChronoField.YEAR)
                val hMonth = hijrahDate.get(java.time.temporal.ChronoField.MONTH_OF_YEAR)
                val hDay = hijrahDate.get(java.time.temporal.ChronoField.DAY_OF_MONTH)
                if (hYear in 1300..1600 && hMonth in 1..12 && hDay in 1..30) {
                    return Triple(hYear, hMonth, hDay)
                }
            }
        } catch (_: Throwable) {}

        // Method 2: High-accuracy Tabular Islamic Calendar mathematical algorithm
        return calculateCivilHijri(year, month, day)
    }

    // Precise civil/astronomical tabular algorithm
    private fun calculateCivilHijri(year: Int, month: Int, day: Int): Triple<Int, Int, Int> {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = y / 100
        val b = 2 - a + a / 4
        val jd = (floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524).toLong()

        // Days since Islamic Epoch (July 16, 622 CE = JD 1948439.5)
        val days = jd - 1948439L
        val cycle = days / 10631L
        val rem = (days % 10631L).toInt()

        val leapYears = intArrayOf(2, 5, 7, 10, 13, 16, 18, 21, 24, 26, 29)
        var yearInCycle = 1
        var daysAcc = 0
        var remDays = rem

        for (yr in 1..30) {
            val lenYr = if (yr in leapYears) 355 else 354
            if (daysAcc + lenYr > rem) {
                yearInCycle = yr
                remDays = rem - daysAcc
                break
            }
            daysAcc += lenYr
        }

        val hYear = (cycle * 30 + yearInCycle).toInt()
        val isLeap = yearInCycle in leapYears
        var hMonth = 1
        var hDay = 1

        for (mon in 1..12) {
            val lenMon = if (mon % 2 != 0) 30 else if (mon == 12 && isLeap) 30 else 29
            if (remDays < lenMon) {
                hMonth = mon
                hDay = remDays + 1
                break
            }
            remDays -= lenMon
        }

        return Triple(hYear, hMonth, hDay.coerceIn(1, 30))
    }

    fun lunarToGregorian(hy: Int, hm: Int, hd: Int): Triple<Int, Int, Int> {
        val safeMonth = hm.coerceIn(1, 12)
        // Method 1: Android API 26+ built-in Umm al-Qura HijrahDate (matches gregorianToLunar)
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                val firstOfMonth = java.time.chrono.HijrahDate.of(hy, safeMonth, 1)
                val maxDay = firstOfMonth.lengthOfMonth()
                val safeDay = hd.coerceIn(1, maxDay)
                val hijrahDate = java.time.chrono.HijrahDate.of(hy, safeMonth, safeDay)
                val localDate = java.time.LocalDate.from(hijrahDate)
                return Triple(localDate.year, localDate.monthValue, localDate.dayOfMonth)
            }
        } catch (_: Throwable) {}

        // Method 2: Tabular Islamic Calendar mathematical fallback
        val leapYears = intArrayOf(2, 5, 7, 10, 13, 16, 18, 21, 24, 26, 29)
        val cycle = (hy - 1) / 30
        val yearInCycle = ((hy - 1) % 30 + 30) % 30 + 1

        var days = cycle * 10631L
        for (yr in 1 until yearInCycle) {
            days += if (yr in leapYears) 355L else 354L
        }

        val isLeap = yearInCycle in leapYears
        for (m in 1 until safeMonth) {
            val lenMon = if (m % 2 != 0) 30 else if (m == 12 && isLeap) 30 else 29
            days += lenMon
        }
        days += (hd.coerceIn(1, 30) - 1)

        val jd = days + 1948439L
        return julianDayToGregorian(jd)
    }

    private fun julianDayToGregorian(jd: Long): Triple<Int, Int, Int> {
        val z = jd + 1
        val alpha = floor((z - 1867216.25) / 36524.25).toLong()
        val a = z + 1 + alpha - (alpha / 4)
        val b = a + 1524
        val c = floor((b - 122.1) / 365.25).toLong()
        val d = floor(365.25 * c).toLong()
        val e = floor((b - d) / 30.6001).toLong()
        val day = (b - d - floor(30.6001 * e)).toInt()
        val month = (if (e < 14) e - 1 else e - 13).toInt()
        val year = (if (month > 2) c - 4716 else c - 4715).toInt()
        return Triple(year, month, day)
    }

    // Convert anchor date in either calendar to epoch day (days since 1970-01-01)
    fun getEpochDayFromCalendar(calType: String, year: Int, month: Int, day: Int): Long {
        val (gy, gm, gd) = when (calType.uppercase(Locale.ROOT)) {
            "SOLAR" -> solarToGregorian(year, month, day)
            "LUNAR" -> lunarToGregorian(year, month, day)
            else -> Triple(year, month, day)
        }
        val calendar = Calendar.getInstance()
        calendar.set(gy, gm - 1, gd, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis / (24 * 60 * 60 * 1000)
    }

    // Convert any date & time in Solar, Gregorian, or Lunar calendar directly to epoch millis
    fun getMillisFromCalendar(calType: String, year: Int, month: Int, day: Int, hour: Int, minute: Int): Long {
        val (gy, gm, gd) = when (calType.uppercase(Locale.ROOT)) {
            "SOLAR" -> solarToGregorian(year, month, day)
            "LUNAR" -> lunarToGregorian(year, month, day)
            else -> Triple(year, month, day)
        }
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, gy)
            set(Calendar.MONTH, gm - 1)
            set(Calendar.DAY_OF_MONTH, gd)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    // Days count in a specific month for Solar, Gregorian, or Lunar calendars
    fun getDaysInMonth(calType: String, year: Int, month: Int): Int {
        return when (calType.uppercase(Locale.ROOT)) {
            "SOLAR" -> {
                when {
                    month in 1..6 -> 31
                    month in 7..11 -> 30
                    month == 12 -> {
                        // Solar leap year calculation
                        val a = 0.025
                        val b = 266
                        val leap = (((year + 38) * 31) % 128) <= 30
                        if (leap) 30 else 29
                    }
                    else -> 30
                }
            }
            "GREGORIAN" -> {
                when (month) {
                    1, 3, 5, 7, 8, 10, 12 -> 31
                    4, 6, 9, 11 -> 30
                    2 -> if ((year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)) 29 else 28
                    else -> 30
                }
            }
            "LUNAR" -> {
                try {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        val safeMonth = month.coerceIn(1, 12)
                        return java.time.chrono.HijrahDate.of(year, safeMonth, 1).lengthOfMonth()
                    }
                } catch (_: Throwable) {}

                // Tabular Islamic calendar fallback: odd months 30, even months 29, 12th month 30 in leap year
                val leapYears = intArrayOf(2, 5, 7, 10, 13, 16, 18, 21, 24, 26, 29)
                val yearInCycle = (((year - 1) % 30) + 30) % 30 + 1
                val isLeap = yearInCycle in leapYears
                if (month % 2 != 0) 30 else if (month == 12 && isLeap) 30 else 29
            }
            else -> 30
        }
    }
}

