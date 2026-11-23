package com.example.ui.security

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AppSettings
import com.example.security.AppLockManager
import com.example.ui.theme.EpicTheme
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonRed
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun AppLockScreen(
    appSettings: AppSettings,
    onUnlocked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val theme = EpicTheme.colors
    val coroutineScope = rememberCoroutineScope()

    var enteredPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val shakeOffset = remember { Animatable(0f) }

    val hasAppPin = appSettings.appLockUseAppPin && appSettings.appLockAppPinHash.isNotBlank()
    val canUseDeviceCredentials = (appSettings.appLockUseFingerprint || appSettings.appLockUseFace || appSettings.appLockUseDevicePin) &&
            AppLockManager.isDeviceSecure(context)

    // Launcher for Android System Confirm Device Credential (Fingerprint, Face, or Device PIN)
    val deviceCredentialLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            onUnlocked()
        } else {
            errorMessage = "تأیید هویت دستگاه ناموفق بود"
        }
    }

    fun launchDeviceCredentialPrompt() {
        if (canUseDeviceCredentials) {
            val title = "ورود به ساعت هشدار اپیک"
            val desc = "جهت ورود به برنامه، هویت خود را با اثر انگشت، تشخیص چهره یا رمز دستگاه تأیید نمایید"
            val intent = AppLockManager.createDeviceCredentialIntent(context, title, desc)
            if (intent != null) {
                deviceCredentialLauncher.launch(intent)
            }
        }
    }

    // Auto-prompt device credentials on launch if enabled and device is secure
    LaunchedEffect(Unit) {
        if (canUseDeviceCredentials) {
            launchDeviceCredentialPrompt()
        }
    }

    fun handlePinKey(digit: String) {
        if (enteredPin.length < 6) {
            val newPin = enteredPin + digit
            enteredPin = newPin
            errorMessage = null

            // If PIN matches stored hash
            if (AppLockManager.verifyPin(newPin, appSettings.appLockAppPinHash)) {
                onUnlocked()
            } else if (newPin.length >= 4 && appSettings.appLockAppPinHash.isNotBlank()) {
                // If maximum expected length reached and still invalid
                if (newPin.length == 6) {
                    coroutineScope.launch {
                        errorMessage = "رمز عبور اختصاصی اشتباه است"
                        shakeOffset.animateTo(
                            targetValue = 0f,
                            animationSpec = keyframes {
                                durationMillis = 400
                                -20f at 50
                                20f at 100
                                -15f at 150
                                15f at 200
                                -10f at 250
                                10f at 300
                                0f at 400
                            }
                        )
                        enteredPin = ""
                    }
                }
            }
        }
    }

    fun handleDelete() {
        if (enteredPin.isNotEmpty()) {
            enteredPin = enteredPin.dropLast(1)
            errorMessage = null
        }
    }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("app_lock_screen"),
        color = theme.surface
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .offset { IntOffset(shakeOffset.value.roundToInt(), 0) }
            ) {
                // Security Icon & Badge
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(theme.accentCyan.copy(alpha = 0.15f))
                        .border(1.5.dp, theme.accentCyan.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (canUseDeviceCredentials) Icons.Default.Fingerprint else Icons.Default.Lock,
                        contentDescription = null,
                        tint = theme.accentCyan,
                        modifier = Modifier.size(42.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "ساعت هشدار اپیک",
                    color = theme.primaryText,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = if (hasAppPin) "لطفاً پین کد امنیتی را وارد نمایید" else "ورود امن با هویت دستگاه",
                    color = theme.secondaryText,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                // Error message
                AnimatedVisibility(visible = errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = NeonRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // PIN Dots Display (if in-app PIN is enabled)
                if (hasAppPin) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 12.dp)
                    ) {
                        val pinSlots = 4.coerceAtLeast(enteredPin.length)
                        for (i in 0 until pinSlots) {
                            val isFilled = i < enteredPin.length
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(if (isFilled) theme.accentCyan else theme.surfaceElevated)
                                    .border(1.5.dp, if (isFilled) theme.accentCyan else theme.border, CircleShape)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Numeric Keypad
                    val keypad = listOf(
                        listOf("1", "2", "3"),
                        listOf("4", "5", "6"),
                        listOf("7", "8", "9"),
                        listOf("bio", "0", "del")
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        keypad.forEach { row ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(20.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                row.forEach { key ->
                                    when (key) {
                                        "bio" -> {
                                            if (canUseDeviceCredentials) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(68.dp)
                                                        .clip(CircleShape)
                                                        .background(theme.surfaceElevated)
                                                        .border(1.dp, theme.accentCyan.copy(alpha = 0.4f), CircleShape)
                                                        .clickable { launchDeviceCredentialPrompt() }
                                                        .testTag("lock_bio_button"),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Fingerprint,
                                                        contentDescription = "Device Biometric",
                                                        tint = theme.accentCyan,
                                                        modifier = Modifier.size(30.dp)
                                                    )
                                                }
                                            } else {
                                                Spacer(modifier = Modifier.size(68.dp))
                                            }
                                        }
                                        "del" -> {
                                            Box(
                                                modifier = Modifier
                                                    .size(68.dp)
                                                    .clip(CircleShape)
                                                    .background(theme.surfaceElevated)
                                                    .border(1.dp, theme.border, CircleShape)
                                                    .clickable { handleDelete() }
                                                    .testTag("lock_delete_button"),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Backspace,
                                                    contentDescription = "Delete",
                                                    tint = theme.primaryText,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                        }
                                        else -> {
                                            Box(
                                                modifier = Modifier
                                                    .size(68.dp)
                                                    .clip(CircleShape)
                                                    .background(theme.surfaceElevated)
                                                    .border(1.dp, theme.border, CircleShape)
                                                    .clickable { handlePinKey(key) }
                                                    .testTag("lock_key_$key"),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = key,
                                                    color = theme.primaryText,
                                                    fontSize = 24.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // If only device credentials enabled or prominent button
                if (canUseDeviceCredentials && !hasAppPin) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { launchDeviceCredentialPrompt() },
                        colors = ButtonDefaults.buttonColors(containerColor = theme.accentCyan),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("lock_device_credential_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "ورود با اثر انگشت، چهره یا رمز دستگاه",
                            color = Color.Black,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Fallback unlock if neither device credentials nor in-app PIN are configured properly
                if (!canUseDeviceCredentials && !hasAppPin) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "هیچ روش احراز هویتی فعال نیست. برای ورود دکمه زیر را لمس کنید:",
                        color = NeonAmber,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onUnlocked,
                        colors = ButtonDefaults.buttonColors(containerColor = theme.accentCyan),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "ورود به برنامه", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
