package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

enum class AppThemeMode {
    DARK,
    LIGHT,
    SYSTEM
}

data class EpicThemePalette(
    val isDark: Boolean,
    val background: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val surfaceVariant: Color,
    val border: Color,
    val primaryText: Color,
    val secondaryText: Color,
    val mutedText: Color,
    val accentCyan: Color,
    val accentPurple: Color,
    val accentAmber: Color,
    val accentPink: Color,
    val accentRed: Color,
    val accentGreen: Color
)

val DarkPalette = EpicThemePalette(
    isDark = true,
    background = Color(0xFF05070B),
    surface = Color(0xFF0E131E),
    surfaceElevated = Color(0xFF1F293D),
    surfaceVariant = Color(0xFF161E2E),
    border = Color(0xFF26334D),
    primaryText = Color(0xFFF1F5F9),
    secondaryText = Color(0xFF94A3B8),
    mutedText = Color(0xFF64748B),
    accentCyan = Color(0xFF00F2FE),
    accentPurple = Color(0xFF9D4EDD),
    accentAmber = Color(0xFFFFB703),
    accentPink = Color(0xFFFF2A85),
    accentRed = Color(0xFFFF4B4B),
    accentGreen = Color(0xFF00F5A0)
)

val LightPalette = EpicThemePalette(
    isDark = false,
    background = Color(0xFFF8FAFC),
    surface = Color(0xFFFFFFFF),
    surfaceElevated = Color(0xFFF1F5F9),
    surfaceVariant = Color(0xFFE2E8F0),
    border = Color(0xFFCBD5E1),
    primaryText = Color(0xFF0F172A),
    secondaryText = Color(0xFF1E293B),
    mutedText = Color(0xFF475569),
    accentCyan = Color(0xFF0284C7),
    accentPurple = Color(0xFF6D28D9),
    accentAmber = Color(0xFFB45309),
    accentPink = Color(0xFFBE185D),
    accentRed = Color(0xFFB91C1C),
    accentGreen = Color(0xFF15803D)
)

val LocalEpicTheme = staticCompositionLocalOf { DarkPalette }

object EpicTheme {
    val colors: EpicThemePalette
        @Composable
        @ReadOnlyComposable
        get() = LocalEpicTheme.current
}

private val DarkM3ColorScheme = darkColorScheme(
    primary = Color(0xFF00F2FE),
    onPrimary = Color(0xFF05070B),
    primaryContainer = Color(0xFF1F293D),
    onPrimaryContainer = Color(0xFF00F2FE),
    secondary = Color(0xFF9D4EDD),
    onSecondary = Color(0xFFF1F5F9),
    secondaryContainer = Color(0xFF161E2E),
    onSecondaryContainer = Color(0xFF9D4EDD),
    tertiary = Color(0xFFFFB703),
    onTertiary = Color(0xFF05070B),
    tertiaryContainer = Color(0xFF161E2E),
    onTertiaryContainer = Color(0xFFFFB703),
    background = Color(0xFF05070B),
    onBackground = Color(0xFFF1F5F9),
    surface = Color(0xFF0E131E),
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = Color(0xFF161E2E),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF26334D),
    error = Color(0xFFFF4B4B),
    onError = Color(0xFFF1F5F9)
)

private val LightM3ColorScheme = lightColorScheme(
    primary = Color(0xFF0284C7),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = Color(0xFF0369A1),
    secondary = Color(0xFF7C3AED),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEDE9FE),
    onSecondaryContainer = Color(0xFF6D28D9),
    tertiary = Color(0xFFD97706),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFEF3C7),
    onTertiaryContainer = Color(0xFFB45309),
    background = Color(0xFFF1F5F9),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF8FAFC),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1),
    error = Color(0xFFDC2626),
    onError = Color.White
)

@Composable
fun EpicAlarmClockTheme(
    themeMode: AppThemeMode = AppThemeMode.DARK,
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        AppThemeMode.DARK -> true
        AppThemeMode.LIGHT -> false
        AppThemeMode.SYSTEM -> isSystemDark
    }

    val palette = if (isDark) DarkPalette else LightPalette
    val m3ColorScheme = if (isDark) DarkM3ColorScheme else LightM3ColorScheme

    CompositionLocalProvider(LocalEpicTheme provides palette) {
        MaterialTheme(
            colorScheme = m3ColorScheme,
            typography = Typography,
            content = content
        )
    }
}
