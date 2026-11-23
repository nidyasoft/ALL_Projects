package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Theme 1: Golden Phoenix
private val GoldenPhoenixDark = darkColorScheme(
    primary = GoldPrimary,
    secondary = GoldSecondary,
    tertiary = GoldSecondary,
    background = GoldBackground,
    surface = GoldSurface,
    onPrimary = GoldOnPrimary,
    onSecondary = GoldOnPrimary,
    onBackground = GoldOnBackground,
    onSurface = GoldOnBackground,
    surfaceVariant = Color(0xFF1E1E1E),
    onSurfaceVariant = Color(0xFFCCCCCC)
)
private val GoldenPhoenixLight = lightColorScheme(
    primary = Color(0xFFB8860B),
    secondary = Color(0xFFD4AF37),
    tertiary = Color(0xFF8B6508),
    background = Color(0xFFFFFDF8),
    surface = Color(0xFFFFF8EA),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color(0xFF2C1E0A),
    onSurface = Color(0xFF2C1E0A),
    surfaceVariant = Color(0xFFF5ECCB),
    onSurfaceVariant = Color(0xFF5C4312)
)

// Theme 2: Cosmic Nebula
private val CosmicNebulaDark = darkColorScheme(
    primary = CosmicPrimary,
    secondary = CosmicSecondary,
    tertiary = CosmicSecondary,
    background = CosmicBackground,
    surface = CosmicSurface,
    onPrimary = CosmicOnPrimary,
    onSecondary = CosmicOnPrimary,
    onBackground = CosmicOnBackground,
    onSurface = CosmicOnBackground,
    surfaceVariant = Color(0xFF16102D),
    onSurfaceVariant = CosmicOnBackground
)
private val CosmicNebulaLight = lightColorScheme(
    primary = Color(0xFF6200EE),
    secondary = Color(0xFF9D4EDD),
    tertiary = Color(0xFF7B2CBF),
    background = Color(0xFFF6F4FE),
    surface = Color(0xFFECE6FE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF160B30),
    onSurface = Color(0xFF160B30),
    surfaceVariant = Color(0xFFDFD7FB),
    onSurfaceVariant = Color(0xFF381E72)
)

// Theme 3: Forest Mint
private val ForestMintDark = darkColorScheme(
    primary = MintPrimary,
    secondary = MintSecondary,
    tertiary = MintSecondary,
    background = MintBackground,
    surface = MintSurface,
    onPrimary = MintOnPrimary,
    onSecondary = MintOnPrimary,
    onBackground = MintOnBackground,
    onSurface = MintOnBackground,
    surfaceVariant = Color(0xFF0E241C),
    onSurfaceVariant = MintOnBackground
)
private val ForestMintLight = lightColorScheme(
    primary = Color(0xFF00897B),
    secondary = Color(0xFF26A69A),
    tertiary = Color(0xFF00695C),
    background = Color(0xFFF2FBF7),
    surface = Color(0xFFE0F2F1),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF042419),
    onSurface = Color(0xFF042419),
    surfaceVariant = Color(0xFFC8E6C9),
    onSurfaceVariant = Color(0xFF004D40)
)

// Theme 4: Oceanic Coral
private val OceanicCoralDark = darkColorScheme(
    primary = CoralPrimary,
    secondary = CoralSecondary,
    tertiary = CoralSecondary,
    background = CoralBackground,
    surface = CoralSurface,
    onPrimary = CoralOnPrimary,
    onSecondary = CoralOnPrimary,
    onBackground = CoralOnBackground,
    onSurface = CoralOnBackground,
    surfaceVariant = Color(0xFF0D2230),
    onSurfaceVariant = CoralOnBackground
)
private val OceanicCoralLight = lightColorScheme(
    primary = Color(0xFFD84315),
    secondary = Color(0xFF0288D1),
    tertiary = Color(0xFFBF360C),
    background = Color(0xFFF2F8FC),
    surface = Color(0xFFE1F0FA),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF0B212D),
    onSurface = Color(0xFF0B212D),
    surfaceVariant = Color(0xFFB3E5FC),
    onSurfaceVariant = Color(0xFF01579B)
)

