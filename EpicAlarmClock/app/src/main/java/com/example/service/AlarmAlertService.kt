package com.example.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import com.example.audio.SoundAndVibrationManager
import com.example.data.local.SettingsManager
import kotlinx.coroutines.launch
import java.util.Calendar

class AlarmAlertService : Service() {

    private var wakeLock: PowerManager.WakeLock? = null
    private var screenOffReceiver: android.content.BroadcastReceiver? = null
    private var shakeDetector: com.example.sensor.ShakeDetector? = null
    private var presenceDetector: com.example.sensor.PresenceDetector? = null
    private var flipToSnoozeDetector: com.example.sensor.FlipToSnoozeDetector? = null
    private var guardianJob: kotlinx.coroutines.Job? = null
    private val serviceScope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Default)

    data class ActiveAlertData(
        val alarmId: Long,
        val title: String,
        val isTimer: Boolean,
        val soundId: String,
        val vibration: String
    )

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action

        if (action == ACTION_STOP) {
            SoundAndVibrationManager.stopAlarm(this)
            stopSelf()
            return START_NOT_STICKY
        }

        val alarmId = intent?.getLongExtra("EXTRA_ALARM_ID", 0L) ?: 0L
        val title = intent?.getStringExtra("EXTRA_ALARM_LABEL") ?: "هشدار بیدارباش"
        val soundId = intent?.getStringExtra("EXTRA_SOUND_ID") ?: "cyber_pulse"
        val vibration = intent?.getStringExtra("EXTRA_VIBRATION") ?: "INTERMITTENT"
        val isTimer = intent?.getBooleanExtra("EXTRA_IS_TIMER", false) ?: false

        _activeAlertFlow.value = ActiveAlertData(
            alarmId = alarmId,
            title = title,
            isTimer = isTimer,
            soundId = soundId,
            vibration = vibration
        )

        // Refresh widgets state on home screen (1x1 Quick Stop and Analog Clock with alert badge)
        try {
            com.example.widget.QuickStopWidgetProvider.updateAllWidgets(this)
            com.example.widget.AnalogClockWidgetProvider.updateAllWidgets(this)
        } catch (_: Throwable) {}

        setupScreenOffReceiver()

        val settings = SettingsManager.getInstance(applicationContext).settings.value

        // Setup Shake to Stop if enabled
        if (settings.shakeToStopEnabled) {
            shakeDetector = com.example.sensor.ShakeDetector(this, settings.shakeSensitivity) {
                dismissCurrentAlert()
            }
            shakeDetector?.start()
        }

        // Setup Presence / Pickup detection to reduce volume by 75%
        if (settings.presenceAutoLowerEnabled) {
            presenceDetector = com.example.sensor.PresenceDetector(this) {
                SoundAndVibrationManager.reduceVolumeToQuarter()
            }
            presenceDetector?.start()
        }

        // Setup Flip-to-Snooze when phone is turned screen-down
        if (settings.flipToSnoozeEnabled) {
            flipToSnoozeDetector?.stop()
            flipToSnoozeDetector = com.example.sensor.FlipToSnoozeDetector(this) {
                snoozeCurrentAlert()
            }
            flipToSnoozeDetector?.start()
        }

        // Setup Guardian Alert secondary SMS trigger if not dismissed within N minutes
        if (settings.guardianAlertEnabled && settings.guardianPhoneNumber.isNotBlank()) {
            val timeoutMs = (settings.guardianTimeoutMinutes.coerceAtLeast(1) * 60 * 1000L)
            guardianJob = serviceScope.launch {
                kotlinx.coroutines.delay(timeoutMs)
                if (_activeAlertFlow.value != null && SoundAndVibrationManager.isAlarmPlaying()) {
                    com.example.guardian.GuardianAlertManager.sendEmergencyAlert(
                        context = applicationContext,
                        settings = settings
                    )
                }
            }
        }

        // Acquire WakeLock
        try {
            val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP, "EpicAlarm:AlertWakeLock").apply {
                acquire(10 * 60 * 1000L) // 10 minutes max
            }
        } catch (_: Throwable) {}

        // Build notification
        val notification = AlarmNotificationHelper.buildAlarmNotification(
            context = this,
            title = title,
            isTimer = isTimer,
            alarmId = alarmId,
            soundId = soundId,
            vibration = vibration
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    AlarmNotificationHelper.NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                )
            } else {
                startForeground(AlarmNotificationHelper.NOTIFICATION_ID, notification)
            }
        } catch (_: Throwable) {
            startForeground(AlarmNotificationHelper.NOTIFICATION_ID, notification)
        }

        // Direct Activity Launch for Samsung / Lockscreen pop-up
        try {
            val fullScreenIntent = Intent(this, com.example.ui.alarm.AlarmAlertActivity::class.java).apply {
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_NO_USER_ACTION or
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                )
                putExtra("EXTRA_ALARM_ID", alarmId)
                putExtra("EXTRA_ALARM_LABEL", title)
                putExtra("EXTRA_SOUND_ID", soundId)
                putExtra("EXTRA_VIBRATION", vibration)
                putExtra("EXTRA_IS_TIMER", isTimer)
            }
            startActivity(fullScreenIntent)
        } catch (_: Throwable) {}

        // Fetch user volume & auto silence settings
        val volume = if (isTimer) settings.timerVolume else settings.alarmVolume
        val autoSilenceMinutes = settings.autoSilenceMinutes

        // Start playback
        SoundAndVibrationManager.playAlarm(
            context = this,
            soundIdOrUri = soundId,
            vibrationPattern = vibration,
            volume = volume,
            autoSilenceMinutes = autoSilenceMinutes,
            onAutoSilenced = {
                // Record missed alarm for 24h history
                com.example.sensor.GyroscopeMotionTracker.recordMissedAlarm(applicationContext)

                // When auto-silenced due to no response:
                // 1. Post Missed Alarm Notification
                val now = Calendar.getInstance()
                val currentHour = now.get(Calendar.HOUR_OF_DAY)
                val currentMinute = now.get(Calendar.MINUTE)
                val timeStr = String.format("%02d:%02d", currentHour, currentMinute)

                AlarmNotificationHelper.showMissedAlarmNotification(
                    context = applicationContext,
                    alarmTimeStr = timeStr,
                    label = title,
                    isTimer = isTimer
                )

                // 2. Broadcast to dismiss the fullscreen UI
                try {
                    val closeIntent = Intent(ACTION_CLOSE_ALERT_UI)
                    sendBroadcast(closeIntent)
                } catch (_: Throwable) {}

                // 3. Stop this alert service
                stopSelf()
            }
        )

        return START_NOT_STICKY
    }

    private fun setupScreenOffReceiver() {
        if (screenOffReceiver == null) {
            screenOffReceiver = object : android.content.BroadcastReceiver() {
                private var powerPressTimes = mutableListOf<Long>()
                override fun onReceive(context: Context?, intent: Intent?) {
                    if (intent?.action == Intent.ACTION_SCREEN_OFF || intent?.action == Intent.ACTION_SCREEN_ON) {
                        val settings = SettingsManager.getInstance(applicationContext).settings.value
                        if (settings.dismissPowerEnabled) {
                            val now = System.currentTimeMillis()
                            powerPressTimes.add(now)
                            // Keep events within last 3 seconds
                            powerPressTimes.removeAll { now - it > 3000 }
                            val requiredCount = settings.dismissPowerPressCount.coerceAtLeast(1)
                            if (powerPressTimes.size >= requiredCount) {
                                powerPressTimes.clear()
                                dismissCurrentAlert()
                            }
                        }
                    }
                }
            }
            val filter = android.content.IntentFilter().apply {
                addAction(Intent.ACTION_SCREEN_OFF)
                addAction(Intent.ACTION_SCREEN_ON)
            }
            registerReceiver(screenOffReceiver, filter)
        }
    }

    private fun dismissCurrentAlert() {
        val current = _activeAlertFlow.value
        val dismissIntent = Intent(this, com.example.receiver.AlarmActionReceiver::class.java).apply {
            action = com.example.receiver.AlarmActionReceiver.ACTION_DISMISS
            if (current != null) {
                putExtra("EXTRA_ALARM_ID", current.alarmId)
                putExtra("EXTRA_IS_TIMER", current.isTimer)
                putExtra("EXTRA_ALARM_LABEL", current.title)
                putExtra("EXTRA_SOUND_ID", current.soundId)
                putExtra("EXTRA_VIBRATION", current.vibration)
            }
        }
        sendBroadcast(dismissIntent)
    }

    private fun snoozeCurrentAlert() {
        val current = _activeAlertFlow.value
        val snoozeIntent = Intent(this, com.example.receiver.AlarmActionReceiver::class.java).apply {
            action = com.example.receiver.AlarmActionReceiver.ACTION_SNOOZE
            if (current != null) {
                putExtra("EXTRA_ALARM_ID", current.alarmId)
                putExtra("EXTRA_IS_TIMER", current.isTimer)
                putExtra("EXTRA_ALARM_LABEL", current.title)
                putExtra("EXTRA_SOUND_ID", current.soundId)
                putExtra("EXTRA_VIBRATION", current.vibration)
            }
        }
        sendBroadcast(snoozeIntent)
    }

    override fun onDestroy() {
        super.onDestroy()
        SoundAndVibrationManager.stopAlarm(this)
        _activeAlertFlow.value = null

        try {
            shakeDetector?.stop()
            shakeDetector = null
        } catch (_: Throwable) {}

        try {
            presenceDetector?.stop()
            presenceDetector = null
        } catch (_: Throwable) {}

        try {
            flipToSnoozeDetector?.stop()
            flipToSnoozeDetector = null
        } catch (_: Throwable) {}

        try {
            guardianJob?.cancel()
            guardianJob = null
        } catch (_: Throwable) {}

        try {
            com.example.widget.QuickStopWidgetProvider.updateAllWidgets(this)
            com.example.widget.AnalogClockWidgetProvider.updateAllWidgets(this)
        } catch (_: Throwable) {}

        try {
            if (screenOffReceiver != null) {
                unregisterReceiver(screenOffReceiver)
                screenOffReceiver = null
            }
        } catch (_: Throwable) {}
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (_: Throwable) {}
        wakeLock = null
    }

    companion object {
        const val ACTION_STOP = "com.example.service.ACTION_STOP"
        const val ACTION_START = "com.example.service.ACTION_START"
        const val ACTION_CLOSE_ALERT_UI = "com.example.action.CLOSE_ALERT_UI"

        private val _activeAlertFlow = kotlinx.coroutines.flow.MutableStateFlow<ActiveAlertData?>(null)
        val activeAlertFlow: kotlinx.coroutines.flow.StateFlow<ActiveAlertData?> = _activeAlertFlow
    }
}
