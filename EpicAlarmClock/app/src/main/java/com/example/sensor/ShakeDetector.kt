package com.example.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

/**
 * Detects phone shake gestures with configurable sensitivity.
 * Sensitivity thresholds (m/s² beyond 1g):
 * - LOW: 24.0 (requires vigorous deliberate shaking)
 * - MEDIUM: 17.0 (balanced, natural shake)
 * - HIGH: 12.0 (gentle shake, ideal for low-vision users & dark environments)
 */
class ShakeDetector(
    private val context: Context,
    private val sensitivity: String = "MEDIUM",
    private val onShake: () -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var lastShakeTimestamp: Long = 0L
    private val debounceMillis = 1500L

    private val threshold: Float = when (sensitivity) {
        "LOW" -> 24.0f
        "HIGH" -> 12.0f
        else -> 17.0f
    }

    fun start() {
        if (accelerometer != null && sensorManager != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stop() {
        try {
            sensorManager?.unregisterListener(this)
        } catch (_: Throwable) {}
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type != Sensor.TYPE_ACCELEROMETER) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        val totalG = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
        val netForce = kotlin.math.abs(totalG - SensorManager.GRAVITY_EARTH)

        if (netForce > threshold) {
            val now = System.currentTimeMillis()
            if (now - lastShakeTimestamp > debounceMillis) {
                lastShakeTimestamp = now
                onShake()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op
    }
}
