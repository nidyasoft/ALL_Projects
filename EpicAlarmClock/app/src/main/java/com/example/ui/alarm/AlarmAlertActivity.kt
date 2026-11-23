package com.example.ui.alarm

import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlarmOff
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.i18n.AppLanguage
import com.example.i18n.Strings
import com.example.quote.WakeupQuote
import com.example.quote.WakeupQuotes
import com.example.receiver.AlarmActionReceiver
import com.example.service.AlarmAlertService
import com.example.ui.components.AnalogClockDial
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonRed
import com.example.ui.theme.OledBackground
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import java.util.Calendar

class AlarmAlertActivity : ComponentActivity() {

    private val closeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == AlarmAlertService.ACTION_CLOSE_ALERT_UI) {
                finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Turn screen on and show on top of keyguard / lock screen (including Samsung One UI)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
            keyguardManager?.requestDismissKeyguard(this, null)
        }
        @Suppress("DEPRECATION")
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
        )

        val filter = IntentFilter(AlarmAlertService.ACTION_CLOSE_ALERT_UI)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(closeReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(closeReceiver, filter)
        }

        val alarmId = intent.getLongExtra("EXTRA_ALARM_ID", 0L)
        val title = intent.getStringExtra("EXTRA_ALARM_LABEL") ?: "هشدار بیدارباش"
        val soundId = intent.getStringExtra("EXTRA_SOUND_ID") ?: "cyber_pulse"
        val vibration = intent.getStringExtra("EXTRA_VIBRATION") ?: "INTERMITTENT"
        val isTimer = intent.getBooleanExtra("EXTRA_IS_TIMER", false)

        val settingsManager = com.example.data.local.SettingsManager.getInstance(this)
        val settings = settingsManager.getSettings()
        val language = settingsManager.getLanguage()
        val themeMode = settingsManager.getThemeMode()
        val randomQuote = WakeupQuotes.getRandomQuote()

        val layoutDirection = if (language == AppLanguage.PERSIAN || language == AppLanguage.ARABIC) {
            LayoutDirection.Rtl
        } else {
            LayoutDirection.Ltr
        }

        if (settings.shakeToStopEnabled) {
            shakeDetector = com.example.sensor.ShakeDetector(this, settings.shakeSensitivity) {
                sendAction(AlarmActionReceiver.ACTION_DISMISS, alarmId, title, soundId, vibration, isTimer)
                finish()
            }
            shakeDetector?.start()
        }

        setContent {
            androidx.compose.runtime.CompositionLocalProvider(
                LocalLayoutDirection provides layoutDirection
            ) {
                com.example.ui.theme.EpicAlarmClockTheme(themeMode = themeMode) {
                    AlarmAlertFullscreenContent(
                        title = title,
                        isTimer = isTimer,
                        snoozeDurationMinutes = settings.snoozeDurationMinutes,
                        quote = randomQuote,
                        language = language,
                        onDismiss = {
                            sendAction(AlarmActionReceiver.ACTION_DISMISS, alarmId, title, soundId, vibration, isTimer)
                            finish()
                        },
                        onSnooze = {
                            sendAction(AlarmActionReceiver.ACTION_SNOOZE, alarmId, title, soundId, vibration, isTimer)
                            finish()
                        }
                    )
                }
            }
        }
    }

    private var shakeDetector: com.example.sensor.ShakeDetector? = null

    override fun onDestroy() {
        super.onDestroy()
        try {
            shakeDetector?.stop()
            shakeDetector = null
        } catch (_: Throwable) {}
        try {
            unregisterReceiver(closeReceiver)
        } catch (_: Throwable) {}
    }

    private fun sendAction(actionStr: String, alarmId: Long, title: String, soundId: String, vibration: String, isTimer: Boolean) {
        val broadcastIntent = Intent(this, AlarmActionReceiver::class.java).apply {
            action = actionStr
            putExtra("EXTRA_ALARM_ID", alarmId)
            putExtra("EXTRA_ALARM_LABEL", title)
            putExtra("EXTRA_SOUND_ID", soundId)
            putExtra("EXTRA_VIBRATION", vibration)
            putExtra("EXTRA_IS_TIMER", isTimer)
        }
        sendBroadcast(broadcastIntent)
    }
}

