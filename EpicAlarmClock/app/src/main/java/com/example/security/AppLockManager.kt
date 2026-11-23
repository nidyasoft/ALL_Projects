package com.example.security

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import java.security.MessageDigest

object AppLockManager {

    /**
     * Hashes a 4-6 digit in-app PIN using SHA-256 with a project-specific salt.
     */
    fun hashPin(pin: String): String {
        val salt = "epic_alarm_clock_salt_928371"
        val message = "$salt:$pin"
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(message.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Validates entered PIN against the stored hash.
     */
    fun verifyPin(enteredPin: String, storedHash: String): Boolean {
        if (storedHash.isBlank() || enteredPin.isBlank()) return false
        return hashPin(enteredPin) == storedHash
    }

    /**
     * Checks if the Android device has a secure lock screen (Fingerprint, Face, PIN, Pattern, Password).
     */
    fun isDeviceSecure(context: Context): Boolean {
        val km = context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager ?: return false
        return km.isDeviceSecure
    }

    /**
     * Creates system intent to verify device credentials (Fingerprint, Face, or Device PIN).
     */
    fun createDeviceCredentialIntent(context: Context, title: String, description: String): Intent? {
        val km = context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager ?: return null
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            km.createConfirmDeviceCredentialIntent(title, description)
        } else {
            null
        }
    }
}
