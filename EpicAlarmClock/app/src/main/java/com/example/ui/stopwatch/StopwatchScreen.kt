package com.example.ui.stopwatch

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.i18n.AppLanguage
import com.example.i18n.Strings
import com.example.ui.LapItem
import com.example.ui.MainViewModel
import com.example.ui.components.AnalogStopwatchDial
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonRed
import com.example.ui.theme.OledBackground
import com.example.ui.theme.OledBorder
import com.example.ui.theme.OledSurfaceElevated
import com.example.ui.theme.OledSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun StopwatchScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val isRunning by viewModel.stopwatchRunning.collectAsState()
    val elapsedMillis by viewModel.stopwatchElapsed.collectAsState()
    val laps by viewModel.stopwatchLaps.collectAsState()
    val language by viewModel.currentLanguage.collectAsState()

    // Formatted stopwatch digital string: mm:ss:ms (e.g. 02:45.89 or 01:23:45.67)
    val totalSeconds = elapsedMillis / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    val centis = (elapsedMillis % 1000) / 10

    val digitalFormatted = if (hours > 0) {
        String.format("%02d:%02d:%02d.%02d", hours, minutes, seconds, centis)
    } else {
        String.format("%02d:%02d.%02d", minutes, seconds, centis)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(OledBackground)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // 1. Analog Stopwatch Gauge
        AnalogStopwatchDial(
            elapsedMillis = elapsedMillis,
            dialSize = 210.dp
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Digital Stopwatch Time
        Text(
            text = Strings.formatNumber(digitalFormatted, language),
            color = TextPrimary,
            fontSize = 38.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(18.dp))

        // 3. Control Buttons: Start/Pause, Lap, Reset
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Reset Button
            OutlinedButton(
                onClick = { viewModel.resetStopwatch() },
                enabled = elapsedMillis > 0,
                modifier = Modifier
                    .size(68.dp)
                    .testTag("stopwatch_reset_button"),
                shape = CircleShape,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = if (elapsedMillis > 0) NeonAmber else TextMuted
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(
                        if (elapsedMillis > 0) NeonAmber else OledBorder
                    )
                )
            ) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = Strings.get("reset", language), modifier = Modifier.size(24.dp))
            }

            // Start / Pause Primary Button
            Button(
                onClick = {
                    if (isRunning) viewModel.pauseStopwatch() else viewModel.startStopwatch()
                },
                modifier = Modifier
                    .size(80.dp)
                    .testTag("stopwatch_start_pause_button"),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRunning) NeonRed else NeonCyan,
                    contentColor = OledBackground
                )
            ) {
                Icon(
                    imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isRunning) Strings.get("pause", language) else Strings.get("start", language),
                    modifier = Modifier.size(36.dp)
                )
            }

            // Lap Record Button
            OutlinedButton(
                onClick = { viewModel.recordLap() },
                enabled = isRunning,
                modifier = Modifier
                    .size(68.dp)
                    .testTag("stopwatch_lap_button"),
                shape = CircleShape,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = if (isRunning) NeonPurple else TextMuted
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(
                        if (isRunning) NeonPurple else OledBorder
                    )
                )
            ) {
                Icon(imageVector = Icons.Default.Flag, contentDescription = Strings.get("lap", language), modifier = Modifier.size(24.dp))
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 4. Laps Header & List
        if (laps.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = Strings.get("lap_num", language), color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(text = Strings.get("split_diff", language), color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(text = Strings.get("lap_time", language), color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(text = Strings.get("overall_time", language), color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            // Find fastest & slowest lap times
            val minLapTime = laps.minOfOrNull { it.lapTimeMillis } ?: 0L
            val maxLapTime = laps.maxOfOrNull { it.lapTimeMillis } ?: 0L

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(laps) { lap ->
                    LapRowItem(
                        lap = lap,
                        isFastest = laps.size > 1 && lap.lapTimeMillis == minLapTime,
                        isSlowest = laps.size > 1 && lap.lapTimeMillis == maxLapTime,
                        language = language
                    )
                }
            }
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun LapRowItem(
    lap: LapItem,
    isFastest: Boolean,
    isSlowest: Boolean,
    language: AppLanguage
) {
    fun formatMillis(millis: Long): String {
        val totalSec = Math.abs(millis) / 1000
        val m = (totalSec % 3600) / 60
        val s = totalSec % 60
        val c = (Math.abs(millis) % 1000) / 10
        return String.format("%02d:%02d.%02d", m, s, c)
    }

    val lapTimeStr = formatMillis(lap.lapTimeMillis)
    val totalTimeStr = formatMillis(lap.totalTimeMillis)
    val diffSign = if (lap.splitDiffMillis > 0) "+" else if (lap.splitDiffMillis < 0) "-" else " "
    val diffStr = "$diffSign${formatMillis(lap.splitDiffMillis)}"

    val rowColor = if (isFastest) NeonGreen else if (isSlowest) NeonRed else TextPrimary

    Card(
        colors = CardDefaults.cardColors(containerColor = OledSurfaceElevated),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isFastest) NeonGreen.copy(alpha = 0.5f) else if (isSlowest) NeonRed.copy(alpha = 0.5f) else OledBorder,
                RoundedCornerShape(12.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Lap Number
            Text(
                text = "${Strings.get("lap", language)} ${Strings.formatNumber(lap.lapNumber, language)}",
                color = rowColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )

            // Split Difference from last lap
            Text(
                text = Strings.formatNumber(diffStr, language),
                color = if (lap.splitDiffMillis < 0) NeonGreen else if (lap.splitDiffMillis > 0) NeonRed else TextMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )

            // Lap Time
            Text(
                text = Strings.formatNumber(lapTimeStr, language),
                color = rowColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )

            // Total Elapsed
            Text(
                text = Strings.formatNumber(totalTimeStr, language),
                color = TextSecondary,
                fontSize = 13.sp
            )
        }
    }
}
