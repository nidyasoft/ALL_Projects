package com.example.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.FastScrollbarForScrollState
import com.example.ui.components.FluidBackground
import com.example.ui.utils.LanguageUtils
import com.example.ui.viewmodel.MusicViewModel
import kotlin.math.roundToInt

@Composable
fun EqualizerScreen(
    viewModel: MusicViewModel,
    onBack: () -> Unit
) {
    val lang by viewModel.selectedLanguage.collectAsState()
    val eqEnabled by viewModel.eqEnabled.collectAsState()
    val bands by viewModel.eqBands.collectAsState()
    val bassBoost by viewModel.eqBassBoost.collectAsState()
    val virtualizer by viewModel.eqVirtualizer.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val activeThemeIdx by viewModel.appThemeIndex.collectAsState()
    val activeSubThemeIdx by viewModel.subThemeIndex.collectAsState()

    val presets = listOf("Normal", "Classic", "Techno", "Pop", "Jazz", "Folk", "Rock")
    val bandLabels = listOf("60Hz", "230Hz", "910Hz", "4kHz", "14kHz")
    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize()) {
        FluidBackground(themeIndex = activeThemeIdx, subThemeIndex = activeSubThemeIdx, isPlaying = isPlaying)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp)
                .padding(bottom = 60.dp)
        ) {
            // App Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.Equalizer,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = LanguageUtils.get("equalizer", lang),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                Switch(
                    checked = eqEnabled,
                    onCheckedChange = { viewModel.toggleEq(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                        uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Professional Spectrum Visualizer Canvas Curve
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                val activeColor = MaterialTheme.colorScheme.primary
                Box(modifier = Modifier.fillMaxSize()) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 24.dp)
                    ) {
                        val w = size.width
                        val h = size.height
                        val centerY = h / 2f
                        val points = ArrayList<Offset>()

                        // Calculate 5 band nodes
                        val division = w / (bands.size + 1)
                        bands.forEachIndexed { i, db ->
                            val factor = if (eqEnabled) db / 15f else 0f
                            val y = centerY - (factor * (h / 2f) * 0.85f)
                            val x = division * (i + 1)
                            points.add(Offset(x, y))
                        }

                        // Draw baseline
                        drawLine(
                            color = Color.Gray.copy(alpha = 0.2f),
                            start = Offset(0f, centerY),
                            end = Offset(w, centerY),
                            strokeWidth = 2f,
                            cap = StrokeCap.Round
                        )

                        // Smooth spline curve
                        if (points.isNotEmpty()) {
                            val path = Path()
                            path.moveTo(0f, centerY)

                            for (i in 0 until points.size) {
                                val current = points[i]
                                val prev = if (i == 0) Offset(0f, centerY) else points[i - 1]

                                val cp1X = prev.x + (current.x - prev.x) / 2f
                                val cp1 = Offset(cp1X, prev.y)
                                val cp2X = prev.x + (current.x - prev.x) / 2f
                                val cp2 = Offset(cp2X, current.y)

                                path.cubicTo(cp1.x, cp1.y, cp2.x, cp2.y, current.x, current.y)
                            }

                            path.lineTo(w, centerY)

                            val fillPath = Path().apply {
                                addPath(path)
                                lineTo(w, h)
                                lineTo(0f, h)
                                close()
                            }

                            drawPath(
                                path = fillPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        activeColor.copy(alpha = 0.35f),
                                        activeColor.copy(alpha = 0.0f)
                                    )
                                )
                            )

                            drawPath(
                                path = path,
                                color = activeColor,
                                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                            )

                            points.forEach { pt ->
                                drawCircle(
                                    color = Color.White,
                                    radius = 6.dp.toPx(),
                                    center = pt
                                )
                                drawCircle(
                                    color = activeColor,
                                    radius = 4.dp.toPx(),
                                    center = pt
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Preset List Selector
            Text(
                text = LanguageUtils.get("themes", lang),
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(presets) { preset ->
                    val isSelected = when (preset.lowercase()) {
                        "normal" -> bands.contentEquals(intArrayOf(0, 0, 0, 0, 0))
                        "classic" -> bands.contentEquals(intArrayOf(3, 2, -1, 2, 4))
                        "techno" -> bands.contentEquals(intArrayOf(5, 3, 0, 3, 5))
                        "pop" -> bands.contentEquals(intArrayOf(-2, -1, 3, 2, -1))
                        "jazz" -> bands.contentEquals(intArrayOf(3, 1, -2, 2, -1))
                        "folk" -> bands.contentEquals(intArrayOf(1, 2, 4, 1, -2))
                        "rock" -> bands.contentEquals(intArrayOf(4, 2, -3, 1, 5))
                        else -> false
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .clickable(enabled = eqEnabled) {
                                viewModel.applyPreset(preset)
                            }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = preset,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (eqEnabled) 1f else 0.4f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Vertical Equalizer Frequency Bands (ستون های عمودی تنظیم فرکانس صدا)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                bands.forEachIndexed { i, valDB ->
                    VerticalEqBandColumn(
                        label = bandLabels.getOrElse(i) { "${i + 1}" },
                        valueDb = valDB,
                        enabled = eqEnabled,
                        onValueChange = { newDb ->
                            viewModel.updateEqBand(i, newDb)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Extra Effects: Bass Boost & 3D Virtualizer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Bass Boost Card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Bass Boost",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Slider(
                            value = bassBoost.toFloat(),
                            onValueChange = { viewModel.updateBassBoost(it.toInt()) },
                            valueRange = 0f..100f,
                            enabled = eqEnabled,
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary,
                                inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                            )
                        )
                        Text(
                            text = "$bassBoost%",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.align(Alignment.End)
                        )
                    }
                }

                // Virtualizer 3D Card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "3D Surround",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Slider(
                            value = virtualizer.toFloat(),
                            onValueChange = { viewModel.updateVirtualizer(it.toInt()) },
                            valueRange = 0f..100f,
                            enabled = eqEnabled,
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary,
                                inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                            )
                        )
                        Text(
                            text = "$virtualizer%",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.align(Alignment.End)
                        )
                    }
                }
            }
        }

        // Fast Scrollbar for Equalizer screen
        FastScrollbarForScrollState(
            state = scrollState,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 4.dp, top = 80.dp, bottom = 40.dp)
        )
    }
}

