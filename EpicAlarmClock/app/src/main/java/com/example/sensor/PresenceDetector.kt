package com.example.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

/**
 * Detects user presence (proximity) or device pickup (accelerometer) during alarm ringing.
 * When presence or pickup is detected, invokes onPresenceDetected so alarm volume can be
 * lowered by 75% (maintaining 25% volume) instead of sudden silence.
 */
class PresenceDetector(
    private val context: Context,
    private val onPresenceDetected: () -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val proximitySensor = sensorManager?.getDefaultSensor(Sensor.TYPE_PROXIMITY)
    private val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var hasTriggered = false
    private var initialReadingsCount = 0
    private var baseAccMagnitude = 0f

    fun start() {
        hasTriggered = false
        initialReadingsCount = 0
        sensorManager?.let { sm ->
            proximitySensor?.let { sm.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
            accelerometer?.let { sm.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
        }
    }

    fun stop() {
        try {
            sensorManager?.unregisterListener(this)
        } catch (_: Throwable) {}
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (hasTriggered || event == null) return

        when (event.sensor.type) {
            Sensor.TYPE_PROXIMITY -> {
                val distance = event.values[0]
                val maxRange = proximitySensor?.maximumRange ?: 5f
                // If user is within close proximity (under 5cm or less than maxRange)
                if (distance < maxRange.coerceAtMost(5f)) {
                    hasTriggered = true
                    onPresenceDetected()
                }
            }
            Sensor.TYPE_ACCELEROMETER -> {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                val mag = sqrt((x * x + y * y + z * z).toDouble()).toFloat()

                // Calibrate initial resting state
                if (initialReadingsCount < 5) {
                    baseAccMagnitude = mag
                    initialReadingsCount++
                } else {
                    // Check for pickup movement delta
                    val delta = kotlin.math.abs(mag - baseAccMagnitude)
                    if (delta > 3.2f) { // Noticeable movement/pickup
                        hasTriggered = true
                        onPresenceDetected()
                    }
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op
    }
}
