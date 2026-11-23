package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AppSettings(
    // Audio settings
    val defaultAlarmSound: String = "cyber_pulse",
    val defaultAlarmSoundTitle: String = "Cyber Pulse",
    val defaultTimerSound: String = "digital_beep",
    val defaultTimerSoundTitle: String = "Digital Beep",
    val defaultStopwatchSound: String = "cosmic_chime",
    val defaultStopwatchSoundTitle: String = "Cosmic Chime",
    val autoSilenceMinutes: Int = 5, // 0 = never, 1, 2, 5, 10, 15
    val alarmVolume: Float = 0.85f, // 0.0 to 1.0
    val timerVolume: Float = 0.85f,
    val snoozeDurationMinutes: Int = 5,

    // Hardware Button Dismiss Settings (قطع به وسیله دکمه‌های فیزیکی)
    val dismissPowerEnabled: Boolean = false,
    val dismissPowerPressCount: Int = 3, // e.g. 3 or 6 presses
    val dismissVolumeDownEnabled: Boolean = false,
    val dismissVolumeDownPressCount: Int = 3,
    val dismissVolumeUpEnabled: Boolean = false,
    val dismissVolumeUpPressCount: Int = 3,

    // Shake to Stop (قطع آلارم با تکان دادن گوشی)
    val shakeToStopEnabled: Boolean = true,
    val shakeSensitivity: String = "MEDIUM", // "LOW", "MEDIUM", "HIGH"

    // Presence / Pick-up Auto-Lower Volume (کاهش ۷۵٪ صدا با برداشتن یا نزدیک شدن)
    val presenceAutoLowerEnabled: Boolean = true,

    // Flip-to-Snooze (به تعویق انداختن با برگرداندن گوشی صفحه به سمت زمین)
    val flipToSnoozeEnabled: Boolean = true,

    // Guardian Alert (هشدار ثانویه پیامکی در صورت عدم قطع زنگ)
    val guardianAlertEnabled: Boolean = false,
    val guardianTimeoutMinutes: Int = 3, // 1, 2, 3, 5, 10
    val guardianPhoneNumber: String = "",
    val guardianContactName: String = "",
    val guardianCustomMessage: String = "هشدار اضطراری: زنگ ساعت هشدار قطع نشده و پاسخی ثبت نشده است.",
    val guardianIncludeMissedAlarms: Boolean = true,
    val guardianIncludeGyroscopeData: Boolean = true,
    val gyroscopeLoggerEnabled: Boolean = true,

    // Onboarding Wizard status
    val onboardingCompleted: Boolean = false,

    // In-App Calendar display settings
    val inAppShowSolarDate: Boolean = true,     // تاریخ شمسی داخل برنامه
    val inAppShowGregorianDate: Boolean = true, // تاریخ میلادی داخل برنامه
    val inAppShowLunarDate: Boolean = true,     // تاریخ قمری داخل برنامه

    // Home Screen Widget Triple Date settings & styling
    val widgetTripleShowDigitalClock: Boolean = true, // ساعت دیجیتال بزرگ در ویجت تاریخ
    val widgetTripleShowSolar: Boolean = true,        // تاریخ شمسی در ویجت
    val widgetTripleShowGregorian: Boolean = true,    // تاریخ میلادی در ویجت
    val widgetTripleShowLunar: Boolean = true,        // تاریخ قمری در ویجت
    val widgetTripleBgColor: Long = 0xFF0F172A,       // رنگ پس‌زمینه ویجت تقویم و ساعت سه‌گانه
    val widgetTripleBgOpacity: Float = 0.90f,         // میزان شفافیت پس‌زمینه (0.0 کاملا شفاف تا 1.0 مات)
    val widgetTripleClockColor: Long = 0xFF00F2FE,    // رنگ ساعت دیجیتال ویجت
    val widgetTripleSolarColor: Long = 0xFF00F2FE,    // رنگ تاریخ شمسی ویجت
    val widgetTripleGregorianColor: Long = 0xFFFFB703,// رنگ تاریخ میلادی ویجت
    val widgetTripleLunarColor: Long = 0xFFB388FF,    // رنگ تاریخ قمری ویجت

    // App Lock & Biometric Security (ورود امن دستگاه و برنامه)
    val appLockEnabled: Boolean = false,              // فعالسازی قفل و ورود امن
    val appLockUseFingerprint: Boolean = true,        // استفاده از اثر انگشت ثبت شده دستگاه
    val appLockUseFace: Boolean = true,               // استفاده از تشخیص چهره دستگاه
    val appLockUseDevicePin: Boolean = true,          // استفاده از پین کد یا قفل دستگاه
    val appLockUseAppPin: Boolean = false,            // استفاده از پین کد اختصاصی برنامه ساعت هشدار اپیک
    val appLockAppPinHash: String = "",               // هش دائمی رمز اختصاصی داخل برنامه

    // Home Screen Widget Analog Clock settings
    val widgetAnalogShowWeekday: Boolean = true,   // نمایش روز هفته در ویجت ساعت آنالوگ
    val widgetAnalogShowSolar: Boolean = true,     // تاریخ شمسی در ویجت ساعت آنالوگ
    val widgetAnalogShowGregorian: Boolean = true, // تاریخ میلادی در ویجت ساعت آنالوگ
    val widgetAnalogShowLunar: Boolean = true,     // تاریخ قمری در ویجت ساعت آنالوگ

    // Analog clock styling
    val clockFaceColor: Long = 0xFF0B0E14, // OLED dark
    val hourHandColor: Long = 0xFF00E5FF,  // Neon Cyan
    val minuteHandColor: Long = 0xFFD500F9,// Neon Purple
    val secondHandColor: Long = 0xFFFF3366,// Neon Red
    val numberColor: Long = 0xFFE0E6ED,    // Soft White
    val showNumbers: Boolean = true,
    val clockSizePx: Int = 220,            // 160 to 320 px
    val fontSizeScale: String = "NORMAL",   // "SMALL", "NORMAL", "LARGE", "LARGER"

    // Persistent top shade notification settings
    val showPersistentNotification: Boolean = true,
    val persistentShowSolarDate: Boolean = true,
    val persistentShowGregorianDate: Boolean = true,
    val persistentShowLunarDate: Boolean = true,
    val persistentShowNextAlarm: Boolean = true,
    val persistentShowTimer: Boolean = true,
    val persistentShowStopwatch: Boolean = true,

    // World Clock 4 Analog Clocks city IDs
    val worldClockAnalogCityIds: String = "Asia/Tehran,Europe/London,America/New_York,Asia/Tokyo",

    // Sorting preferences for Alarms & Timers
    val alarmSortBy: String = "TIME", // "TIME" (ساعت و دقیقه) or "ADDED" (ترتیب افزودن)
    val alarmSortAscending: Boolean = true,
    val timerSortBy: String = "TIME", // "TIME" or "ADDED"
    val timerSortAscending: Boolean = true,

    // Smartwatch (K59 Pro & BLE Wearables) Integration
    val smartwatchConnectedMac: String = "",
    val smartwatchDeviceName: String = "K59pro",
    val smartwatchFirmware: String = "MOY-JFE3-2.1.1",
    val smartwatchAutoConnect: Boolean = true,
    val smartwatchUseForSleepScore: Boolean = true,
    val smartwatchSimulateTelemetry: Boolean = false
) {
    // Backwards-compatibility getters for existing references
    val showSolarDate: Boolean get() = inAppShowSolarDate
    val showGregorianDate: Boolean get() = inAppShowGregorianDate
    val showLunarDate: Boolean get() = inAppShowLunarDate
    val persistentShowDates: Boolean get() = persistentShowSolarDate || persistentShowGregorianDate || persistentShowLunarDate
}

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("epic_clock_settings", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private fun loadSettings(): AppSettings {
        return AppSettings(
            defaultAlarmSound = prefs.getString("default_alarm_sound", "cyber_pulse") ?: "cyber_pulse",
            defaultAlarmSoundTitle = prefs.getString("default_alarm_sound_title", "پالس سایبر (Cyber Pulse)") ?: "پالس سایبر",
            defaultTimerSound = prefs.getString("default_timer_sound", "digital_beep") ?: "digital_beep",
            defaultTimerSoundTitle = prefs.getString("default_timer_sound_title", "بیپ دیجیتال (Digital Beep)") ?: "بیپ دیجیتال",
            defaultStopwatchSound = prefs.getString("default_stopwatch_sound", "cosmic_chime") ?: "cosmic_chime",
            defaultStopwatchSoundTitle = prefs.getString("default_stopwatch_sound_title", "زنگ کیهانی (Cosmic Chime)") ?: "زنگ کیهانی",
            autoSilenceMinutes = prefs.getInt("auto_silence_minutes", 5),
            alarmVolume = prefs.getFloat("alarm_volume", 0.85f),
            timerVolume = prefs.getFloat("timer_volume", 0.85f),
            snoozeDurationMinutes = prefs.getInt("snooze_duration_minutes", 5),

            // Hardware Button Dismiss Settings
            dismissPowerEnabled = prefs.getBoolean("dismiss_power_enabled", false),
            dismissPowerPressCount = prefs.getInt("dismiss_power_press_count", 3),
            dismissVolumeDownEnabled = prefs.getBoolean("dismiss_volume_down_enabled", false),
            dismissVolumeDownPressCount = prefs.getInt("dismiss_volume_down_press_count", 3),
            dismissVolumeUpEnabled = prefs.getBoolean("dismiss_volume_up_enabled", false),
            dismissVolumeUpPressCount = prefs.getInt("dismiss_volume_up_press_count", 3),

            // Shake to Stop
            shakeToStopEnabled = prefs.getBoolean("shake_to_stop_enabled", true),
            shakeSensitivity = prefs.getString("shake_sensitivity", "MEDIUM") ?: "MEDIUM",

            // Presence / Pick-up Auto-Lower Volume
            presenceAutoLowerEnabled = prefs.getBoolean("presence_auto_lower_enabled", true),

            // Flip-to-Snooze
            flipToSnoozeEnabled = prefs.getBoolean("flip_to_snooze_enabled", true),

            // Guardian Alert
            guardianAlertEnabled = prefs.getBoolean("guardian_alert_enabled", false),
            guardianTimeoutMinutes = prefs.getInt("guardian_timeout_minutes", 3),
            guardianPhoneNumber = prefs.getString("guardian_phone_number", "") ?: "",
            guardianContactName = prefs.getString("guardian_contact_name", "") ?: "",
            guardianCustomMessage = prefs.getString("guardian_custom_message", "هشدار اضطراری: زنگ ساعت هشدار قطع نشده و پاسخی ثبت نشده است.") ?: "هشدار اضطراری: زنگ ساعت هشدار قطع نشده و پاسخی ثبت نشده است.",
            guardianIncludeMissedAlarms = prefs.getBoolean("guardian_include_missed_alarms", true),
            guardianIncludeGyroscopeData = prefs.getBoolean("guardian_include_gyroscope_data", true),
            gyroscopeLoggerEnabled = prefs.getBoolean("gyroscope_logger_enabled", true),

            // Onboarding status
            onboardingCompleted = prefs.getBoolean("onboarding_completed", false),

            // In-app dates
            inAppShowSolarDate = prefs.getBoolean("in_app_show_solar_date", prefs.getBoolean("show_solar_date", true)),
            inAppShowGregorianDate = prefs.getBoolean("in_app_show_gregorian_date", prefs.getBoolean("show_gregorian_date", true)),
            inAppShowLunarDate = prefs.getBoolean("in_app_show_lunar_date", prefs.getBoolean("show_lunar_date", true)),

            // Home widget triple date
            widgetTripleShowDigitalClock = prefs.getBoolean("widget_triple_show_digital_clock", true),
            widgetTripleShowSolar = prefs.getBoolean("widget_triple_show_solar", true),
            widgetTripleShowGregorian = prefs.getBoolean("widget_triple_show_gregorian", true),
            widgetTripleShowLunar = prefs.getBoolean("widget_triple_show_lunar", true),
            widgetTripleBgColor = prefs.getLong("widget_triple_bg_color", 0xFF0F172A),
            widgetTripleBgOpacity = prefs.getFloat("widget_triple_bg_opacity", 0.90f),
            widgetTripleClockColor = prefs.getLong("widget_triple_clock_color", 0xFF00F2FE),
            widgetTripleSolarColor = prefs.getLong("widget_triple_solar_color", 0xFF00F2FE),
            widgetTripleGregorianColor = prefs.getLong("widget_triple_gregorian_color", 0xFFFFB703),
            widgetTripleLunarColor = prefs.getLong("widget_triple_lunar_color", 0xFFB388FF),

            // App Lock & Biometric Security
            appLockEnabled = prefs.getBoolean("app_lock_enabled", false),
            appLockUseFingerprint = prefs.getBoolean("app_lock_use_fingerprint", true),
            appLockUseFace = prefs.getBoolean("app_lock_use_face", true),
            appLockUseDevicePin = prefs.getBoolean("app_lock_use_device_pin", true),
            appLockUseAppPin = prefs.getBoolean("app_lock_use_app_pin", false),
            appLockAppPinHash = prefs.getString("app_lock_app_pin_hash", "") ?: "",

            // Home widget analog clock
            widgetAnalogShowWeekday = prefs.getBoolean("widget_analog_show_weekday", true),
            widgetAnalogShowSolar = prefs.getBoolean("widget_analog_show_solar", true),
            widgetAnalogShowGregorian = prefs.getBoolean("widget_analog_show_gregorian", true),
            widgetAnalogShowLunar = prefs.getBoolean("widget_analog_show_lunar", true),

            clockFaceColor = prefs.getLong("clock_face_color", 0xFF0B0E14),
            hourHandColor = prefs.getLong("hour_hand_color", 0xFF00E5FF),
            minuteHandColor = prefs.getLong("minute_hand_color", 0xFFD500F9),
            secondHandColor = prefs.getLong("second_hand_color", 0xFFFF3366),
            numberColor = prefs.getLong("number_color", 0xFFE0E6ED),
            showNumbers = prefs.getBoolean("show_numbers", true),
            clockSizePx = prefs.getInt("clock_size_px", 220),
            fontSizeScale = prefs.getString("font_size_scale", "NORMAL") ?: "NORMAL",

            // Persistent top shade notification
            showPersistentNotification = prefs.getBoolean("show_persistent_notification", true),
            persistentShowSolarDate = prefs.getBoolean("persistent_show_solar_date", true),
            persistentShowGregorianDate = prefs.getBoolean("persistent_show_gregorian_date", true),
            persistentShowLunarDate = prefs.getBoolean("persistent_show_lunar_date", true),
            persistentShowNextAlarm = prefs.getBoolean("persistent_show_next_alarm", true),
            persistentShowTimer = prefs.getBoolean("persistent_show_timer", true),
            persistentShowStopwatch = prefs.getBoolean("persistent_show_stopwatch", true),

            worldClockAnalogCityIds = prefs.getString("world_clock_analog_city_ids", "Asia/Tehran,Europe/London,America/New_York,Asia/Tokyo") ?: "Asia/Tehran,Europe/London,America/New_York,Asia/Tokyo",

            alarmSortBy = prefs.getString("alarm_sort_by", "TIME") ?: "TIME",
            alarmSortAscending = prefs.getBoolean("alarm_sort_ascending", true),
            timerSortBy = prefs.getString("timer_sort_by", "TIME") ?: "TIME",
            timerSortAscending = prefs.getBoolean("timer_sort_ascending", true),

            smartwatchConnectedMac = prefs.getString("smartwatch_connected_mac", "") ?: "",
            smartwatchDeviceName = prefs.getString("smartwatch_device_name", "K59pro") ?: "K59pro",
            smartwatchFirmware = prefs.getString("smartwatch_firmware", "MOY-JFE3-2.1.1") ?: "MOY-JFE3-2.1.1",
            smartwatchAutoConnect = prefs.getBoolean("smartwatch_auto_connect", true),
            smartwatchUseForSleepScore = prefs.getBoolean("smartwatch_use_for_sleep_score", true),
            smartwatchSimulateTelemetry = prefs.getBoolean("smartwatch_simulate_telemetry", false)
        )
    }

    fun getSettings(): AppSettings = _settings.value

    fun getLanguage(): com.example.i18n.AppLanguage {
        val code = prefs.getString("pref_language", com.example.i18n.AppLanguage.PERSIAN.name) ?: com.example.i18n.AppLanguage.PERSIAN.name
        return try { com.example.i18n.AppLanguage.valueOf(code) } catch (_: Throwable) { com.example.i18n.AppLanguage.PERSIAN }
    }

    fun getThemeMode(): com.example.ui.theme.AppThemeMode {
        val mode = prefs.getString("pref_theme_mode", com.example.ui.theme.AppThemeMode.DARK.name) ?: com.example.ui.theme.AppThemeMode.DARK.name
        return try { com.example.ui.theme.AppThemeMode.valueOf(mode) } catch (_: Throwable) { com.example.ui.theme.AppThemeMode.DARK }
    }

    fun setLanguage(language: com.example.i18n.AppLanguage) {
        prefs.edit().putString("pref_language", language.name).apply()
    }

    fun setThemeMode(themeMode: com.example.ui.theme.AppThemeMode) {
        prefs.edit().putString("pref_theme_mode", themeMode.name).apply()
    }

    fun updateSettings(transform: (AppSettings) -> AppSettings) {
        val updated = transform(_settings.value)
        updateSettings(updated)
    }

    fun updateSettings(newSettings: AppSettings) {
        _settings.value = newSettings
        prefs.edit().apply {
            putString("default_alarm_sound", newSettings.defaultAlarmSound)
            putString("default_alarm_sound_title", newSettings.defaultAlarmSoundTitle)
            putString("default_timer_sound", newSettings.defaultTimerSound)
            putString("default_timer_sound_title", newSettings.defaultTimerSoundTitle)
            putString("default_stopwatch_sound", newSettings.defaultStopwatchSound)
            putString("default_stopwatch_sound_title", newSettings.defaultStopwatchSoundTitle)
            putInt("auto_silence_minutes", newSettings.autoSilenceMinutes)
            putFloat("alarm_volume", newSettings.alarmVolume)
            putFloat("timer_volume", newSettings.timerVolume)
            putInt("snooze_duration_minutes", newSettings.snoozeDurationMinutes)

            // Hardware Button Dismiss Settings
            putBoolean("dismiss_power_enabled", newSettings.dismissPowerEnabled)
            putInt("dismiss_power_press_count", newSettings.dismissPowerPressCount)
            putBoolean("dismiss_volume_down_enabled", newSettings.dismissVolumeDownEnabled)
            putInt("dismiss_volume_down_press_count", newSettings.dismissVolumeDownPressCount)
            putBoolean("dismiss_volume_up_enabled", newSettings.dismissVolumeUpEnabled)
            putInt("dismiss_volume_up_press_count", newSettings.dismissVolumeUpPressCount)

            // Shake to Stop
            putBoolean("shake_to_stop_enabled", newSettings.shakeToStopEnabled)
            putString("shake_sensitivity", newSettings.shakeSensitivity)

            // Presence / Pick-up Auto-Lower Volume
            putBoolean("presence_auto_lower_enabled", newSettings.presenceAutoLowerEnabled)

            // Flip-to-Snooze
            putBoolean("flip_to_snooze_enabled", newSettings.flipToSnoozeEnabled)

            // Guardian Alert
            putBoolean("guardian_alert_enabled", newSettings.guardianAlertEnabled)
            putInt("guardian_timeout_minutes", newSettings.guardianTimeoutMinutes)
            putString("guardian_phone_number", newSettings.guardianPhoneNumber)
            putString("guardian_contact_name", newSettings.guardianContactName)
            putString("guardian_custom_message", newSettings.guardianCustomMessage)
            putBoolean("guardian_include_missed_alarms", newSettings.guardianIncludeMissedAlarms)
            putBoolean("guardian_include_gyroscope_data", newSettings.guardianIncludeGyroscopeData)
            putBoolean("gyroscope_logger_enabled", newSettings.gyroscopeLoggerEnabled)

            // Onboarding status
            putBoolean("onboarding_completed", newSettings.onboardingCompleted)

            // In-app dates
            putBoolean("in_app_show_solar_date", newSettings.inAppShowSolarDate)
            putBoolean("in_app_show_gregorian_date", newSettings.inAppShowGregorianDate)
            putBoolean("in_app_show_lunar_date", newSettings.inAppShowLunarDate)
            // also sync legacy key
            putBoolean("show_solar_date", newSettings.inAppShowSolarDate)
            putBoolean("show_gregorian_date", newSettings.inAppShowGregorianDate)
            putBoolean("show_lunar_date", newSettings.inAppShowLunarDate)

            // Home widget triple date
            putBoolean("widget_triple_show_digital_clock", newSettings.widgetTripleShowDigitalClock)
            putBoolean("widget_triple_show_solar", newSettings.widgetTripleShowSolar)
            putBoolean("widget_triple_show_gregorian", newSettings.widgetTripleShowGregorian)
            putBoolean("widget_triple_show_lunar", newSettings.widgetTripleShowLunar)
            putLong("widget_triple_bg_color", newSettings.widgetTripleBgColor)
            putFloat("widget_triple_bg_opacity", newSettings.widgetTripleBgOpacity)
            putLong("widget_triple_clock_color", newSettings.widgetTripleClockColor)
            putLong("widget_triple_solar_color", newSettings.widgetTripleSolarColor)
            putLong("widget_triple_gregorian_color", newSettings.widgetTripleGregorianColor)
            putLong("widget_triple_lunar_color", newSettings.widgetTripleLunarColor)

            // App Lock & Biometric Security
            putBoolean("app_lock_enabled", newSettings.appLockEnabled)
            putBoolean("app_lock_use_fingerprint", newSettings.appLockUseFingerprint)
            putBoolean("app_lock_use_face", newSettings.appLockUseFace)
            putBoolean("app_lock_use_device_pin", newSettings.appLockUseDevicePin)
            putBoolean("app_lock_use_app_pin", newSettings.appLockUseAppPin)
            putString("app_lock_app_pin_hash", newSettings.appLockAppPinHash)

            // Home widget analog clock
            putBoolean("widget_analog_show_weekday", newSettings.widgetAnalogShowWeekday)
            putBoolean("widget_analog_show_solar", newSettings.widgetAnalogShowSolar)
            putBoolean("widget_analog_show_gregorian", newSettings.widgetAnalogShowGregorian)
            putBoolean("widget_analog_show_lunar", newSettings.widgetAnalogShowLunar)

            putLong("clock_face_color", newSettings.clockFaceColor)
            putLong("hour_hand_color", newSettings.hourHandColor)
            putLong("minute_hand_color", newSettings.minuteHandColor)
            putLong("second_hand_color", newSettings.secondHandColor)
            putLong("number_color", newSettings.numberColor)
            putBoolean("show_numbers", newSettings.showNumbers)
            putInt("clock_size_px", newSettings.clockSizePx)
            putString("font_size_scale", newSettings.fontSizeScale)

            // Persistent top shade notification
            putBoolean("show_persistent_notification", newSettings.showPersistentNotification)
            putBoolean("persistent_show_solar_date", newSettings.persistentShowSolarDate)
            putBoolean("persistent_show_gregorian_date", newSettings.persistentShowGregorianDate)
            putBoolean("persistent_show_lunar_date", newSettings.persistentShowLunarDate)
            putBoolean("persistent_show_dates", newSettings.persistentShowDates)
            putBoolean("persistent_show_next_alarm", newSettings.persistentShowNextAlarm)
            putBoolean("persistent_show_timer", newSettings.persistentShowTimer)
            putBoolean("persistent_show_stopwatch", newSettings.persistentShowStopwatch)

            putString("world_clock_analog_city_ids", newSettings.worldClockAnalogCityIds)

            putString("alarm_sort_by", newSettings.alarmSortBy)
            putBoolean("alarm_sort_ascending", newSettings.alarmSortAscending)
            putString("timer_sort_by", newSettings.timerSortBy)
            putBoolean("timer_sort_ascending", newSettings.timerSortAscending)

            putString("smartwatch_connected_mac", newSettings.smartwatchConnectedMac)
            putString("smartwatch_device_name", newSettings.smartwatchDeviceName)
            putString("smartwatch_firmware", newSettings.smartwatchFirmware)
            putBoolean("smartwatch_auto_connect", newSettings.smartwatchAutoConnect)
            putBoolean("smartwatch_use_for_sleep_score", newSettings.smartwatchUseForSleepScore)
            putBoolean("smartwatch_simulate_telemetry", newSettings.smartwatchSimulateTelemetry)
            apply()
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: SettingsManager? = null

        fun getInstance(context: Context): SettingsManager {
            return INSTANCE ?: synchronized(this) {
                val inst = SettingsManager(context.applicationContext)
                INSTANCE = inst
                inst
            }
        }
    }
}
