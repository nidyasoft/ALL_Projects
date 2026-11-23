package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

// Dynamic Theme Colors wired to LocalEpicTheme / EpicTheme.colors
val OledBackground: Color
    @Composable
    @ReadOnlyComposable
    get() = EpicTheme.colors.background

val OledSurface: Color
    @Composable
    @ReadOnlyComposable
    get() = EpicTheme.colors.surface

val OledSurfaceVariant: Color
    @Composable
    @ReadOnlyComposable
    get() = EpicTheme.colors.surfaceVariant

val OledSurfaceElevated: Color
    @Composable
    @ReadOnlyComposable
    get() = EpicTheme.colors.surfaceElevated

val OledBorder: Color
    @Composable
    @ReadOnlyComposable
    get() = EpicTheme.colors.border

// Dynamic Accent Colors (Fluorescent in dark mode, high-contrast deep tones in light mode)
val NeonCyan: Color
    @Composable
    @ReadOnlyComposable
    get() = EpicTheme.colors.accentCyan

val NeonCyanDark: Color
    @Composable
    @ReadOnlyComposable
    get() = if (EpicTheme.colors.isDark) Color(0xFF00838F) else Color(0xFF0369A1)

val NeonPurple: Color
    @Composable
    @ReadOnlyComposable
    get() = EpicTheme.colors.accentPurple

val NeonPink: Color
    @Composable
    @ReadOnlyComposable
    get() = EpicTheme.colors.accentPink

val NeonAmber: Color
    @Composable
    @ReadOnlyComposable
    get() = EpicTheme.colors.accentAmber

val NeonGreen: Color
    @Composable
    @ReadOnlyComposable
    get() = EpicTheme.colors.accentGreen

val NeonRed: Color
    @Composable
    @ReadOnlyComposable
    get() = EpicTheme.colors.accentRed

// Dynamic Text Colors
val TextPrimary: Color
    @Composable
    @ReadOnlyComposable
    get() = EpicTheme.colors.primaryText

val TextSecondary: Color
    @Composable
    @ReadOnlyComposable
    get() = EpicTheme.colors.secondaryText

val TextMuted: Color
    @Composable
    @ReadOnlyComposable
    get() = EpicTheme.colors.mutedText

// Clock Hands
val HandSecondColor = Color(0xFFFF3366)
val HandMinuteColor = Color(0xFF00F2FE)
val HandHourColor = Color(0xFFB388FF)
