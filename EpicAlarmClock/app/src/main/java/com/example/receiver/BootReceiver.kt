package com.example.receiver

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.example.alarm.AlarmScheduler
import com.example.widget.AnalogClockWidgetProvider
import com.example.widget.ClockWidgetUpdateService

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == "android.intent.action.QUICKBOOT_POWERON" ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            action == Intent.ACTION_LOCKED_BOOT_COMPLETED
        ) {
            // Re-schedule all active alarms and running timers
            AlarmScheduler.rescheduleAllAlarmsAndTimers(context)

            // Update widgets
            try {
                com.example.widget.TripleDateWidgetProvider.updateAllWidgets(context)
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val ids = appWidgetManager.getAppWidgetIds(ComponentName(context, AnalogClockWidgetProvider::class.java))
                if (ids.isNotEmpty()) {
                    ClockWidgetUpdateService.start(context)
                }
            } catch (_: Throwable) {}

            // Update persistent status notification card
            try {
                com.example.service.PersistentStatusNotificationHelper.updateStatusNotification(context)
            } catch (_: Throwable) {}
        }
    }
}
