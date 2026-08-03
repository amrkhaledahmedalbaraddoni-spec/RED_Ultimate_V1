package com.red.core.theme

import androidx.compose.material3.*
import androidx.compose.ui.graphics.Color

val RedPrimary = Color(0xFFD32F2F)
val RedDark = Color(0xFFB71C1C)
val BlackBackground = Color(0xFF0A0A0A)
val SurfaceDark = Color(0xFF1E1E1E)

private val DarkColorScheme = darkColorScheme(
    primary = RedPrimary,
    secondary = RedDark,
    background = BlackBackground,
    surface = SurfaceDark,
    onPrimary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

@Composable
fun REDTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography(), // 2026 Modern Sans
        content = content
    )
}