// Theme 5: Cyberpunk Neon
private val CyberpunkNeonDark = darkColorScheme(
    primary = CyberPrimary,
    secondary = CyberSecondary,
    tertiary = CyberTertiary,
    background = CyberBackground,
    surface = CyberSurface,
    onPrimary = CyberOnPrimary,
    onSecondary = CyberOnPrimary,
    onBackground = CyberOnBackground,
    onSurface = CyberOnBackground,
    surfaceVariant = Color(0xFF19062D),
    onSurfaceVariant = CyberOnBackground
)
private val CyberpunkNeonLight = lightColorScheme(
    primary = Color(0xFFD81B60),
    secondary = Color(0xFF00ACC1),
    tertiary = Color(0xFFF50057),
    background = Color(0xFFFDF0F8),
    surface = Color(0xFFF8E1F4),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF21002B),
    onSurface = Color(0xFF21002B),
    surfaceVariant = Color(0xFFF48FB1),
    onSurfaceVariant = Color(0xFF880E4F)
)

// Fallbacks
private val DarkFallbackColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = Color(0xFF1C1B1F),
    surface = Color(0xFF1C1B1F)
)
private val LightFallbackColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE)
)

// Template 1: Glassmorphic Aurora
private val GlassmorphicAuroraDark = darkColorScheme(
    primary = AuroraPrimary,
    secondary = AuroraSecondary,
    tertiary = AuroraSecondary,
    background = AuroraBackground,
    surface = AuroraSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = AuroraOnBackground,
    onSurface = AuroraOnBackground,
    surfaceVariant = Color(0x1F1E294B),
    onSurfaceVariant = AuroraOnBackground
)
private val GlassmorphicAuroraLight = lightColorScheme(
    primary = Color(0xFF0284C7),
    secondary = Color(0xFF38BDF8),
    tertiary = Color(0xFF0369A1),
    background = Color(0xFFF0F8FF),
    surface = Color(0xFFE0F2FE),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color(0xFF0C4A6E),
    onSurface = Color(0xFF0C4A6E),
    surfaceVariant = Color(0xFFBAE6FD),
    onSurfaceVariant = Color(0xFF0369A1)
)

// Template 2: Retro Tape Deck
private val RetroTapeDeckDark = darkColorScheme(
    primary = TapePrimary,
    secondary = TapeSecondary,
    tertiary = TapeSecondary,
    background = TapeBackground,
    surface = TapeSurface,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = TapeOnBackground,
    onSurface = TapeOnBackground,
    surfaceVariant = Color(0xFF1E1E24),
    onSurfaceVariant = TapeOnBackground
)
private val RetroTapeDeckLight = lightColorScheme(
    primary = Color(0xFFD97706),
    secondary = Color(0xFFB45309),
    tertiary = Color(0xFF78350F),
    background = Color(0xFFFAF7F2),
    surface = Color(0xFFF3EDE2),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF291E13),
    onSurface = Color(0xFF291E13),
    surfaceVariant = Color(0xFFE7DBCE),
    onSurfaceVariant = Color(0xFF451A03)
)

// Template 3: Cyber Grid Neon
private val CyberGridNeonDark = darkColorScheme(
    primary = GridPrimary,
    secondary = GridSecondary,
    tertiary = GridSecondary,
    background = GridBackground,
    surface = GridSurface,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = GridOnBackground,
    onSurface = GridOnBackground,
    surfaceVariant = Color(0xFF0C0C12),
    onSurfaceVariant = GridOnBackground
)
private val CyberGridNeonLight = lightColorScheme(
    primary = Color(0xFF0284C7),
    secondary = Color(0xFF0D9488),
    tertiary = Color(0xFF0F766E),
    background = Color(0xFFF8FAFC),
    surface = Color(0xFFEDF2F7),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF1E293B)
)

// Template 4: Zen Nature Ripple
private val ZenNatureRippleDark = darkColorScheme(
    primary = ZenPrimary,
    secondary = ZenSecondary,
    tertiary = ZenSecondary,
    background = ZenBackground,
    surface = ZenSurface,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = ZenOnBackground,
    onSurface = ZenOnBackground,
    surfaceVariant = Color(0xFF041008),
    onSurfaceVariant = ZenOnBackground
)
private val ZenNatureRippleLight = lightColorScheme(
    primary = Color(0xFF15803D),
    secondary = Color(0xFF047857),
    tertiary = Color(0xFF166534),
    background = Color(0xFFF4FBF7),
    surface = Color(0xFFE6F4ED),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF052E16),
    onSurface = Color(0xFF052E16),
    surfaceVariant = Color(0xFFDCFCE7),
    onSurfaceVariant = Color(0xFF14532D)
)

