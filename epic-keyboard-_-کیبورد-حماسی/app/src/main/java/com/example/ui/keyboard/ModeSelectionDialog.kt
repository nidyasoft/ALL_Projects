package com.example.ui.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.settings.KeyboardMode
import com.example.settings.KeyboardSettingsState

@Composable
fun ModeSelectionDialog(
    settings: KeyboardSettingsState,
    onSelectMode: (KeyboardMode) -> Unit,
    onClose: () -> Unit
) {
    val colors = settings.getColorTokens()

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = colors.background),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "انتخاب حالت نمایش کیبورد",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textColor
                    )
                    IconButton(onClick = onClose, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "بستن",
                            tint = colors.textColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 4 Keyboard Modes
                KeyboardMode.entries.forEach { mode ->
                    val isSelected = settings.mode == mode
                    val icon: ImageVector = when (mode) {
                        KeyboardMode.STANDARD -> Icons.Default.Fullscreen
                        KeyboardMode.ONE_HANDED_LEFT -> Icons.AutoMirrored.Filled.ArrowBack
                        KeyboardMode.ONE_HANDED_RIGHT -> Icons.AutoMirrored.Filled.ArrowForward
                        KeyboardMode.FLOATING -> Icons.Default.OpenWith
                    }

                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                onSelectMode(mode)
                                onClose()
                            },
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = if (isSelected) colors.specialKeyBackground.copy(alpha = 0.85f)
                            else colors.keyBackground.copy(alpha = 0.5f)
                        ),
                        border = CardDefaults.outlinedCardBorder(isSelected)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (isSelected) colors.accentColor else colors.textColor,
                                modifier = Modifier.size(24.dp)
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = mode.titlePersian,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) colors.accentColor else colors.textColor
                                )
                                Text(
                                    text = mode.descriptionPersian,
                                    fontSize = 11.sp,
                                    color = colors.textColor.copy(alpha = 0.7f)
                                )
                            }

                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = colors.accentColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
