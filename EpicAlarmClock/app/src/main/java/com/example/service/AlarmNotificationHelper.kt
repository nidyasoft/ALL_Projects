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
import com.example.receiver.AlarmActionReceiver
import com.example.ui.alarm.AlarmAlertActivity

object AlarmNotificationHelper {

    const val CHANNEL_ID = "epic_alarm_alert_channel"
    const val NOTIFICATION_ID = 4040

    const val MISSED_CHANNEL_ID = "epic_missed_alarm_channel"
    const val MISSED_NOTIFICATION_ID = 4041

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "هشدارهای فعال و تمام‌صفحه (Alarms & Timers)"
            val descriptionText = "نمایش نوتیفیکیشن شناور و تمام‌صفحه برای زنگ بیدارباش و پایان تایمر"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
                enableLights(true)
                setBypassDnd(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun createMissedAlarmNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "هشدارهای بدون پاسخ (Missed Alarms)"
            val descriptionText = "اعلان آلارم‌ها و تایمرهایی که به دلیل عدم پاسخ خودکار متوقف شده‌اند"
            val channel = NotificationChannel(MISSED_CHANNEL_ID, name, NotificationManager.IMPORTANCE_HIGH).apply {
                description = descriptionText
                enableVibration(true)
                setShowBadge(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    fun showMissedAlarmNotification(
        context: Context,
        alarmTimeStr: String,
        label: String,
        isTimer: Boolean
    ) {
        try {
            createMissedAlarmNotificationChannel(context)
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            // Cancel active alert notification
            nm.cancel(NOTIFICATION_ID)

            val mainIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                2020,
                mainIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val cleanLabel = label.ifBlank { if (isTimer) "تایمر" else "آلارم" }
            val titleText = if (isTimer) "تایمر بدون پاسخ" else "آلارم بدون پاسخ"
            val bodyText = "شما یک آلارم بدون پاسخ دارید که در ساعت $alarmTimeStr بود و برچسب آلارم «$cleanLabel» بوده است."

            val notification = NotificationCompat.Builder(context, MISSED_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(titleText)
                .setContentText(bodyText)
                .setStyle(NotificationCompat.BigTextStyle().bigText(bodyText))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build()

            nm.notify(MISSED_NOTIFICATION_ID, notification)
        } catch (_: Throwable) {}
    }

    fun buildAlarmNotification(
        context: Context,
        title: String,
        isTimer: Boolean,
        alarmId: Long,
        soundId: String,
        vibration: String
    ): Notification {
        createNotificationChannel(context)

        // Full Screen Intent (Triggers AlarmAlertActivity directly when screen is off/locked)
        val fullScreenIntent = Intent(context, AlarmAlertActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_NO_USER_ACTION
            putExtra("EXTRA_ALARM_ID", alarmId)
            putExtra("EXTRA_ALARM_LABEL", title)
            putExtra("EXTRA_SOUND_ID", soundId)
            putExtra("EXTRA_VIBRATION", vibration)
            putExtra("EXTRA_IS_TIMER", isTimer)
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            alarmId.toInt(),
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Dismiss Action
        val dismissIntent = Intent(context, AlarmActionReceiver::class.java).apply {
            action = AlarmActionReceiver.ACTION_DISMISS
            putExtra("EXTRA_ALARM_ID", alarmId)
            putExtra("EXTRA_IS_TIMER", isTimer)
            putExtra("EXTRA_ALARM_LABEL", title)
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            context,
            (alarmId + 100).toInt(),
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Snooze Action (for BOTH Alarm and Timer)
        val snoozeIntent = Intent(context, AlarmActionReceiver::class.java).apply {
            action = AlarmActionReceiver.ACTION_SNOOZE
            putExtra("EXTRA_ALARM_ID", alarmId)
            putExtra("EXTRA_ALARM_LABEL", title)
            putExtra("EXTRA_SOUND_ID", soundId)
            putExtra("EXTRA_VIBRATION", vibration)
            putExtra("EXTRA_IS_TIMER", isTimer)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            (alarmId + 200).toInt(),
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val subText = if (isTimer) "پایان زمان تایمر" else "زمان بیداری فرارسیده است!"

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("🔔 $title")
            .setContentText(subText)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .setBigContentTitle("🔔 $title")
                    .setSummaryText(if (isTimer) "تایمر" else "آلارم")
                    .bigText("« $title »\n$subText")
            )
            .setSubText("ساعت هشدار اپیک")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setAutoCancel(false)
            .setContentIntent(fullScreenPendingIntent)
            .setFullScreenIntent(fullScreenPendingIntent, true) // Required for lockscreen full-screen popup
            // Action 1: Dismiss (خاموش کردن)
            .addAction(
                android.R.drawable.ic_delete,
                "خاموش کردن (Dismiss)",
                dismissPendingIntent
            )
            // Action 2: Snooze (تعویق)
            .addAction(
                android.R.drawable.ic_popup_sync,
                "تعویق (Snooze)",
                snoozePendingIntent
            )
            .build()
    }
}
