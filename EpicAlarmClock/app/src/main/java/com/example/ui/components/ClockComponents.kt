package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EpicTheme
import com.example.ui.theme.HandHourColor
import com.example.ui.theme.HandMinuteColor
import com.example.ui.theme.HandSecondColor
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.OledBackground
import com.example.ui.theme.OledBorder
import com.example.ui.theme.OledSurface
import com.example.ui.theme.OledSurfaceElevated
import com.example.ui.theme.OledSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AnalogClockDial(
    hours: Int,
    minutes: Int,
    seconds: Int,
    milliseconds: Int = 0,
    modifier: Modifier = Modifier,
    dialSize: Dp = 220.dp,
    faceColor: Color? = null,
    hourHandColor: Color? = null,
    minuteHandColor: Color? = null,
    secondHandColor: Color? = null,
    numberColor: Color? = null,
    showNumbers: Boolean = true
) {
    val theme = EpicTheme.colors
    val borderColor = theme.border
    val backgroundColor = theme.background
    val mutedColor = theme.mutedText

    val resolvedFace = faceColor ?: (if (theme.isDark) theme.background else theme.surface)
    val dialGradient = listOf(
        resolvedFace.copy(alpha = 0.95f),
        resolvedFace,
        resolvedFace.copy(alpha = 0.8f)
    )

    val defaultHour = if (theme.isDark) Color(0xFFB388FF) else Color(0xFF6D28D9)
    val defaultMin = if (theme.isDark) Color(0xFF00F2FE) else Color(0xFF0284C7)
    val defaultSec = if (theme.isDark) Color(0xFFFF3366) else Color(0xFFDC2626)

    val isFaceBright = (resolvedFace.red * 0.299f + resolvedFace.green * 0.587f + resolvedFace.blue * 0.114f) > 0.6f

    val rawHour = hourHandColor ?: defaultHour
    val rawMin = minuteHandColor ?: defaultMin
    val rawSec = secondHandColor ?: defaultSec
    val rawNum = numberColor ?: theme.primaryText

    // Auto-contrast protection for light/white backgrounds
    val actualHourColor = if (isFaceBright && (rawHour.red > 0.85f && rawHour.green > 0.85f && rawHour.blue > 0.85f)) Color(0xFF6D28D9) else rawHour
    val actualMinColor = if (isFaceBright && (rawMin.red > 0.85f && rawMin.green > 0.85f && rawMin.blue > 0.85f)) Color(0xFF0284C7) else rawMin
    val actualSecColor = if (isFaceBright && (rawSec.red > 0.85f && rawSec.green > 0.85f && rawSec.blue > 0.85f)) Color(0xFFDC2626) else rawSec
    val actualNumColor = if (isFaceBright && (rawNum.red > 0.85f && rawNum.green > 0.85f && rawNum.blue > 0.85f)) Color(0xFF0F172A) else rawNum

    Box(
        modifier = modifier
            .size(dialSize)
            .shadow(16.dp, CircleShape, spotColor = actualHourColor)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = dialGradient
                )
            )
            .border(2.dp, Brush.sweepGradient(listOf(actualHourColor, actualMinColor, actualHourColor)), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(dialSize - 16.dp)) {
            val radius = size.minDimension / 2
            val center = Offset(size.width / 2, size.height / 2)

            // Outer decorative ring
            drawCircle(
                color = borderColor,
                radius = radius,
                center = center,
                style = Stroke(width = 1.5f)
            )

            // Inner subtle ring
            drawCircle(
                color = borderColor.copy(alpha = 0.5f),
                radius = radius * 0.78f,
                center = center,
                style = Stroke(width = 1f)
            )

            // 60 tick marks
            for (i in 0 until 60) {
                val angleRad = Math.toRadians((i * 6.0) - 90.0)
                val isMajor = i % 5 == 0
                val tickLength = if (isMajor) radius * 0.14f else radius * 0.06f
                val strokeWidth = if (isMajor) 3f else 1.2f
                val tickColor = if (isMajor) actualHourColor else mutedColor.copy(alpha = 0.6f)

                val start = Offset(
                    x = center.x + ((radius - tickLength) * cos(angleRad)).toFloat(),
                    y = center.y + ((radius - tickLength) * sin(angleRad)).toFloat()
                )
                val end = Offset(
                    x = center.x + (radius * cos(angleRad)).toFloat(),
                    y = center.y + (radius * sin(angleRad)).toFloat()
                )
                drawLine(
                    color = tickColor,
                    start = start,
                    end = end,
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
            }

            // Draw numbers 1 to 12 if enabled
            if (showNumbers) {
                val textPaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.argb(
                        (actualNumColor.alpha * 255).toInt(),
                        (actualNumColor.red * 255).toInt(),
                        (actualNumColor.green * 255).toInt(),
                        (actualNumColor.blue * 255).toInt()
                    )
                    textSize = (radius * 0.18f).coerceAtLeast(20f)
                    textAlign = android.graphics.Paint.Align.CENTER
                    isAntiAlias = true
                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                }

                val numRadius = radius * 0.68f
                for (hour in 1..12) {
                    val angle = Math.toRadians((hour * 30.0) - 90.0)
                    val x = (center.x + numRadius * cos(angle)).toFloat()
                    val y = (center.y + numRadius * sin(angle) + (textPaint.textSize / 3)).toFloat()
                    drawContext.canvas.nativeCanvas.drawText(hour.toString(), x, y, textPaint)
                }
            }

            // Hour Hand
            val hourAngle = ((hours % 12) + minutes / 60f) * 30.0 - 90.0
            val hourRad = Math.toRadians(hourAngle)
            val hourLen = radius * 0.50f
            val hourEnd = Offset(
                x = center.x + (hourLen * cos(hourRad)).toFloat(),
                y = center.y + (hourLen * sin(hourRad)).toFloat()
            )
            val hourTail = Offset(
                x = center.x - (radius * 0.12f * cos(hourRad)).toFloat(),
                y = center.y - (radius * 0.12f * sin(hourRad)).toFloat()
            )
            drawLine(
                color = actualHourColor,
                start = hourTail,
                end = hourEnd,
                strokeWidth = 6.5f,
                cap = StrokeCap.Round
            )

            // Minute Hand
            val minuteAngle = (minutes + seconds / 60f) * 6.0 - 90.0
            val minuteRad = Math.toRadians(minuteAngle)
            val minuteLen = radius * 0.72f
            val minuteEnd = Offset(
                x = center.x + (minuteLen * cos(minuteRad)).toFloat(),
                y = center.y + (minuteLen * sin(minuteRad)).toFloat()
            )
            val minuteTail = Offset(
                x = center.x - (radius * 0.15f * cos(minuteRad)).toFloat(),
                y = center.y - (radius * 0.15f * sin(minuteRad)).toFloat()
            )
            drawLine(
                color = actualMinColor,
                start = minuteTail,
                end = minuteEnd,
                strokeWidth = 4f,
                cap = StrokeCap.Round
            )

            // Second Hand (Smooth with millis)
            val secondAngle = (seconds + (milliseconds / 1000f)) * 6.0 - 90.0
            val secondRad = Math.toRadians(secondAngle)
            val secondLen = radius * 0.86f
            val secondEnd = Offset(
                x = center.x + (secondLen * cos(secondRad)).toFloat(),
                y = center.y + (secondLen * sin(secondRad)).toFloat()
            )
            val secondTail = Offset(
                x = center.x - (radius * 0.22f * cos(secondRad)).toFloat(),
                y = center.y - (radius * 0.22f * sin(secondRad)).toFloat()
            )
            drawLine(
                color = actualSecColor,
                start = secondTail,
                end = secondEnd,
                strokeWidth = 2f,
                cap = StrokeCap.Round
            )

            // Center Pin Caps
            drawCircle(color = actualSecColor, radius = 6.5f, center = center)
            drawCircle(color = resolvedFace, radius = 2.5f, center = center)
        }
    }
}