/**
 * Vertical Column Equalizer Band (ستون عمودی تنظیم فرکانس صدا)
 * Provides tactile drag and tap adjustments, zero-dB center baseline indicator,
 * dynamic Aurora gradient fill, and clear dB readout badge.
 */
@Composable
fun VerticalEqBandColumn(
    label: String,
    valueDb: Int,
    enabled: Boolean,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val trackHeightDp = 180.dp
    val trackWidthDp = 44.dp
    val thumbSizeDp = 28.dp

    // dB range is -15 to +15 (span of 30 dB)
    val fraction = ((valueDb + 15) / 30f).coerceIn(0f, 1f)
    val animatedFraction by animateFloatAsState(
        targetValue = fraction,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "eq_column_fraction"
    )

    val activeTrackColor = when {
        valueDb > 5 -> Color(0xFF06B6D4) // Bright Cyan
        valueDb > 0 -> Color(0xFF10B981) // Emerald Green
        valueDb < -5 -> Color(0xFFF43F5E) // Coral / Rose
        valueDb < 0 -> Color(0xFFA855F7) // Purple
        else -> MaterialTheme.colorScheme.primary
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // dB Readout Badge
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(
                    if (valueDb != 0 && enabled) activeTrackColor.copy(alpha = 0.18f)
                    else MaterialTheme.colorScheme.surfaceVariant
                )
                .padding(horizontal = 8.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${if (valueDb > 0) "+" else ""}$valueDb dB",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (enabled) {
                    if (valueDb != 0) activeTrackColor else MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Vertical Slider Column Track
        BoxWithConstraints(
            modifier = Modifier
                .height(trackHeightDp)
                .width(trackWidthDp)
                .clip(RoundedCornerShape(22.dp))
                .background(Color.White.copy(alpha = 0.05f))
                .border(
                    width = 1.dp,
                    color = if (enabled) activeTrackColor.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.06f),
                    shape = RoundedCornerShape(22.dp)
                )
                .pointerInput(enabled) {
                    if (!enabled) return@pointerInput
                    detectTapGestures(
                        onDoubleTap = {
                            onValueChange(0) // Quick reset to 0 dB
                        },
                        onTap = { offset ->
                            val totalH = size.height.toFloat()
                            val thumbHalf = with(density) { (thumbSizeDp / 2).toPx() }
                            val effectiveH = (totalH - (thumbHalf * 2)).coerceAtLeast(1f)
                            val clampedY = (offset.y - thumbHalf).coerceIn(0f, effectiveH)
                            val f = (1f - (clampedY / effectiveH)).coerceIn(0f, 1f)
                            val newDb = (-15 + (f * 30f)).roundToInt().coerceIn(-15, 15)
                            onValueChange(newDb)
                        }
                    )
                }
                .pointerInput(enabled) {
                    if (!enabled) return@pointerInput
                    detectVerticalDragGestures { change, _ ->
                        change.consume()
                        val totalH = size.height.toFloat()
                        val thumbHalf = with(density) { (thumbSizeDp / 2).toPx() }
                        val effectiveH = (totalH - (thumbHalf * 2)).coerceAtLeast(1f)
                        val clampedY = (change.position.y - thumbHalf).coerceIn(0f, effectiveH)
                        val f = (1f - (clampedY / effectiveH)).coerceIn(0f, 1f)
                        val newDb = (-15 + (f * 30f)).roundToInt().coerceIn(-15, 15)
                        onValueChange(newDb)
                    }
                },
            contentAlignment = Alignment.TopCenter
        ) {
            val totalH = maxHeight
            val thumbHalf = thumbSizeDp / 2
            val travelH = totalH - thumbSizeDp

            // Center baseline (0 dB) and subtle scale ticks
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val midY = h / 2f

                // 0 dB Zero level indicator line
                drawLine(
                    color = Color.White.copy(alpha = if (enabled) 0.35f else 0.15f),
                    start = Offset(6.dp.toPx(), midY),
                    end = Offset(w - 6.dp.toPx(), midY),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Minor tick marks
                val tickSteps = listOf(0.166f, 0.333f, 0.666f, 0.833f)
                tickSteps.forEach { step ->
                    val y = h * step
                    drawLine(
                        color = Color.White.copy(alpha = 0.12f),
                        start = Offset(12.dp.toPx(), y),
                        end = Offset(w - 12.dp.toPx(), y),
                        strokeWidth = 1.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }

            // Fill Bar between 0 dB (center) and current value
            val centerOffset = totalH / 2
            val thumbOffset = travelH * (1f - animatedFraction) + thumbHalf

            if (enabled && valueDb != 0) {
                if (valueDb > 0) {
                    // Positive boost fill (center upwards to thumb)
                    val fillTop = thumbOffset
                    val fillHeight = (centerOffset - fillTop).coerceAtLeast(0.dp)
                    Box(
                        modifier = Modifier
                            .padding(top = fillTop)
                            .height(fillHeight)
                            .width(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        activeTrackColor.copy(alpha = 0.9f),
                                        activeTrackColor.copy(alpha = 0.4f)
                                    )
                                )
                            )
                    )
                } else {
                    // Negative cut fill (center downwards to thumb)
                    val fillTop = centerOffset
                    val fillHeight = (thumbOffset - centerOffset).coerceAtLeast(0.dp)
                    Box(
                        modifier = Modifier
                            .padding(top = fillTop)
                            .height(fillHeight)
                            .width(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        activeTrackColor.copy(alpha = 0.4f),
                                        activeTrackColor.copy(alpha = 0.9f)
                                    )
                                )
                            )
                    )
                }
            }

            // Tactile Column Thumb Knob
            Box(
                modifier = Modifier
                    .offset(y = travelH * (1f - animatedFraction))
                    .size(thumbSizeDp)
                    .clip(CircleShape)
                    .background(
                        if (enabled) {
                            Brush.radialGradient(
                                colors = listOf(
                                    Color.White,
                                    activeTrackColor
                                )
                            )
                        } else {
                            Brush.radialGradient(
                                colors = listOf(
                                    Color.Gray,
                                    Color.DarkGray
                                )
                            )
                        }
                    )
                    .border(
                        width = 2.dp,
                        color = if (enabled) Color.White else Color.Gray.copy(alpha = 0.4f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Tactile center grip dot
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (enabled) activeTrackColor else Color.Black.copy(alpha = 0.5f))
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Frequency Label
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
        )
    }
}
