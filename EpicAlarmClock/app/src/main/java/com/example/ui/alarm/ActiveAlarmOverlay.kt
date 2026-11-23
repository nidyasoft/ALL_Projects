package com.example.ui.alarm

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AlarmOff
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.i18n.AppLanguage
import com.example.i18n.Strings
import com.example.ui.FiredAlarm
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonRed
import com.example.ui.theme.OledBackground
import com.example.ui.theme.OledBorder
import com.example.ui.theme.OledSurface
import com.example.ui.theme.OledSurfaceElevated
import com.example.ui.theme.TextPrimary

@Composable
fun ActiveAlarmOverlay(
    firedAlarm: FiredAlarm,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onSnooze: (Int) -> Unit
) {
    val pulseScale = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        pulseScale.animateTo(
            targetValue = 1.25f,
            animationSpec = infiniteRepeatable(
                animation = tween(600, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
    }

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .border(2.dp, Brush.sweepGradient(listOf(NeonRed, NeonAmber, NeonCyan, NeonRed)), RoundedCornerShape(28.dp)),
            colors = CardDefaults.cardColors(containerColor = OledSurfaceElevated),
            shape = RoundedCornerShape(28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Pulsing glowing icon
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .scale(pulseScale.value)
                        .clip(CircleShape)
                        .background(NeonRed.copy(alpha = 0.2f))
                        .border(2.dp, NeonRed, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (firedAlarm.isTimer) Icons.Default.HourglassBottom else Icons.Default.Alarm,
                        contentDescription = null,
                        tint = NeonRed,
                        modifier = Modifier.size(54.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = firedAlarm.title,
                    color = TextPrimary,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (firedAlarm.isTimer) "زمان تایمر به پایان رسید!" else "زمان بیداری فرارسیده است!",
                    color = NeonAmber,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Actions: Dismiss & Snooze (Available for BOTH Alarm and Timer)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("dismiss_alarm_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonRed, contentColor = TextPrimary),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(imageVector = Icons.Default.AlarmOff, contentDescription = null, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(text = Strings.get("dismiss", language), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    // Snooze option available for both Alarm and Timer
                    OutlinedButton(
                        onClick = { onSnooze(5) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("snooze_alarm_button"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(NeonCyan)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Snooze, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(text = Strings.get("snooze", language) + " (۵ دقیقه)", fontSize = 14.sp)
                    }
                }
            }
        }
    }
}
