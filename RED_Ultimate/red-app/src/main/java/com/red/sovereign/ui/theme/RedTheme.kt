package com.red.sovereign.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// YOUNES semantic palette. Components should consume MaterialTheme tokens rather than raw colors.
val YounesEmerald = Color(0xFF2DDBA4)
val YounesEmeraldGlow = Color(0xFF75F3CB)
val YounesGold = Color(0xFFE9BC62)
val YounesGoldLight = Color(0xFFFFDEA0)
val YounesInk = Color(0xFF050A0E)
val YounesMidnight = Color(0xFF09131B)
val YounesSurface = Color(0xFF101E28)
val YounesSurfaceHigh = Color(0xFF172A37)
val YounesCyan = Color(0xFF65D7E7)
val YounesMutedText = Color(0xFFA8BBC6)
val YounesDanger = Color(0xFFFF6B78)

// Migration aliases retained until feature files are split out of the legacy monolithic screen.
val AqyalGold = YounesGold
val AqyalGoldLight = YounesGoldLight
val AqyalDarkObsidian = YounesInk
val AqyalRoyalBlue = YounesMidnight
val AqyalSurfaceNavy = YounesSurface
val AqyalSurfaceRaised = YounesSurfaceHigh
val AqyalCyanGlow = YounesCyan
val RedCrimson = YounesDanger
val RedCrimsonGlow = YounesDanger
val RedMutedText = YounesMutedText

private val colors = darkColorScheme(
    primary = YounesEmerald,
    secondary = AqyalCyanGlow,
    tertiary = AqyalGold,
    background = AqyalDarkObsidian,
    surface = AqyalSurfaceNavy,
    surfaceVariant = AqyalSurfaceRaised,
    primaryContainer = Color(0xFF004D3A),
    secondaryContainer = Color(0xFF073D48),
    tertiaryContainer = Color(0xFF4C3600),
    onPrimary = Color.White,
    onSecondary = Color(0xFF001F24),
    onTertiary = Color(0xFF261A00),
    onBackground = Color(0xFFF2F7FA),
    onSurface = Color(0xFFF2F7FA),
    onSurfaceVariant = RedMutedText,
    outline = Color(0xFF45606F),
    outlineVariant = Color(0xFF263A46),
    scrim = Color(0xD9000000),
    error = YounesDanger
)

private val younesFont = FontFamily.SansSerif
private val typography = Typography(
    displaySmall = TextStyle(fontFamily = younesFont, fontSize = 36.sp, lineHeight = 44.sp, fontWeight = FontWeight.Bold),
    headlineLarge = TextStyle(fontFamily = younesFont, fontSize = 30.sp, lineHeight = 38.sp, fontWeight = FontWeight.Bold),
    headlineMedium = TextStyle(fontFamily = younesFont, fontSize = 25.sp, lineHeight = 32.sp, fontWeight = FontWeight.Bold),
    headlineSmall = TextStyle(fontFamily = younesFont, fontSize = 22.sp, lineHeight = 29.sp, fontWeight = FontWeight.SemiBold),
    titleLarge = TextStyle(fontFamily = younesFont, fontSize = 21.sp, lineHeight = 28.sp, fontWeight = FontWeight.Bold),
    titleMedium = TextStyle(fontFamily = younesFont, fontSize = 17.sp, lineHeight = 24.sp, fontWeight = FontWeight.SemiBold),
    titleSmall = TextStyle(fontFamily = younesFont, fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontFamily = younesFont, fontSize = 16.sp, lineHeight = 25.sp),
    bodyMedium = TextStyle(fontFamily = younesFont, fontSize = 14.sp, lineHeight = 22.sp),
    bodySmall = TextStyle(fontFamily = younesFont, fontSize = 12.sp, lineHeight = 18.sp),
    labelLarge = TextStyle(fontFamily = younesFont, fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold),
    labelMedium = TextStyle(fontFamily = younesFont, fontSize = 12.sp, lineHeight = 17.sp, fontWeight = FontWeight.Medium),
    labelSmall = TextStyle(fontFamily = younesFont, fontSize = 11.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium)
)

private val shapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(34.dp)
)

@Composable
fun YounesTheme(highContrast: Boolean = false, content: @Composable () -> Unit) = MaterialTheme(
    colorScheme = if (highContrast) colors.copy(
        onBackground = Color.White,
        onSurface = Color.White,
        onSurfaceVariant = Color(0xFFE6F2F6),
        outline = YounesEmeraldGlow
    ) else colors,
    typography = typography,
    shapes = shapes,
    content = content
)

/** Calm, content-first background. Brand color is reserved for hierarchy and actions. */
@Composable
fun SovereignBackground(content: @Composable () -> Unit) {
    Box(
        Modifier.fillMaxSize().background(
            Brush.verticalGradient(
                colors = listOf(YounesInk, YounesMidnight, Color(0xFF071017))
            )
        )
    ) { content() }
}
