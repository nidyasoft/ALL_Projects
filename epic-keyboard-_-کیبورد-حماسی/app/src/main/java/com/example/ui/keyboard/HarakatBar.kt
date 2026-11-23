package com.example.ui.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.settings.KeyboardSettingsState

@Composable
fun HarakatBar(
    settings: KeyboardSettingsState,
    onKeyClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (!settings.harakatTopRowEnabled && !settings.harakatBottomRowEnabled) return

    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    val colors = settings.getColorTokens()
    val activeFont = settings.getActiveFont().toFontFamily()
    val rowHeight = if (isLandscape) {
        32.dp
    } else {
        (settings.fontSizeHarakatSp * 1.85f + 10f).dp.coerceIn(42.dp, 58.dp)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.background.copy(alpha = 0.75f))
            .padding(horizontal = 2.dp, vertical = 2.dp)
    ) {
        // Top row (8 keys: ـٓ / ـٔ / ـ / ـٕ / ـٰ / « / » / ٫)
        if (settings.harakatTopRowEnabled) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(rowHeight)
                    .padding(vertical = 1.dp)
            ) {
                KeyboardLayouts.HARAKAT_TOP_ROW.forEach { key ->
                    KeyButton(
                        key = key,
                        modifier = Modifier.weight(1f),
                        fontSizeSp = settings.fontSizeHarakatSp,
                        fontFamily = activeFont,
                        isRtl = true,
                        rtlMirrorBrackets = settings.rtlMirrorBrackets,
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

        // Bottom row (8 keys in reversed order: ـٍ / ـٌ / ـً / ـْ / ـّ / ـِ / ـُ / ـَ)
        if (settings.harakatBottomRowEnabled) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(rowHeight)
                    .padding(vertical = 1.dp)
            ) {
                KeyboardLayouts.HARAKAT_BOTTOM_ROW.forEach { key ->
                    KeyButton(
                        key = key,
                        modifier = Modifier.weight(1f),
                        fontSizeSp = settings.fontSizeHarakatSp,
                        fontFamily = activeFont,
                        isRtl = true,
                        rtlMirrorBrackets = settings.rtlMirrorBrackets,
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
}
