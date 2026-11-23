package com.example.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.alarm.AlarmScheduler
import com.example.data.local.AppDatabase
import com.example.data.local.SettingsManager
import com.example.service.AlarmAlertService
import com.example.service.AlarmNotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val alarmId = intent.getLongExtra("EXTRA_ALARM_ID", 0L)
        val label = intent.getStringExtra("EXTRA_ALARM_LABEL") ?: "هشدار"
        val soundId = intent.getStringExtra("EXTRA_SOUND_ID") ?: "cyber_pulse"
        val vibration = intent.getStringExtra("EXTRA_VIBRATION") ?: "INTERMITTENT"
        val isTimer = intent.getBooleanExtra("EXTRA_IS_TIMER", false)

        // Cancel notification
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        nm?.cancel(AlarmNotificationHelper.NOTIFICATION_ID)

        // Stop Foreground Alert Service
        val stopIntent = Intent(context, AlarmAlertService::class.java).apply {
            this.action = AlarmAlertService.ACTION_STOP
        }
        context.stopService(stopIntent)

        // Broadcast to close alert activity if open
        try {
            val closeIntent = Intent(AlarmAlertService.ACTION_CLOSE_ALERT_UI)
            context.sendBroadcast(closeIntent)
        } catch (_: Throwable) {}

        if (action == ACTION_DISMISS) {
            // Apply auto-delete after single use upon user dismiss
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getInstance(context)
                    if (isTimer) {
                        val timerDao = db.timerDao()
                        val timer = timerDao.getTimerById(alarmId)
                        if (timer != null) {
                            if (timer.autoDeleteAfterUse) {
                                timerDao.delete(timer)
                            } else {
                                timerDao.update(timer.copy(isRunning = false, remainingSeconds = 0, targetEndTimeMillis = 0L))
                            }
                        }
                    } else if (alarmId > 0) {
                        val alarmDao = db.alarmDao()
                        val alarm = alarmDao.getAlarmById(alarmId)
                        if (alarm != null) {
                            if (alarm.autoDeleteAfterUse) {
                                alarmDao.delete(alarm)
                            } else if (alarm.repeatType == "ONCE") {
                                alarmDao.update(alarm.copy(isEnabled = false))
                            }
                        }
                    }
                } catch (_: Throwable) {}
            }
        } else if (action == ACTION_SNOOZE) {
            val settings = SettingsManager.getInstance(context).settings.value
            val snoozeMinutes = settings.snoozeDurationMinutes.coerceAtLeast(1)
            AlarmScheduler.scheduleSnooze(
                context = context,
                title = label,
                soundId = soundId,
                vibration = vibration,
                snoozeMinutes = snoozeMinutes,
                isTimer = isTimer
            )
        }
    }

    companion object {
        const val ACTION_DISMISS = "com.example.receiver.ACTION_DISMISS"
        const val ACTION_SNOOZE = "com.example.receiver.ACTION_SNOOZE"
    }
}