@Composable
fun ConfigurableAnalogClockSection(
    hours: Int,
    minutes: Int,
    seconds: Int,
    milliseconds: Int = 0,
    settings: com.example.data.local.AppSettings,
    language: com.example.i18n.AppLanguage,
    modifier: Modifier = Modifier
) {
    val theme = EpicTheme.colors
    val tripleDate = remember(language, hours, minutes) {
        com.example.calendar.TripleCalendarHelper.getTodayTripleDate(language)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnalogClockDial(
            hours = hours,
            minutes = minutes,
            seconds = seconds,
            milliseconds = milliseconds,
            dialSize = settings.clockSizePx.dp,
            faceColor = Color(settings.clockFaceColor),
            hourHandColor = Color(settings.hourHandColor),
            minuteHandColor = Color(settings.minuteHandColor),
            secondHandColor = Color(settings.secondHandColor),
            numberColor = Color(settings.numberColor),
            showNumbers = settings.showNumbers
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Dates underneath analog clock
        if (settings.showSolarDate || settings.showGregorianDate || settings.showLunarDate) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (settings.showSolarDate) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(theme.surfaceVariant)
                            .border(1.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "☀️ " + tripleDate.solarFormatted,
                            color = NeonCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                if (settings.showGregorianDate) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(theme.surfaceVariant)
                            .border(1.dp, NeonPurple.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "🌍 " + tripleDate.gregorianFormatted,
                            color = NeonPurple,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                if (settings.showLunarDate) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(theme.surfaceVariant)
                            .border(1.dp, NeonAmber.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "🌙 " + tripleDate.lunarFormatted,
                            color = NeonAmber,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AnalogStopwatchDial(
    elapsedMillis: Long,
    modifier: Modifier = Modifier,
    dialSize: Dp = 230.dp
) {
    val totalSeconds = (elapsedMillis / 1000) % 60
    val totalMinutes = (elapsedMillis / 60000) % 30
    val millisRemainder = (elapsedMillis % 1000).toInt()

    val theme = EpicTheme.colors
    val borderColor = theme.border
    val backgroundColor = theme.background
    val surfaceColor = theme.surface
    val mutedColor = theme.mutedText
    val dialGradient = if (theme.isDark) {
        listOf(theme.surfaceElevated, theme.surface, theme.background)
    } else {
        listOf(theme.surface, theme.surfaceElevated, theme.surfaceVariant)
    }

    Box(
        modifier = modifier
            .size(dialSize)
            .shadow(16.dp, CircleShape, spotColor = NeonPurple)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(dialGradient)
            )
            .border(2.dp, Brush.sweepGradient(listOf(NeonPurple, NeonCyan, NeonPurple)), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(dialSize - 16.dp)) {
            val radius = size.minDimension / 2
            val center = Offset(size.width / 2, size.height / 2)

            // Outer track
            drawCircle(
                color = borderColor,
                radius = radius,
                center = center,
                style = Stroke(width = 1.5f)
            )

            // Stopwatch Tick marks (outer 60s)
            for (i in 0 until 120) {
                val angleRad = Math.toRadians((i * 3.0) - 90.0)
                val isSecond = i % 2 == 0
                val isFiveSeconds = i % 10 == 0
                val tickLength = if (isFiveSeconds) radius * 0.15f else if (isSecond) radius * 0.08f else radius * 0.04f
                val strokeWidth = if (isFiveSeconds) 3f else if (isSecond) 1.5f else 0.8f
                val tickColor = if (isFiveSeconds) theme.accentCyan else mutedColor.copy(alpha = 0.5f)

                val start = Offset(
                    x = center.x + ((radius - tickLength) * cos(angleRad)).toFloat(),
                    y = center.y + ((radius - tickLength) * sin(angleRad)).toFloat()
                )
                val end = Offset(
                    x = center.x + (radius * cos(angleRad)).toFloat(),
                    y = center.y + (radius * sin(angleRad)).toFloat()
                )
                drawLine(
                    color = tickColor,
                    start = start,
                    end = end,
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
            }

            // Inset sub-dial for 30 minutes at top center
            val subCenter = Offset(center.x, center.y - (radius * 0.40f))
            val subRadius = radius * 0.28f

            drawCircle(
                color = surfaceColor,
                radius = subRadius,
                center = subCenter
            )
            drawCircle(
                color = theme.accentCyan.copy(alpha = 0.4f),
                radius = subRadius,
                center = subCenter,
                style = Stroke(width = 1f)
            )

            // Sub-dial ticks (0, 10, 20, 30 min)
            for (m in 0 until 30 step 5) {
                val subAngle = Math.toRadians((m * 12.0) - 90.0)
                val subStart = Offset(
                    x = subCenter.x + ((subRadius - 6f) * cos(subAngle)).toFloat(),
                    y = subCenter.y + ((subRadius - 6f) * sin(subAngle)).toFloat()
                )
                val subEnd = Offset(
                    x = subCenter.x + (subRadius * cos(subAngle)).toFloat(),
                    y = subCenter.y + (subRadius * sin(subAngle)).toFloat()
                )
                drawLine(
                    color = theme.accentAmber,
                    start = subStart,
                    end = subEnd,
                    strokeWidth = 1.5f
                )
            }

            // Sub-dial needle (minutes)
            val subNeedleAngle = Math.toRadians((totalMinutes * 12.0) - 90.0)
            val subNeedleEnd = Offset(
                x = subCenter.x + ((subRadius * 0.75f) * cos(subNeedleAngle)).toFloat(),
                y = subCenter.y + ((subRadius * 0.75f) * sin(subNeedleAngle)).toFloat()
            )
            drawLine(
                color = theme.accentAmber,
                start = subCenter,
                end = subNeedleEnd,
                strokeWidth = 2.5f,
                cap = StrokeCap.Round
            )
            drawCircle(color = theme.accentAmber, radius = 3f, center = subCenter)

            // Main Sweep Needle (60 seconds)
            val mainAngle = (totalSeconds + (millisRemainder / 1000f)) * 6.0 - 90.0
            val mainRad = Math.toRadians(mainAngle)
            val mainLen = radius * 0.88f
            val mainEnd = Offset(
                x = center.x + (mainLen * cos(mainRad)).toFloat(),
                y = center.y + (mainLen * sin(mainRad)).toFloat()
            )
            val mainTail = Offset(
                x = center.x - (radius * 0.20f * cos(mainRad)).toFloat(),
                y = center.y - (radius * 0.20f * sin(mainRad)).toFloat()
            )
            drawLine(
                color = HandSecondColor,
                start = mainTail,
                end = mainEnd,
                strokeWidth = 2.5f,
                cap = StrokeCap.Round
            )

            // Center Pin
            drawCircle(color = HandSecondColor, radius = 6.5f, center = center)
            drawCircle(color = backgroundColor, radius = 2.5f, center = center)
        }
    }
}

@Composable
fun CountdownBanner(
    text: String,
    isTimer: Boolean = false,
    modifier: Modifier = Modifier
) {
    val pulseAnim = remember { Animatable(1f) }
    LaunchedEffect(Unit) {
        pulseAnim.animateTo(
            targetValue = 1.05f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
    }

    val glowColor = if (isTimer) NeonAmber else NeonCyan

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .shadow(8.dp, RoundedCornerShape(16.dp), spotColor = glowColor)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        OledSurfaceElevated,
                        OledSurfaceVariant
                    )
                )
            )
            .border(
                1.dp,
                Brush.horizontalGradient(listOf(glowColor.copy(alpha = 0.8f), NeonPurple.copy(alpha = 0.5f))),
                RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(glowColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isTimer) Icons.Default.HourglassTop else Icons.Default.Alarm,
                    contentDescription = null,
                    tint = glowColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = text,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 20.sp
                )
            }
        }
    }
}
