package com.red.sovereign.ui.theme

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val AqyalGold = Color(0xFFF59E0B)
val AqyalGoldLight = Color(0xFFFBBF24)
val AqyalDarkObsidian = Color(0xFF030712)
val AqyalRoyalBlue = Color(0xFF0F172A)
val AqyalSurfaceNavy = Color(0xFF1E293B)
val AqyalCyanGlow = Color(0xFF38BDF8)

private val colors = darkColorScheme(
    primary = AqyalGold,
    secondary = AqyalCyanGlow,
    background = AqyalDarkObsidian,
    surface = AqyalSurfaceNavy,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White
)

@Composable
fun RedTheme(content: @Composable () -> Unit) = MaterialTheme(colorScheme = colors, typography = Typography(), content = content)

@Composable
fun SovereignBackground(content: @Composable () -> Unit) {
    val transition = rememberInfiniteTransition(label = "red-background")
    val glow by transition.animateFloat(
        initialValue = 0.18f, targetValue = 0.4f,
        animationSpec = infiniteRepeatable(tween(4000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "gold-glow"
    )
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(AqyalDarkObsidian, AqyalRoyalBlue, Color(0xFF090D16))))) {
        Box(Modifier.fillMaxSize().background(Brush.radialGradient(listOf(AqyalGold.copy(alpha = glow), Color.Transparent), radius = 1300f)))
        content()
    }
}
