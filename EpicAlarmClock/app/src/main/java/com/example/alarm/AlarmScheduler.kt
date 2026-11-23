package com.example.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.calendar.TripleCalendarHelper
import com.example.data.local.AppDatabase
import com.example.data.model.AlarmEntity
import com.example.data.model.TimerEntity
import com.example.receiver.AlarmReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.Calendar

object AlarmScheduler {

    fun scheduleAlarm(context: Context, alarm: AlarmEntity) {
        if (!alarm.isEnabled) {
            cancelAlarm(context, alarm.id)
            return
        }

        val triggerMillis = calculateNextTriggerMillis(alarm)
        if (triggerMillis <= System.currentTimeMillis()) {
            cancelAlarm(context, alarm.id)
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "com.example.ALARM_TRIGGER"
            putExtra("EXTRA_ALARM_ID", alarm.id)
            putExtra("EXTRA_ALARM_LABEL", alarm.label)
            putExtra("EXTRA_SOUND_ID", alarm.soundId)
            putExtra("EXTRA_VIBRATION", alarm.vibrationPattern)
            putExtra("EXTRA_IS_TIMER", false)
            putExtra("EXTRA_AUTO_DELETE", alarm.autoDeleteAfterUse)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Show intent for AlarmClockInfo (opens app when user taps alarm clock in system UI)
        val showIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val showPendingIntent = PendingIntent.getActivity(
            context,
            (alarm.id + 10000).toInt(),
            showIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        setAlarmExactOrFallback(alarmManager, triggerMillis, pendingIntent, showPendingIntent)
        com.example.service.PersistentStatusNotificationHelper.updateStatusNotification(context)
    }

    fun scheduleTimer(context: Context, timer: TimerEntity) {
        if (!timer.isRunning || timer.targetEndTimeMillis <= System.currentTimeMillis()) {
            cancelTimer(context, timer.id)
            return
        }

        val triggerMillis = timer.targetEndTimeMillis
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "com.example.ALARM_TRIGGER"
            putExtra("EXTRA_ALARM_ID", timer.id)
            putExtra("EXTRA_ALARM_LABEL", timer.label.ifBlank { "تایمر" })
            putExtra("EXTRA_SOUND_ID", timer.soundId)
            putExtra("EXTRA_VIBRATION", timer.vibrationPattern)
            putExtra("EXTRA_IS_TIMER", true)
            putExtra("EXTRA_AUTO_DELETE", timer.autoDeleteAfterUse)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            (timer.id + 50000).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val showIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val showPendingIntent = PendingIntent.getActivity(
            context,
            (timer.id + 60000).toInt(),
            showIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        setAlarmExactOrFallback(alarmManager, triggerMillis, pendingIntent, showPendingIntent)
        com.example.service.PersistentStatusNotificationHelper.updateStatusNotification(context)
    }

    fun scheduleSnooze(context: Context, title: String, soundId: String, vibration: String, snoozeMinutes: Int, isTimer: Boolean) {
        val triggerMillis = System.currentTimeMillis() + (snoozeMinutes * 60 * 1000L)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val snoozeId = 99999
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "com.example.ALARM_TRIGGER"
            putExtra("EXTRA_ALARM_ID", snoozeId.toLong())
            putExtra("EXTRA_ALARM_LABEL", title)
            putExtra("EXTRA_SOUND_ID", soundId)
            putExtra("EXTRA_VIBRATION", vibration)
            putExtra("EXTRA_IS_TIMER", isTimer)
            putExtra("EXTRA_AUTO_DELETE", false)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            snoozeId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val showIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val showPendingIntent = PendingIntent.getActivity(
            context,
            snoozeId + 1,
            showIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        setAlarmExactOrFallback(alarmManager, triggerMillis, pendingIntent, showPendingIntent)
        com.example.service.PersistentStatusNotificationHelper.updateStatusNotification(context)
    }

    fun cancelAlarm(context: Context, alarmId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "com.example.ALARM_TRIGGER"
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        try {
            alarmManager.cancel(pendingIntent)
        } catch (_: Throwable) {}
        com.example.service.PersistentStatusNotificationHelper.updateStatusNotification(context)
    }

    fun cancelTimer(context: Context, timerId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "com.example.ALARM_TRIGGER"
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            (timerId + 50000).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        try {
            alarmManager.cancel(pendingIntent)
        } catch (_: Throwable) {}
        com.example.service.PersistentStatusNotificationHelper.updateStatusNotification(context)
    }

    private fun setAlarmExactOrFallback(
        alarmManager: AlarmManager,
        triggerMillis: Long,
        pendingIntent: PendingIntent,
        showPendingIntent: PendingIntent
    ) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerMillis, showPendingIntent)
                alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
            }
        } catch (_: SecurityException) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
                }
            } catch (_: Throwable) {}
        } catch (_: Throwable) {}
    }

    fun rescheduleAllAlarmsAndTimers(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context)
                val allAlarms = db.alarmDao().getAllAlarms().firstOrNull() ?: emptyList()
                for (alarm in allAlarms) {
                    if (alarm.isEnabled) {
                        scheduleAlarm(context, alarm)
                    }
                }

                val allTimers = db.timerDao().getAllTimers().firstOrNull() ?: emptyList()
                val now = System.currentTimeMillis()
                for (timer in allTimers) {
                    if (timer.isRunning) {
                        if (timer.targetEndTimeMillis > now) {
                            scheduleTimer(context, timer)
                        } else {
                            // Already expired while phone was off/app was closed, trigger immediately!
                            val intent = Intent(context, AlarmReceiver::class.java).apply {
                                action = "com.example.ALARM_TRIGGER"
                                putExtra("EXTRA_ALARM_ID", timer.id)
                                putExtra("EXTRA_ALARM_LABEL", timer.label.ifBlank { "تایمر" })
                                putExtra("EXTRA_SOUND_ID", timer.soundId)
                                putExtra("EXTRA_VIBRATION", timer.vibrationPattern)
                                putExtra("EXTRA_IS_TIMER", true)
                                putExtra("EXTRA_AUTO_DELETE", timer.autoDeleteAfterUse)
                            }
                            context.sendBroadcast(intent)
                        }
                    }
                }
            } catch (_: Throwable) {}
        }
    }

    fun calculateNextTriggerMillis(alarm: AlarmEntity, ignoreSkip: Boolean = false): Long {
        val nowMillis = System.currentTimeMillis()
        val effectiveNow = if (!ignoreSkip && alarm.skippedUntilMillis > nowMillis) {
            alarm.skippedUntilMillis
        } else {
            nowMillis
        }

        val target = Calendar.getInstance().apply {
            timeInMillis = effectiveNow
            set(Calendar.HOUR_OF_DAY, alarm.hour)
            set(Calendar.MINUTE, alarm.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        when (alarm.repeatType) {
            "ONCE", "DAILY" -> {
                while (target.timeInMillis <= effectiveNow) {
                    target.add(Calendar.DAY_OF_YEAR, 1)
                }
                return target.timeInMillis
            }
            "SAT_THU" -> {
                while (target.timeInMillis <= effectiveNow || target.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) {
                    target.add(Calendar.DAY_OF_YEAR, 1)
                }
                return target.timeInMillis
            }
            "MON_FRI" -> {
                while (target.timeInMillis <= effectiveNow || target.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || target.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                    target.add(Calendar.DAY_OF_YEAR, 1)
                }
                return target.timeInMillis
            }
            "CUSTOM" -> {
                val daysMap = mapOf(
                    Calendar.SATURDAY to "SAT",
                    Calendar.SUNDAY to "SUN",
                    Calendar.MONDAY to "MON",
                    Calendar.TUESDAY to "TUE",
                    Calendar.WEDNESDAY to "WED",
                    Calendar.THURSDAY to "THU",
                    Calendar.FRIDAY to "FRI"
                )
                while (target.timeInMillis <= effectiveNow) {
                    target.add(Calendar.DAY_OF_YEAR, 1)
                }
                for (i in 0..7) {
                    val code = daysMap[target.get(Calendar.DAY_OF_WEEK)] ?: ""
                    if (alarm.customDays.contains(code)) {
                        return target.timeInMillis
                    }
                    target.add(Calendar.DAY_OF_YEAR, 1)
                }
                return target.timeInMillis
            }
            "SHIFT" -> {
                val startMillis = TripleCalendarHelper.getMillisFromCalendar(
                    calType = alarm.shiftAnchorCalendar,
                    year = alarm.shiftAnchorYear,
                    month = alarm.shiftAnchorMonth,
                    day = alarm.shiftAnchorDay,
                    hour = alarm.hour,
                    minute = alarm.minute
                )
                val intervalVal = alarm.shiftIntervalValue.coerceAtLeast(1)
                val intervalMs: Long = when (alarm.shiftIntervalUnit) {
                    "MINUTES" -> intervalVal * 60 * 1000L
                    "HOURS" -> intervalVal * 3600 * 1000L
                    else -> intervalVal * 24L * 3600 * 1000L // "DAYS"
                }
                if (effectiveNow < startMillis) {
                    return startMillis
                } else {
                    val elapsed = effectiveNow - startMillis
                    val periods = (elapsed / intervalMs) + 1
                    return startMillis + (periods * intervalMs)
                }
            }
            "EVENT", "MEETING" -> {
                val eventYear = if (alarm.eventYear > 0) alarm.eventYear else alarm.shiftAnchorYear
                val eventMonth = if (alarm.eventMonth > 0) alarm.eventMonth else alarm.shiftAnchorMonth
                val eventDay = if (alarm.eventDay > 0) alarm.eventDay else alarm.shiftAnchorDay
                val eventMillis = TripleCalendarHelper.getMillisFromCalendar(
                    calType = alarm.eventCalendar,
                    year = eventYear,
                    month = eventMonth,
                    day = eventDay,
                    hour = alarm.hour,
                    minute = alarm.minute
                )
                return if (!ignoreSkip && alarm.skippedUntilMillis > nowMillis && eventMillis <= effectiveNow) 0L else eventMillis
            }
            else -> {
                while (target.timeInMillis <= effectiveNow) {
                    target.add(Calendar.DAY_OF_YEAR, 1)
                }
                return target.timeInMillis
            }
        }
    }
}