// Template 5: Cosmic Starfield
private val CosmicStarfieldDark = darkColorScheme(
    primary = StarPrimary,
    secondary = StarSecondary,
    tertiary = StarSecondary,
    background = StarBackground,
    surface = StarSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = StarOnBackground,
    onSurface = StarOnBackground,
    surfaceVariant = Color(0xFF05020D),
    onSurfaceVariant = StarOnBackground
)
private val CosmicStarfieldLight = lightColorScheme(
    primary = Color(0xFF7C3AED),
    secondary = Color(0xFFC026D3),
    tertiary = Color(0xFF6D28D9),
    background = Color(0xFFFAF5FF),
    surface = Color(0xFFF3E8FF),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF1E0E3D),
    onSurface = Color(0xFF1E0E3D),
    surfaceVariant = Color(0xFFE9D5FF),
    onSurfaceVariant = Color(0xFF581C87)
)

// Template 6 (NEW): Vintage Gramophone (صفحه گرامافون قدیمی)
private val VintageGramophoneDark = darkColorScheme(
    primary = Color(0xFFD4AF37), // Antique Gold
    secondary = Color(0xFFE6C280), // Warm Brass
    tertiary = Color(0xFFC5A059),
    background = Color(0xFF160E08), // Dark Mahogany Wood
    surface = Color(0xFF261910),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color(0xFFFDFBF7), // Parchment
    onSurface = Color(0xFFFDFBF7),
    surfaceVariant = Color(0xFF3B281B),
    onSurfaceVariant = Color(0xFFE6C280)
)
private val VintageGramophoneLight = lightColorScheme(
    primary = Color(0xFF6B4226), // Dark Wood Mahogany
    secondary = Color(0xFFB8860B), // Antique Golden Brass
    tertiary = Color(0xFF8B5A2B),
    background = Color(0xFFFDFBF7), // Warm Parchment Paper
    surface = Color(0xFFF7F0E6),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF2A1A0E),
    onSurface = Color(0xFF2A1A0E),
    surfaceVariant = Color(0xFFEDE2D3),
    onSurfaceVariant = Color(0xFF5C3A21)
)

@Composable
fun MyApplicationTheme(
    themeIndex: Int = 0, // 0..6 (0=Legacy Fusion, 1=Aurora, 2=Tape Deck, 3=Cyber Grid, 4=Zen Ripple, 5=Starfield, 6=Vintage Gramophone)
    subThemeIndex: Int = 5,
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when (themeIndex) {
        0 -> {
            when (subThemeIndex) {
                0 -> if (darkTheme) GoldenPhoenixDark else GoldenPhoenixLight
                1 -> if (darkTheme) CosmicNebulaDark else CosmicNebulaLight
                2 -> if (darkTheme) ForestMintDark else ForestMintLight
                3 -> if (darkTheme) OceanicCoralDark else OceanicCoralLight
                4 -> if (darkTheme) CyberpunkNeonDark else CyberpunkNeonLight
                else -> {
                    if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val context = LocalContext.current
                        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
                    } else {
                        if (darkTheme) GoldenPhoenixDark else GoldenPhoenixLight
                    }
                }
            }
        }
        1 -> if (darkTheme) GlassmorphicAuroraDark else GlassmorphicAuroraLight
        2 -> if (darkTheme) RetroTapeDeckDark else RetroTapeDeckLight
        3 -> if (darkTheme) CyberGridNeonDark else CyberGridNeonLight
        4 -> if (darkTheme) ZenNatureRippleDark else ZenNatureRippleLight
        5 -> if (darkTheme) CosmicStarfieldDark else CosmicStarfieldLight
        6 -> if (darkTheme) VintageGramophoneDark else VintageGramophoneLight
        else -> {
            if (darkTheme) DarkFallbackColorScheme else LightFallbackColorScheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
