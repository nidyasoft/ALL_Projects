package com.example.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.abs

/**
 * Detects turning the phone face-down (screen facing the ground) while an alarm/timer rings,
 * triggering a Snooze action once the device is flipped and held screen-down briefly.
 */
class FlipToSnoozeDetector(
    context: Context,
    private val onFlipToSnooze: () -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var hasTriggered = false
    private var sawNotFaceDown = false
    private var faceDownStartTime = 0L

    fun start() {
        hasTriggered = false
        sawNotFaceDown = false
        faceDownStartTime = 0L
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
        if (hasTriggered || event?.sensor?.type != Sensor.TYPE_ACCELEROMETER) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        // Device must first be observed NOT lying flat face-down so a phone that started
        // face-down before the alarm fired does not snooze instantaneously.
        if (!sawNotFaceDown) {
            if (z > -3.5f) {
                sawNotFaceDown = true
            }
            return
        }

        // Screen facing the ground: Z axis points toward ~ -9.8 m/s² while X/Y are near 0
        val isFaceDown = z < -7.5f && abs(x) < 5.0f && abs(y) < 5.0f
        if (isFaceDown) {
            val now = System.currentTimeMillis()
            if (faceDownStartTime == 0L) {
                faceDownStartTime = now
            } else if (now - faceDownStartTime >= 450L) {
                hasTriggered = true
                stop()
                onFlipToSnooze()
            }
        } else {
            faceDownStartTime = 0L
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op
    }
}
