package com.example.ui.keyboard

import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View

object HapticFeedbackHelper {

    fun performHaptic(context: Context, enabled: Boolean, durationMs: Long, view: View? = null) {
        if (!enabled) return

        try {
            if (view != null) {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                return
            }

            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            if (vibrator?.hasVibrator() == true) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(
                        VibrationEffect.createOneShot(
                            durationMs.coerceIn(5L, 60L),
                            VibrationEffect.DEFAULT_AMPLITUDE
                        )
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(durationMs.coerceIn(5L, 60L))
                }
            }
        } catch (_: Exception) {
            // Ignore system vibration restrictions
        }
    }

    fun playSound(context: Context, enabled: Boolean) {
        if (!enabled) return
        try {
            val am = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            am?.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD, 0.4f)
        } catch (_: Exception) {
        }
    }
}
