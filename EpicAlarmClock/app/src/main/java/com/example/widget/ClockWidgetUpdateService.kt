package com.example.widget

import android.app.Service
import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.os.PowerManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class ClockWidgetUpdateService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Default)
    private var tickerJob: Job? = null
    private var isScreenInteractive = true

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_SCREEN_ON, Intent.ACTION_USER_PRESENT -> {
                    isScreenInteractive = true
                    startTicker()
                }
                Intent.ACTION_SCREEN_OFF -> {
                    isScreenInteractive = false
                    stopTicker()
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_USER_PRESENT)
        }
        registerReceiver(screenReceiver, filter)

        val pm = getSystemService(Context.POWER_SERVICE) as? PowerManager
        isScreenInteractive = pm?.isInteractive ?: true
        if (isScreenInteractive) {
            startTicker()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val appWidgetManager = AppWidgetManager.getInstance(this)
        val hasClockWidgets = appWidgetManager.getAppWidgetIds(ComponentName(this, AnalogClockWidgetProvider::class.java)).isNotEmpty()
        val hasTripleDateWidgets = appWidgetManager.getAppWidgetIds(ComponentName(this, TripleDateWidgetProvider::class.java)).isNotEmpty()

        if (!hasClockWidgets && !hasTripleDateWidgets) {
            stopSelf()
            return START_NOT_STICKY
        }

        if (isScreenInteractive && (tickerJob == null || tickerJob?.isActive != true)) {
            startTicker()
        }
        return START_STICKY
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = serviceScope.launch {
            // Immediately draw current state for both widgets and persistent status card
            AnalogClockWidgetProvider.updateAllWidgets(this@ClockWidgetUpdateService)
            TripleDateWidgetProvider.updateAllWidgets(this@ClockWidgetUpdateService)
            com.example.service.PersistentStatusNotificationHelper.updateStatusNotification(this@ClockWidgetUpdateService)

            while (isActive && isScreenInteractive) {
                val now = System.currentTimeMillis()
                val delayMs = 1000L - (now % 1000L)
                delay(delayMs.coerceIn(100L, 1000L))

                if (isActive && isScreenInteractive) {
                    val appWidgetManager = AppWidgetManager.getInstance(this@ClockWidgetUpdateService)
                    val clockIds = appWidgetManager.getAppWidgetIds(
                        ComponentName(this@ClockWidgetUpdateService, AnalogClockWidgetProvider::class.java)
                    )
                    val tripleDateIds = appWidgetManager.getAppWidgetIds(
                        ComponentName(this@ClockWidgetUpdateService, TripleDateWidgetProvider::class.java)
                    )

                    if (clockIds.isEmpty() && tripleDateIds.isEmpty()) {
                        stopSelf()
                        break
                    }

                    for (id in clockIds) {
                        AnalogClockWidgetProvider.updateAppWidget(this@ClockWidgetUpdateService, appWidgetManager, id)
                    }

                    // On minute boundary (seconds == 0), refresh TripleDateWidgetProvider and Persistent Status Card
                    val currentSec = (System.currentTimeMillis() / 1000) % 60
                    if (currentSec == 0L) {
                        for (id in tripleDateIds) {
                            TripleDateWidgetProvider.updateAppWidget(this@ClockWidgetUpdateService, appWidgetManager, id)
                        }
                        com.example.service.PersistentStatusNotificationHelper.updateStatusNotification(this@ClockWidgetUpdateService)
                    }
                }
            }
        }
    }

    private fun stopTicker() {
        tickerJob?.cancel()
        tickerJob = null
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTicker()
        try {
            unregisterReceiver(screenReceiver)
        } catch (_: Throwable) {}
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        fun start(context: Context) {
            try {
                val intent = Intent(context, ClockWidgetUpdateService::class.java)
                context.startService(intent)
            } catch (_: Throwable) {}
        }

        fun stop(context: Context) {
            try {
                val intent = Intent(context, ClockWidgetUpdateService::class.java)
                context.stopService(intent)
            } catch (_: Throwable) {}
        }
    }
}
