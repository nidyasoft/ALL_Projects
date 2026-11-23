package com.example.ui.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.FirstPage
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.LastPage
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.settings.KeyboardMode
import com.example.settings.KeyboardSettingsState

@Composable
fun ActionToolbar(
    settings: KeyboardSettingsState,
    onUndo: () -> Unit,
    onCopy: () -> Unit,
    onCopyAndPin: () -> Unit,
    onPaste: () -> Unit,
    onCut: () -> Unit,
    onSelectAll: () -> Unit,
    onDeleteForward: () -> Unit,
    onHome: () -> Unit,
    onEnd: () -> Unit,
    onHomeAll: () -> Unit,
    onEndAll: () -> Unit,
    onToggleNumPad: () -> Unit,
    onOpenClipboard: () -> Unit,
    onToggleMode: () -> Unit,
    onSelectMode: (KeyboardMode) -> Unit = {},
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val colors = settings.getColorTokens()
    var isModeMenuOpen by remember { mutableStateOf(false) }
    val activeFont = settings.getActiveFont().toFontFamily()

    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    val toolbarHeight = if (isLandscape) 32.dp else (settings.fontSizeToolbarSp * 1.8f + 16f).dp
    val iconSize = if (isLandscape) 18.dp else (settings.fontSizeToolbarSp * 1.25f + 4f).dp
    val buttonSize = if (isLandscape) 28.dp else (settings.fontSizeToolbarSp * 1.7f + 14f).dp

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(toolbarHeight)
            .background(colors.specialKeyBackground.copy(alpha = 0.5f))
            .horizontalScroll(scrollState)
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Undo
        if (settings.undoEnabled) {
            ActionToolbarButton(
                icon = Icons.AutoMirrored.Filled.Undo,
                label = "Undo",
                testTag = "action_undo",
                size = buttonSize,
                iconSize = iconSize,
                iconTint = colors.textColor,
                onClick = onUndo
            )
        }

        // Copy
        if (settings.copyKeyEnabled) {
            ActionToolbarButton(
                icon = Icons.Default.ContentCopy,
                label = "Copy",
                testTag = "action_copy",
                size = buttonSize,
                iconSize = iconSize,
                iconTint = colors.textColor,
                onClick = onCopy
            )
        }

        // Copy & Pin
        if (settings.copyPinKeyEnabled) {
            ActionToolbarButton(
                icon = Icons.Default.PushPin,
                label = "Copy&Pin",
                testTag = "action_copy_pin",
                size = buttonSize,
                iconSize = iconSize,
                iconTint = colors.accentColor,
                onClick = onCopyAndPin
            )
        }

        // Paste
        if (settings.pasteKeyEnabled) {
            ActionToolbarButton(
                icon = Icons.Default.ContentPaste,
                label = "Paste",
                testTag = "action_paste",
                size = buttonSize,
                iconSize = iconSize,
                iconTint = colors.textColor,
                onClick = onPaste
            )
        }

        // Cut
        if (settings.cutKeyEnabled) {
            ActionToolbarButton(
                icon = Icons.Default.ContentCut,
                label = "Cut",
                testTag = "action_cut",
                size = buttonSize,
                iconSize = iconSize,
                iconTint = colors.textColor,
                onClick = onCut
            )
        }

        // Select All
        if (settings.selectAllKeyEnabled) {
            ActionToolbarButton(
                icon = Icons.Default.SelectAll,
                label = "Select All",
                testTag = "action_select_all",
                size = buttonSize,
                iconSize = iconSize,
                iconTint = colors.textColor,
                onClick = onSelectAll
            )
        }

        // Clipboard Manager
        if (settings.clipboardEnabled) {
            ActionToolbarButton(
                icon = Icons.Default.Assignment,
                label = "Clipboard",
                testTag = "action_clipboard",
                size = buttonSize,
                iconSize = iconSize,
                iconTint = colors.textColor,
                onClick = onOpenClipboard
            )
        }

        // Del (Forward Delete)
        if (settings.delKeyEnabled) {
            ActionToolbarTextButton(
                text = "Del",
                testTag = "action_del",
                fontSizeSp = settings.fontSizeToolbarSp,
                fontFamily = activeFont,
                textColor = colors.textColor,
                bgColor = colors.keyBackground,
                onClick = onDeleteForward
            )
        }

        // Navigation Keys
        if (settings.homeEndKeysEnabled) {
            ActionToolbarTextButton(
                text = "Home",
                testTag = "action_home",
                fontSizeSp = settings.fontSizeToolbarSp,
                fontFamily = activeFont,
                textColor = colors.textColor,
                bgColor = colors.keyBackground,
                onClick = onHome
            )
            ActionToolbarTextButton(
                text = "End",
                testTag = "action_end",
                fontSizeSp = settings.fontSizeToolbarSp,
                fontFamily = activeFont,
                textColor = colors.textColor,
                bgColor = colors.keyBackground,
                onClick = onEnd
            )
            ActionToolbarButton(
                icon = Icons.Default.FirstPage,
                label = "Home All",
                testTag = "action_home_all",
                size = buttonSize,
                iconSize = iconSize,
                iconTint = colors.textColor,
                onClick = onHomeAll
            )
            ActionToolbarButton(
                icon = Icons.Default.LastPage,
                label = "End All",
                testTag = "action_end_all",
                size = buttonSize,
                iconSize = iconSize,
                iconTint = colors.textColor,
                onClick = onEndAll
            )
        }

        // NumPad Toggle
        if (settings.numPadKeyEnabled) {
            ActionToolbarButton(
                icon = Icons.Default.Dialpad,
                label = "NumPad",
                testTag = "action_numpad",
                size = buttonSize,
                iconSize = iconSize,
                iconTint = colors.textColor,
                onClick = onToggleNumPad
            )
        }

        // Layout Mode Switcher with compact dropdown menu
        Box {
            ActionToolbarButton(
                icon = when (settings.mode) {
                    KeyboardMode.STANDARD -> Icons.Default.Keyboard
                    KeyboardMode.FLOATING -> Icons.Default.OpenWith
                    KeyboardMode.ONE_HANDED_LEFT -> Icons.AutoMirrored.Filled.ArrowBack
                    KeyboardMode.ONE_HANDED_RIGHT -> Icons.AutoMirrored.Filled.ArrowForward
                },
                label = "Layout",
                testTag = "action_mode",
                size = buttonSize,
                iconSize = iconSize,
                iconTint = if (settings.mode != KeyboardMode.STANDARD) colors.accentColor else colors.textColor,
                onClick = { isModeMenuOpen = true }
            )

            DropdownMenu(
                expanded = isModeMenuOpen,
                onDismissRequest = { isModeMenuOpen = false },
                modifier = Modifier
                    .background(colors.background)
                    .border(1.dp, colors.specialKeyBackground, RoundedCornerShape(10.dp))
            ) {
                KeyboardMode.entries.forEach { mode ->
                    val isSelected = settings.mode == mode
                    val modeIcon = when (mode) {
                        KeyboardMode.STANDARD -> Icons.Default.Fullscreen
                        KeyboardMode.ONE_HANDED_LEFT -> Icons.AutoMirrored.Filled.ArrowBack
                        KeyboardMode.ONE_HANDED_RIGHT -> Icons.AutoMirrored.Filled.ArrowForward
                        KeyboardMode.FLOATING -> Icons.Default.OpenWith
                    }

                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = modeIcon,
                                    contentDescription = null,
                                    tint = if (isSelected) colors.accentColor else colors.textColor,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = mode.titlePersian,
                                    fontSize = 13.sp,
                                    fontFamily = activeFont,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) colors.accentColor else colors.textColor
                                )
                                if (isSelected) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = colors.accentColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        },
                        onClick = {
                            onSelectMode(mode)
                            isModeMenuOpen = false
                        }
                    )
                }
            }
        }

        // Settings
        ActionToolbarButton(
            icon = Icons.Default.Settings,
            label = "Settings",
            testTag = "action_settings",
            size = buttonSize,
            iconSize = iconSize,
            iconTint = colors.textColor,
            onClick = onOpenSettings
        )
    }
}

@Composable
private fun ActionToolbarButton(
    icon: ImageVector,
    label: String,
    testTag: String,
    size: androidx.compose.ui.unit.Dp,
    iconSize: androidx.compose.ui.unit.Dp,
    iconTint: Color,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(size)
            .padding(2.dp)
            .testTag(testTag)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconTint,
            modifier = Modifier.size(iconSize)
        )
    }
}

@Composable
private fun ActionToolbarTextButton(
    text: String,
    testTag: String,
    fontSizeSp: Float,
    fontFamily: FontFamily?,
    textColor: Color,
    bgColor: Color,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .padding(horizontal = 2.dp)
            .height((fontSizeSp * 2f + 8f).dp)
            .testTag(testTag)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(bgColor)
                .padding(horizontal = 7.dp, vertical = 3.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = fontSizeSp.sp,
                fontFamily = fontFamily,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}
