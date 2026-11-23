package com.example.sensor

import android.content.Context
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.sqrt

/**
 * Tracks device motion events via Gyroscope and Accelerometer,
 * and maintains history of missed alarms in the last 24 hours for Guardian Alert.
 */
object GyroscopeMotionTracker {

    private const val PREFS_NAME = "epic_motion_tracker_prefs"
    private const val KEY_LAST_MOTION_TIME = "key_last_motion_timestamp"
    private const val KEY_MISSED_ALARMS_CSV = "key_missed_alarms_csv"

    private var activeSensorManager: SensorManager? = null
    private var activeListener: SensorEventListener? = null

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Record a motion event timestamp
     */
    fun recordMotion(context: Context, timestamp: Long = System.currentTimeMillis()) {
        getPrefs(context).edit().putLong(KEY_LAST_MOTION_TIME, timestamp).apply()
    }

    fun getLastMotionTimestamp(context: Context): Long {
        val saved = getPrefs(context).getLong(KEY_LAST_MOTION_TIME, 0L)
        return if (saved == 0L) System.currentTimeMillis() else saved
    }

    /**
     * Formats last motion time in Persian friendly format (e.g., 14:35 امروز)
     */
    fun getLastMotionTimeFormatted(context: Context): String {
        val lastTime = getLastMotionTimestamp(context)
        val now = System.currentTimeMillis()
        val diffHours = (now - lastTime) / (1000 * 60 * 60)

        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val timeStr = timeFormat.format(Date(lastTime))

        return if (diffHours < 24) {
            "ساعت $timeStr (امروز)"
        } else {
            val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
            dateFormat.format(Date(lastTime))
        }
    }

    /**
     * Calculates duration the phone has remained motionless
     */
    fun getMotionlessDurationSummary(context: Context): String {
        val lastTime = getLastMotionTimestamp(context)
        val elapsedMs = (System.currentTimeMillis() - lastTime).coerceAtLeast(0L)
        val totalMinutes = elapsedMs / (1000 * 60)

        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60

        return when {
            hours > 0 && minutes > 0 -> "$hours ساعت و $minutes دقیقه"
            hours > 0 -> "$hours ساعت"
            minutes > 0 -> "$minutes دقیقه"
            else -> "کمتر از ۱ دقیقه"
        }
    }

    /**
     * Records a missed alarm occurrence with current timestamp
     */
    fun recordMissedAlarm(context: Context) {
        val prefs = getPrefs(context)
        val existingCsv = prefs.getString(KEY_MISSED_ALARMS_CSV, "") ?: ""
        val now = System.currentTimeMillis()

        val list = existingCsv.split(",")
            .mapNotNull { it.toLongOrNull() }
            .filter { now - it <= 24 * 60 * 60 * 1000L }
            .toMutableList()

        list.add(now)
        prefs.edit().putString(KEY_MISSED_ALARMS_CSV, list.joinToString(",")).apply()
    }

    /**
     * Returns count of missed alarms in the preceding 24 hours
     */
    fun getMissedAlarmsLast24Hours(context: Context): Int {
        val prefs = getPrefs(context)
        val existingCsv = prefs.getString(KEY_MISSED_ALARMS_CSV, "") ?: ""
        val now = System.currentTimeMillis()

        val list = existingCsv.split(",")
            .mapNotNull { it.toLongOrNull() }
            .filter { now - it <= 24 * 60 * 60 * 1000L }

        return list.size
    }

    /**
     * Starts continuous gyroscope motion tracking if enabled by user
     */
    fun startTracking(context: Context) {
        stopTracking()
        val sm = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager ?: return
        val gyro = sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
            ?: sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) ?: return

        activeSensorManager = sm
        val listener = object : SensorEventListener {
            private var lastLog = 0L

            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return
                val now = System.currentTimeMillis()
                // Throttle updates to at most once every 3 seconds to save battery
                if (now - lastLog < 3000L) return

                val isGyro = event.sensor.type == Sensor.TYPE_GYROSCOPE
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                val magnitude = sqrt((x * x + y * y + z * z).toDouble()).toFloat()

                val moved = if (isGyro) {
                    magnitude > 0.45f // Angular rotation detected
                } else {
                    kotlin.math.abs(magnitude - SensorManager.GRAVITY_EARTH) > 2.0f
                }

                if (moved) {
                    lastLog = now
                    recordMotion(context, now)
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        activeListener = listener
        sm.registerListener(listener, gyro, SensorManager.SENSOR_DELAY_NORMAL)
    }

    fun stopTracking() {
        try {
            activeListener?.let { activeSensorManager?.unregisterListener(it) }
        } catch (_: Throwable) {}
        activeListener = null
        activeSensorManager = null
    }
}
