package com.example.receiver

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import com.example.alarm.AlarmScheduler
import com.example.data.local.AppDatabase
import com.example.service.AlarmAlertService
import com.example.service.AlarmNotificationHelper
import com.example.ui.alarm.AlarmAlertActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class AlarmReceiver : android.content.BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getLongExtra("EXTRA_ALARM_ID", 0L)
        val label = intent.getStringExtra("EXTRA_ALARM_LABEL") ?: "هشدار بیدارباش"
        val soundId = intent.getStringExtra("EXTRA_SOUND_ID") ?: "cyber_pulse"
        val vibration = intent.getStringExtra("EXTRA_VIBRATION") ?: "INTERMITTENT"
        val isTimer = intent.getBooleanExtra("EXTRA_IS_TIMER", false)
        val autoDelete = intent.getBooleanExtra("EXTRA_AUTO_DELETE", false)

        // 1. Post notification immediately
        val notification = AlarmNotificationHelper.buildAlarmNotification(
            context = context,
            title = label,
            isTimer = isTimer,
            alarmId = alarmId,
            soundId = soundId,
            vibration = vibration
        )
        try {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            nm?.notify(AlarmNotificationHelper.NOTIFICATION_ID, notification)
        } catch (_: Throwable) {}

        // 2. Launch Fullscreen Alert Activity
        val alertIntent = Intent(context, AlarmAlertActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_NO_USER_ACTION
            putExtra("EXTRA_ALARM_ID", alarmId)
            putExtra("EXTRA_ALARM_LABEL", label)
            putExtra("EXTRA_SOUND_ID", soundId)
            putExtra("EXTRA_VIBRATION", vibration)
            putExtra("EXTRA_IS_TIMER", isTimer)
        }
        try {
            context.startActivity(alertIntent)
        } catch (_: Throwable) {}

        // 3. Start Foreground Alert Service for reliable sound, vibration, and wake lock
        val serviceIntent = Intent(context, AlarmAlertService::class.java).apply {
            action = AlarmAlertService.ACTION_START
            putExtra("EXTRA_ALARM_ID", alarmId)
            putExtra("EXTRA_ALARM_LABEL", label)
            putExtra("EXTRA_SOUND_ID", soundId)
            putExtra("EXTRA_VIBRATION", vibration)
            putExtra("EXTRA_IS_TIMER", isTimer)
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        } catch (_: Throwable) {
            try {
                context.startService(serviceIntent)
            } catch (_: Throwable) {}
        }

        // 4. Update Database: Auto-delete or re-schedule next repeat
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context)
                if (isTimer) {
                    val timerDao = db.timerDao()
                    val allTimers = timerDao.getAllTimers().firstOrNull() ?: emptyList()
                    val timer = allTimers.find { it.id == alarmId }
                    if (timer != null) {
                        timerDao.update(timer.copy(isRunning = false, remainingSeconds = 0, targetEndTimeMillis = 0L))
                    }
                } else if (alarmId > 0) {
                    val alarmDao = db.alarmDao()
                    val allAlarms = alarmDao.getAllAlarms().firstOrNull() ?: emptyList()
                    val alarm = allAlarms.find { it.id == alarmId }
                    if (alarm != null) {
                        val cleaned = if (alarm.skippedUntilMillis in 1..System.currentTimeMillis()) {
                            alarm.copy(skippedUntilMillis = 0L).also { alarmDao.update(it) }
                        } else {
                            alarm
                        }
                        if (cleaned.repeatType != "ONCE") {
                            // Repeating alarm (DAILY, SAT_THU, MON_FRI, CUSTOM, SHIFT)
                            // Schedule the next occurrence automatically!
                            AlarmScheduler.scheduleAlarm(context, cleaned)
                        }
                    }
                }
            } catch (_: Throwable) {}
        }
    }
}
