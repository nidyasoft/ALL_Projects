package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.calendar.TripleCalendarHelper
import com.example.data.local.AppDatabase
import com.example.data.local.SettingsManager
import com.example.data.model.AlarmEntity
import com.example.data.model.TimerEntity
import com.example.i18n.AppLanguage
import com.example.i18n.Strings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

object PersistentStatusNotificationHelper {

    const val CHANNEL_ID = "epic_persistent_status_channel"
    const val NOTIFICATION_ID = 5050

    private val scope = CoroutineScope(Dispatchers.IO)

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "نوار اعلان وضعیت دائمی (Persistent Status Card)"
            val descriptionText = "نمایش دائم تاریخ‌های سه‌گانه، شمارش معکوس آلارم بعدی، وضعیت تایمر و کرنومتر (امکان شخصی‌سازی صدا، ویبره و اعلان)"
            // Use IMPORTANCE_DEFAULT to allow Android system Settings > Apps > Epic Clock > Notifications to fully customize sound, vibration, etc.
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun updateStatusNotification(
        context: Context,
        runningStopwatchTimeStr: String? = null,
        isStopwatchRunning: Boolean = false
    ) {
        val settingsManager = SettingsManager.getInstance(context)
        val settings = settingsManager.getSettings()

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (!settings.showPersistentNotification) {
            notificationManager.cancel(NOTIFICATION_ID)
            return
        }

        createNotificationChannel(context)

        scope.launch {
            try {
                val language = settingsManager.getLanguage()
                val triple = TripleCalendarHelper.getTodayTripleDate(language)

                // Query next alarm
                val db = AppDatabase.getInstance(context)
                val allAlarms = db.alarmDao().getAllAlarmsList()
                val activeAlarms = allAlarms.filter { it.isEnabled }
                val nextAlarmInfo = calculateNextAlarmInfo(activeAlarms, language)

                // Query active timers
                val allTimers = db.timerDao().getAllTimersList()
                val runningTimer = allTimers.firstOrNull { it.isRunning && it.remainingSeconds > 0 }

                // Compose Date Header with separate toggles for Solar, Gregorian, Lunar
                val dateParts = mutableListOf<String>()
                if (settings.persistentShowSolarDate) {
                    dateParts.add("${Strings.formatNumber(triple.solarDay, language)} ${triple.solarMonthName}")
                }
                if (settings.persistentShowGregorianDate) {
                    dateParts.add("${Strings.formatNumber(triple.gregorianDay, language)} ${triple.gregorianMonthName}")
                }
                if (settings.persistentShowLunarDate) {
                    dateParts.add("${Strings.formatNumber(triple.lunarDay, language)} ${triple.lunarMonthName}")
                }

                val dateTitle = if (dateParts.isNotEmpty()) {
                    "${triple.weekdayName} | " + dateParts.joinToString(" • ")
                } else {
                    when (language) {
                        AppLanguage.ENGLISH -> "Epic Clock & Calendar"
                        AppLanguage.ARABIC -> "ساعة وتقويم إبيك"
                        AppLanguage.AZERBAIJANI -> "Epik Saat və Təqvim"
                        else -> "ساعت و تقویم سه‌گانه اپیک"
                    }
                }

                // Compose content lines
                val lines = mutableListOf<String>()

                if (settings.persistentShowNextAlarm) {
                    lines.add(nextAlarmInfo)
                }

                if (settings.persistentShowTimer) {
                    if (runningTimer != null) {
                        val timerLabel = if (runningTimer.label.isNotBlank()) " (${runningTimer.label})" else ""
                        val remFormatted = formatSeconds(runningTimer.remainingSeconds, language)
                        lines.add("⏳ تایمر فعال$timerLabel: باقی‌مانده $remFormatted")
                    } else {
                        lines.add("⏳ تایمر فعال: موردی فعال نیست")
                    }
                }

                if (settings.persistentShowStopwatch) {
                    if (isStopwatchRunning && !runningStopwatchTimeStr.isNullOrBlank()) {
                        lines.add("⏱️ کرنومتر در حال اجرا: $runningStopwatchTimeStr")
                    }
                }

                val primaryLine = lines.firstOrNull() ?: dateTitle
                val bigTextContent = lines.joinToString("\n")

                // Open MainActivity on tap
                val mainIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    103,
                    mainIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle(dateTitle)
                    .setContentText(primaryLine)
                    .setStyle(
                        NotificationCompat.BigTextStyle()
                            .setBigContentTitle(dateTitle)
                            .setSummaryText("وضعیت کلی اپلیکیشن")
                            .bigText(bigTextContent)
                    )
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setOngoing(true)
                    .setShowWhen(false)
                    .setOnlyAlertOnce(true)
                    .setContentIntent(pendingIntent)
                    .build()

                notificationManager.notify(NOTIFICATION_ID, notification)
            } catch (_: Throwable) {}
        }
    }

    fun cancelStatusNotification(context: Context) {
        try {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(NOTIFICATION_ID)
        } catch (_: Throwable) {}
    }

    private fun calculateNextAlarmInfo(activeAlarms: List<AlarmEntity>, language: AppLanguage): String {
        if (activeAlarms.isEmpty()) {
            return "⏰ آلارم بعدی: هیچ آلارمی فعال نیست"
        }

        val now = Calendar.getInstance()
        val currentHour = now.get(Calendar.HOUR_OF_DAY)
        val currentMinute = now.get(Calendar.MINUTE)
        val currentMinutesOfDay = currentHour * 60 + currentMinute

        var minDiffMinutes = Int.MAX_VALUE
        var bestAlarm: AlarmEntity? = null

        for (alarm in activeAlarms) {
            val alarmMinutesOfDay = alarm.hour * 60 + alarm.minute
            var diff = alarmMinutesOfDay - currentMinutesOfDay
            if (diff <= 0) {
                diff += 24 * 60
            }
            if (diff < minDiffMinutes) {
                minDiffMinutes = diff
                bestAlarm = alarm
            }
        }

        if (bestAlarm == null) {
            return "⏰ آلارم بعدی: هیچ آلارمی فعال نیست"
        }

        val hoursRemaining = minDiffMinutes / 60
        val minsRemaining = minDiffMinutes % 60
        val timeLabel = String.format("%02d:%02d", bestAlarm.hour, bestAlarm.minute)
        val formattedTime = Strings.formatNumber(timeLabel, language)

        val diffText = if (hoursRemaining > 0) {
            "${Strings.formatNumber(hoursRemaining, language)} ساعت و ${Strings.formatNumber(minsRemaining, language)} دقیقه دیگر"
        } else {
            "${Strings.formatNumber(minsRemaining, language)} دقیقه دیگر"
        }

        val labelStr = if (bestAlarm.label.isNotBlank()) " (${bestAlarm.label})" else ""
        return "⏰ آلارم بعدی: $diffText (ساعت $formattedTime)$labelStr"
    }

    private fun formatSeconds(seconds: Long, language: AppLanguage): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        val text = if (h > 0) {
            String.format("%02d:%02d:%02d", h, m, s)
        } else {
            String.format("%02d:%02d", m, s)
        }
        return Strings.formatNumber(text, language)
    }
}
