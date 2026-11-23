package com.example

import android.Manifest
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.core.content.ContextCompat
import com.example.alarm.AlarmScheduler
import com.example.service.AlarmNotificationHelper
import com.example.ui.MainScreen
import com.example.ui.MainViewModel
import com.example.ui.theme.EpicAlarmClockTheme
import com.example.widget.AnalogClockWidgetProvider
import com.example.widget.ClockWidgetUpdateService

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Create notification channel
        AlarmNotificationHelper.createNotificationChannel(this)

        // Reschedule all active alarms and timers to guarantee background execution
        AlarmScheduler.rescheduleAllAlarmsAndTimers(this)

        // Start widget ticker if any widget exists on the home screen
        try {
            com.example.widget.TripleDateWidgetProvider.updateAllWidgets(this)
            val appWidgetManager = AppWidgetManager.getInstance(this)
            val ids = appWidgetManager.getAppWidgetIds(ComponentName(this, AnalogClockWidgetProvider::class.java))
            if (ids.isNotEmpty()) {
                ClockWidgetUpdateService.start(this)
            }
        } catch (_: Throwable) {}


        // Request POST_NOTIFICATIONS on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            val themeMode by viewModel.currentThemeMode.collectAsState()
            val appSettings by viewModel.appSettings.collectAsState()
            val currentLanguage by viewModel.currentLanguage.collectAsState()

            var isAppUnlocked by androidx.compose.runtime.saveable.rememberSaveable {
                androidx.compose.runtime.mutableStateOf(!appSettings.appLockEnabled)
            }

            androidx.compose.runtime.LaunchedEffect(appSettings.appLockEnabled) {
                if (!appSettings.appLockEnabled) {
                    isAppUnlocked = true
                }
            }

            val fontScaleMultiplier = when (appSettings.fontSizeScale) {
                "SMALL" -> 0.85f
                "NORMAL" -> 1.0f
                "LARGE" -> 1.15f
                "LARGER" -> 1.30f
                else -> 1.0f
            }

            val layoutDirection = if (currentLanguage == com.example.i18n.AppLanguage.PERSIAN || currentLanguage == com.example.i18n.AppLanguage.ARABIC) {
                androidx.compose.ui.unit.LayoutDirection.Rtl
            } else {
                androidx.compose.ui.unit.LayoutDirection.Ltr
            }

            val currentDensity = LocalDensity.current
            CompositionLocalProvider(
                androidx.compose.ui.platform.LocalLayoutDirection provides layoutDirection,
                LocalDensity provides Density(
                    density = currentDensity.density,
                    fontScale = currentDensity.fontScale * fontScaleMultiplier
                )
            ) {
                EpicAlarmClockTheme(themeMode = themeMode) {
                    if (!appSettings.onboardingCompleted) {
                        com.example.ui.onboarding.OnboardingScreen(
                            onFinish = { /* Flow updates automatically via settings */ }
                        )
                    } else if (appSettings.appLockEnabled && !isAppUnlocked) {
                        com.example.ui.security.AppLockScreen(
                            appSettings = appSettings,
                            onUnlocked = { isAppUnlocked = true }
                        )
                    } else {
                        MainScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            com.example.service.PersistentStatusNotificationHelper.updateStatusNotification(this)
        } catch (_: Throwable) {}
        try {
            val settings = com.example.data.local.SettingsManager.getInstance(this).getSettings()
            if (settings.gyroscopeLoggerEnabled) {
                com.example.sensor.GyroscopeMotionTracker.startTracking(this)
            } else {
                com.example.sensor.GyroscopeMotionTracker.stopTracking()
            }
        } catch (_: Throwable) {}
    }

    override fun onPause() {
        super.onPause()
        // Save battery when leaving app if tracking was active
        try {
            com.example.sensor.GyroscopeMotionTracker.stopTracking()
        } catch (_: Throwable) {}
    }
}