@Composable
fun AlarmAlertFullscreenContent(
    title: String,
    isTimer: Boolean,
    snoozeDurationMinutes: Int,
    quote: WakeupQuote,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onSnooze: () -> Unit
) {
    var hour by remember { mutableIntStateOf(0) }
    var minute by remember { mutableIntStateOf(0) }
    var second by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            val cal = Calendar.getInstance()
            hour = cal.get(Calendar.HOUR_OF_DAY)
            minute = cal.get(Calendar.MINUTE)
            second = cal.get(Calendar.SECOND)
            delay(500)
        }
    }

    Scaffold(
        containerColor = OledBackground
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            NeonRed.copy(alpha = 0.25f),
                            OledBackground
                        )
                    )
                ),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Philosophical Wake-up Quote Card at TOP
                QuoteBillboardCard(quote = quote)

                // 2. Functional Analog Clock Dial
                AnalogClockDial(
                    hours = hour,
                    minutes = minute,
                    seconds = second,
                    dialSize = 140.dp,
                    showNumbers = true
                )

                // 3. Digital Clock in standard LTR format (HH:mm:ss)
                androidx.compose.runtime.CompositionLocalProvider(
                    LocalLayoutDirection provides LayoutDirection.Ltr
                ) {
                    Text(
                        text = String.format("%02d:%02d:%02d", hour, minute, second),
                        color = TextPrimary,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                }

                // 4. Electronic LED Marquee Billboard for Alert Label (تابلو روان)
                MarqueeLedBillboard(title = title, isTimer = isTimer)

                Spacer(modifier = Modifier.height(8.dp))

                // 5. Actions: Dismiss & Snooze (with exact configured snooze minutes)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Big Dismiss Button
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp)
                            .testTag("fullscreen_dismiss_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonRed, contentColor = Color.White),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Icon(imageVector = Icons.Default.AlarmOff, contentDescription = null, modifier = Modifier.size(26.dp))
                        Spacer(modifier = Modifier.size(10.dp))
                        Text(text = "خاموش کردن (Dismiss)", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    }

                    // Big Snooze Button showing exact minutes
                    val snoozeMinutes = snoozeDurationMinutes.coerceAtLeast(1)
                    val snoozeText = if (language == AppLanguage.ENGLISH) {
                        "Snooze ($snoozeMinutes min)"
                    } else {
                        "تعویق ${Strings.formatNumber(snoozeMinutes, language)} دقیقه‌ای (Snooze)"
                    }

                    OutlinedButton(
                        onClick = onSnooze,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("fullscreen_snooze_button"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(NeonCyan)
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Snooze, contentDescription = null, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.size(10.dp))
                        Text(text = snoozeText, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
fun QuoteBillboardCard(quote: WakeupQuote) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF0F172A).copy(alpha = 0.85f))
            .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FormatQuote,
                    contentDescription = null,
                    tint = NeonAmber,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "پیام صبحگاهی اندیشمندان",
                    color = NeonAmber,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Persian translation
            Text(
                text = quote.persian,
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            // English quotation
            androidx.compose.runtime.CompositionLocalProvider(
                LocalLayoutDirection provides LayoutDirection.Ltr
            ) {
                Text(
                    text = "\"${quote.english}\"",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontStyle = FontStyle.Italic,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                )
            }

            // Author / Speaker tag
            Text(
                text = "— ${quote.author}",
                color = NeonCyan,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun MarqueeLedBillboard(
    title: String,
    isTimer: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "marqueeBlink")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 650, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    val neonColor = if (isTimer) NeonAmber else NeonCyan

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF070B14))
            .border(
                width = 2.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        neonColor.copy(alpha = glowAlpha),
                        NeonRed.copy(alpha = glowAlpha * 0.8f),
                        neonColor.copy(alpha = glowAlpha)
                    )
                ),
                shape = RoundedCornerShape(18.dp)
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .clip(CircleShape)
                    .background(neonColor.copy(alpha = glowAlpha))
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = if (isTimer) "« برچسب تایمر »" else "« برچسب آلارم »",
                color = neonColor.copy(alpha = glowAlpha),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.size(8.dp))
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .clip(CircleShape)
                    .background(neonColor.copy(alpha = glowAlpha))
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .basicMarquee(
                    iterations = Int.MAX_VALUE,
                    initialDelayMillis = 400,
                    velocity = 45.dp
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title.ifBlank { if (isTimer) "تایمر به پایان رسید" else "ساعت بیدارباش" },
                color = TextPrimary.copy(alpha = 0.85f + (glowAlpha * 0.15f)),
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}
