package com.red.core.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val SovereignGold = Color(0xFFF4B400)
val SovereignBlue = Color(0xFF1E88E5)
val ObsidianBlack = Color(0xFF07090E)
val DeepRoyalBlue = Color(0xFF0F172A)
val SurfaceObsidian = Color(0xFF131B2E)

private val SovereignDarkColorScheme = darkColorScheme(
    primary = SovereignBlue,
    secondary = SovereignGold,
    background = ObsidianBlack,
    surface = SurfaceObsidian,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White
)

@Composable
fun REDTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SovereignDarkColorScheme,
        typography = Typography(),
        content = content
    )
}

/**
 * خلفية أسطورية فاخرة للمنظومة السيادية تعتمد على تدرجات حية (Obsidian & Deep Royal)
 */
@Composable
fun SovereignBackground(content: @Composable () -> Unit) {
    val epicGradient = Brush.verticalGradient(
        colors = listOf(
            ObsidianBlack,
            DeepRoyalBlue,
            Color(0xFF1A233A)
        )
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(epicGradient)
    ) {
        content()
    }
}
