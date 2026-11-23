package com.example.ui.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.settings.KeyboardSettingsState

@Composable
fun NumPadView(
    settings: KeyboardSettingsState,
    onKeyClick: (String) -> Unit,
    onBackspace: () -> Unit,
    onDeleteForward: () -> Unit,
    onEnter: () -> Unit,
    onHome: () -> Unit,
    onEnd: () -> Unit,
    onHomeAll: () -> Unit,
    onEndAll: () -> Unit,
    onCloseNumPad: () -> Unit,
    modifier: Modifier = Modifier
) {
    var digitScript by remember { mutableStateOf("EN") } // EN or FA

    val colors = settings.getColorTokens()

    val digits = remember(digitScript) {
        if (digitScript == "FA") {
            listOf(
                listOf("۷", "۸", "۹"),
                listOf("۴", "۵", "۶"),
                listOf("۱", "۲", "۳"),
                listOf("٫", "۰", "،", "۰۰")
            )
        } else {
            listOf(
                listOf("7", "8", "9"),
                listOf("4", "5", "6"),
                listOf("1", "2", "3"),
                listOf(".", "0", ",", "00")
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.background)
            .padding(8.dp)
    ) {
        // Navigation row: Home, End, Home All, End All + Digit script toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = { digitScript = if (digitScript == "EN") "FA" else "EN" },
                modifier = Modifier.height(36.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (digitScript == "EN") "۱۲۳" else "123",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textColor
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            OutlinedButton(
                onClick = onHome,
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Home", fontSize = 11.sp, color = colors.textColor)
            }

            OutlinedButton(
                onClick = onEnd,
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("End", fontSize = 11.sp, color = colors.textColor)
            }

            OutlinedButton(
                onClick = onHomeAll,
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("HomeAll", fontSize = 10.sp, color = colors.textColor)
            }

            OutlinedButton(
                onClick = onEndAll,
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("EndAll", fontSize = 10.sp, color = colors.textColor)
            }
        }

        // 4 Rows of 3/4 digits + right action column (Del, Backspace, Enter, ABC)
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.weight(3f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                digits.forEach { row ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        row.forEach { d ->
                            KeyButton(
                                key = KeyModel(label = d, output = d),
                                modifier = Modifier.weight(1f),
                                fontSizeSp = settings.fontSizeNumbersSp + 2f,
                                hapticEnabled = settings.hapticEnabled,
                                hapticDurationMs = settings.hapticDurationMs,
                                soundEnabled = settings.soundEnabled,
                                customKeyBg = colors.keyBackground,
                                customSpecialKeyBg = colors.specialKeyBackground,
                                customTextColor = colors.textColor,
                                customAccent = colors.accentColor,
                                onClick = onKeyClick
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Action Column: Del, Backspace, Enter, ABC (Back) — all identical size and shape
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                KeyButton(
                    key = KeyModel(label = "Del", type = KeyType.DEL),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    fontSizeSp = 14f,
                    hapticEnabled = settings.hapticEnabled,
                    hapticDurationMs = settings.hapticDurationMs,
                    soundEnabled = settings.soundEnabled,
                    customKeyBg = colors.specialKeyBackground,
                    customSpecialKeyBg = colors.specialKeyBackground,
                    customTextColor = colors.textColor,
                    customAccent = colors.accentColor,
                    onClick = {},
                    onSpecialAction = { onDeleteForward() }
                )

                KeyButton(
                    key = KeyModel(label = "⌫", type = KeyType.BACKSPACE),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    fontSizeSp = 18f,
                    hapticEnabled = settings.hapticEnabled,
                    hapticDurationMs = settings.hapticDurationMs,
                    soundEnabled = settings.soundEnabled,
                    customKeyBg = colors.specialKeyBackground,
                    customSpecialKeyBg = colors.specialKeyBackground,
                    customTextColor = colors.textColor,
                    customAccent = colors.accentColor,
                    onClick = {},
                    onSpecialAction = { onBackspace() }
                )

                KeyButton(
                    key = KeyModel(label = "↵", type = KeyType.ENTER),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    fontSizeSp = 20f,
                    isHighlighted = true,
                    hapticEnabled = settings.hapticEnabled,
                    hapticDurationMs = settings.hapticDurationMs,
                    soundEnabled = settings.soundEnabled,
                    customKeyBg = colors.keyBackground,
                    customSpecialKeyBg = colors.specialKeyBackground,
                    customTextColor = colors.textColor,
                    customAccent = colors.accentColor,
                    onClick = {},
                    onSpecialAction = { onEnter() }
                )

                KeyButton(
                    key = KeyModel(label = "ABC", type = KeyType.SYMBOLS_TOGGLE),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    fontSizeSp = 14f,
                    isHighlighted = false,
                    hapticEnabled = settings.hapticEnabled,
                    hapticDurationMs = settings.hapticDurationMs,
                    soundEnabled = settings.soundEnabled,
                    customKeyBg = colors.specialKeyBackground,
                    customSpecialKeyBg = colors.specialKeyBackground,
                    customTextColor = colors.textColor,
                    customAccent = colors.accentColor,
                    onClick = { onCloseNumPad() },
                    onSpecialAction = { onCloseNumPad() }
                )
            }
        }
    }
}
