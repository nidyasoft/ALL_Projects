package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.EpicTheme
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonAmber

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ColorPickerDialog(
    initialColorLong: Long,
    title: String,
    onDismiss: () -> Unit,
    onColorSelected: (Long) -> Unit
) {
    val theme = EpicTheme.colors

    // 32 Curated Presets across 4 popular categories
    val presets = remember {
        listOf(
            // Category 1: نئون و درخشان (Vibrant Neons)
            0xFF00E5FF, 0xFFD500F9, 0xFFFF3366, 0xFFFFAB00,
            0xFF00E676, 0xFF2979FF, 0xFFFF1744, 0xFFFFEA00,
            // Category 2: کلاسیک و متالیک (Classic & Metallic)
            0xFFFFFFFF, 0xFFE0E6ED, 0xFF94A3B8, 0xFF475569,
            0xFF334155, 0xFFFFD700, 0xFFD97706, 0xFFF43F5E,
            // Category 3: شب و اولد (OLED & Midnight)
            0xFF000000, 0xFF0B0E14, 0xFF0F172A, 0xFF1E1B4B,
            0xFF064E3B, 0xFF4A044E, 0xFF3B0764, 0xFF271C19,
            // Category 4: پاستلی و آرام (Pastel & Modern)
            0xFF38BDF8, 0xFF4ADE80, 0xFFFB7185, 0xFFC084FC,
            0xFFFBBF24, 0xFF2DD4BF, 0xFFFDBA74, 0xFF67E8F9
        )
    }

    var selectedTab by remember { mutableIntStateOf(0) } // 0: 32 Presets, 1: 16 Million Spectrum

    // Color Components
    val initialColor = Color(initialColorLong)
    var r by remember { mutableFloatStateOf(initialColor.red) }
    var g by remember { mutableFloatStateOf(initialColor.green) }
    var b by remember { mutableFloatStateOf(initialColor.blue) }

    val currentColor = Color(red = r, green = g, blue = b, alpha = 1f)
    var hexInput by remember { mutableStateOf(String.format("%06X", (currentColor.toArgb() and 0xFFFFFF))) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 24.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = theme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(imageVector = Icons.Default.Palette, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(22.dp))
                        Text(
                            text = title,
                            color = theme.primaryText,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = theme.secondaryText)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Current vs Selected Color Preview Banner
                Card(
                    colors = CardDefaults.cardColors(containerColor = theme.surfaceElevated),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, theme.border, RoundedCornerShape(16.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        // Previous
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "رنگ قبلی", color = theme.secondaryText, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color(initialColorLong))
                                    .border(2.dp, theme.border, CircleShape)
                            )
                        }

                        // Arrow
                        Text(text = "➜", color = NeonCyan, fontSize = 20.sp, fontWeight = FontWeight.Bold)

                        // New
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "رنگ جدید انتخابی", color = theme.primaryText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(currentColor)
                                    .border(2.dp, NeonCyan, CircleShape)
                            )
                        }

                        // Hex code badge
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "کد رنگ (HEX)", color = theme.secondaryText, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(theme.surfaceVariant)
                                    .border(1.dp, theme.border, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "#$hexInput",
                                    color = NeonAmber,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Tabs: 32 Presets vs 16-Million Custom
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = theme.surfaceElevated,
                    contentColor = NeonCyan,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = NeonCyan
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                text = "۳۲ رنگ اصلی آماده",
                                fontSize = 13.sp,
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                text = "پالت ۱۶ میلیون رنگ",
                                fontSize = 13.sp,
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (selectedTab == 0) {
                    // TAB 0: 32 PRESETS
                    Text(
                        text = "یکی از رنگ‌های استاندارد و هماهنگ را انتخاب کنید:",
                        color = theme.secondaryText,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        presets.forEach { presetHex ->
                            val presetColor = Color(presetHex)
                            val isMatch = (currentColor.toArgb() and 0xFFFFFF) == (presetHex.toInt() and 0xFFFFFF)
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(presetColor)
                                    .border(
                                        width = if (isMatch) 3.dp else 1.dp,
                                        color = if (isMatch) NeonCyan else theme.border,
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        r = presetColor.red
                                        g = presetColor.green
                                        b = presetColor.blue
                                        hexInput = String.format("%06X", (presetHex.toInt() and 0xFFFFFF))
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isMatch) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = if (presetColor.red > 0.6f && presetColor.green > 0.6f) Color.Black else Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // TAB 1: 16-MILLION COLOR PICKER (RGB + Spectrum)
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "تنظیم دقیق رنگ دلخواه از طیف ۱۶ میلیون رنگ:",
                            color = theme.secondaryText,
                            fontSize = 12.sp
                        )

                        // Red Slider
                        ColorSliderRow(
                            label = "قرمز (Red)",
                            value = r,
                            color = Color.Red,
                            onValueChange = {
                                r = it
                                hexInput = String.format("%06X", (Color(r, g, b).toArgb() and 0xFFFFFF))
                            }
                        )

                        // Green Slider
                        ColorSliderRow(
                            label = "سبز (Green)",
                            value = g,
                            color = Color.Green,
                            onValueChange = {
                                g = it
                                hexInput = String.format("%06X", (Color(r, g, b).toArgb() and 0xFFFFFF))
                            }
                        )

                        // Blue Slider
                        ColorSliderRow(
                            label = "آبی (Blue)",
                            value = b,
                            color = Color.Blue,
                            onValueChange = {
                                b = it
                                hexInput = String.format("%06X", (Color(r, g, b).toArgb() and 0xFFFFFF))
                            }
                        )

                        // Direct HEX Input
                        OutlinedTextField(
                            value = hexInput,
                            onValueChange = { input ->
                                val clean = input.filter { it.isLetterOrDigit() }.take(6).uppercase()
                                hexInput = clean
                                if (clean.length == 6) {
                                    try {
                                        val parsedLong = clean.toLong(16)
                                        r = ((parsedLong shr 16) and 0xFF) / 255f
                                        g = ((parsedLong shr 8) and 0xFF) / 255f
                                        b = (parsedLong and 0xFF) / 255f
                                    } catch (_: Throwable) {}
                                }
                            },
                            label = { Text("ورود مستقیم کد HEX رنگ") },
                            leadingIcon = { Text("#", color = NeonCyan, fontWeight = FontWeight.Bold) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = theme.border,
                                focusedTextColor = theme.primaryText,
                                unfocusedTextColor = theme.primaryText
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Actions: Cancel / Confirm
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = theme.secondaryText)
                    ) {
                        Text("انصراف")
                    }

                    Button(
                        onClick = {
                            val selectedArgb = (0xFFL shl 24) or
                                    (((r * 255).toInt().coerceIn(0, 255).toLong()) shl 16) or
                                    (((g * 255).toInt().coerceIn(0, 255).toLong()) shl 8) or
                                    ((b * 255).toInt().coerceIn(0, 255).toLong())
                            onColorSelected(selectedArgb)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1.4f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonCyan,
                            contentColor = Color.Black
                        )
                    ) {
                        Text("تایید و اعمال رنگ", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorSliderRow(
    label: String,
    value: Float,
    color: Color,
    onValueChange: (Float) -> Unit
) {
    val theme = EpicTheme.colors
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, color = theme.primaryText, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Text(text = "${(value * 255).toInt()}", color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            colors = SliderDefaults.colors(
                thumbColor = color,
                activeTrackColor = color,
                inactiveTrackColor = theme.surfaceElevated
            )
        )
    }
}
