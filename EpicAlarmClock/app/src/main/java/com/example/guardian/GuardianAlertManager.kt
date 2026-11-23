package com.example.guardian

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.telephony.SmsManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.data.local.AppSettings
import com.example.sensor.GyroscopeMotionTracker

object GuardianAlertManager {

    private const val TAG = "GuardianAlertManager"

    /**
     * Builds the complete emergency SMS text according to user settings and sensor state.
     */
    fun buildEmergencyMessage(context: Context, settings: AppSettings): String {
        val baseMsg = settings.guardianCustomMessage.ifBlank {
            "هشدار اضطراری: زنگ ساعت هشدار پس از چند دقیقه بی‌پاسخ مانده و کاربر هنوز واکنشی نشان نداده است."
        }

        val sb = StringBuilder(baseMsg)

        if (settings.guardianIncludeMissedAlarms) {
            val missedCount = GyroscopeMotionTracker.getMissedAlarmsLast24Hours(context)
            sb.append("\n• هشدارهای بی‌پاسخ ۲۴ ساعت گذشته: $missedCount مورد")
        }

        if (settings.guardianIncludeGyroscopeData) {
            val lastMotion = GyroscopeMotionTracker.getLastMotionTimeFormatted(context)
            val motionlessDuration = GyroscopeMotionTracker.getMotionlessDurationSummary(context)
            sb.append("\n• وضعیت حسگر ژیروسکوپ:")
            sb.append("\n  - آخرین حرکت ثبت‌شده: $lastMotion")
            sb.append("\n  - مدت بدون حرکت ماندن گوشی: $motionlessDuration")
        }

        sb.append("\n(ارسال خودکار توسط سامانه نگهبان ساعت هشدار اپیک)")
        return sb.toString()
    }

    /**
     * Dispatches the emergency SMS to the designated contact if permission is granted.
     * Returns true if successfully queued/sent.
     */
    fun sendEmergencyAlert(context: Context, settings: AppSettings): Boolean {
        val phoneNumber = settings.guardianPhoneNumber.trim()
        if (phoneNumber.isBlank()) {
            Log.w(TAG, "Guardian phone number is blank, cannot send SMS.")
            return false
        }

        val fullMessage = buildEmergencyMessage(context, settings)

        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            Log.w(TAG, "SEND_SMS permission not granted. Launching fallback action intent.")
            return false
        }

        return try {
            val smsManager: SmsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }

            val parts = smsManager.divideMessage(fullMessage)
            if (parts.size > 1) {
                smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null)
            } else {
                smsManager.sendTextMessage(phoneNumber, null, fullMessage, null, null)
            }
            Log.i(TAG, "Guardian emergency SMS dispatched to $phoneNumber")
            true
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to send SMS directly: ${e.message}", e)
            false
        }
    }

    /**
     * Launch SMS application as fallback or manual action
     */
    fun openSmsAppFallback(context: Context, phoneNumber: String, message: String) {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:${Uri.encode(phoneNumber)}")
                putExtra("sms_body", message)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Throwable) {}
    }
}
