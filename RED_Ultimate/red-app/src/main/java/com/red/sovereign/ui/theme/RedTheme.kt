package com.red.sovereign.ui.theme

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.red.sovereign.R

val RedCrimson = Color(0xFFE0002A)
val RedCrimsonGlow = Color(0xFFFF3158)
val AqyalGold = Color(0xFFE8B84A)
val AqyalGoldLight = Color(0xFFFFD978)
val AqyalDarkObsidian = Color(0xFF030712)
val AqyalRoyalBlue = Color(0xFF071522)
val AqyalSurfaceNavy = Color(0xFF102233)
val AqyalSurfaceRaised = Color(0xFF183247)
val AqyalCyanGlow = Color(0xFF39D4E8)
val RedMutedText = Color(0xFFA9BBC9)

private val colors = darkColorScheme(
    primary = RedCrimson,
    secondary = AqyalCyanGlow,
    tertiary = AqyalGold,
    background = AqyalDarkObsidian,
    surface = AqyalSurfaceNavy,
    surfaceVariant = AqyalSurfaceRaised,
    primaryContainer = Color(0xFF590014),
    secondaryContainer = Color(0xFF073D48),
    tertiaryContainer = Color(0xFF4C3600),
    onPrimary = Color.White,
    onSecondary = Color(0xFF001F24),
    onTertiary = Color(0xFF261A00),
    onBackground = Color(0xFFF2F7FA),
    onSurface = Color(0xFFF2F7FA),
    onSurfaceVariant = RedMutedText,
    outline = Color(0xFF496275),
    error = Color(0xFFFF6B78)
)

private val typography = Typography(
    headlineLarge = TextStyle(fontSize = 32.sp, lineHeight = 38.sp, fontWeight = FontWeight.Black, letterSpacing = (-0.5).sp),
    headlineMedium = TextStyle(fontSize = 26.sp, lineHeight = 32.sp, fontWeight = FontWeight.Bold),
    titleLarge = TextStyle(fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.Bold),
    titleMedium = TextStyle(fontSize = 17.sp, lineHeight = 23.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 21.sp),
    labelLarge = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Bold)
)

private val shapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(34.dp)
)

@Composable
fun RedTheme(content: @Composable () -> Unit) = MaterialTheme(
    colorScheme = colors,
    typography = typography,
    shapes = shapes,
    content = content
)

/** Layered, low-cost brand background: static authored art plus two GPU-friendly animated light fields. */
@Composable
fun SovereignBackground(content: @Composable () -> Unit) {
    val transition = rememberInfiniteTransition(label = "red-sovereign-background")
    val pulse by transition.animateFloat(
        initialValue = 0.10f,
        targetValue = 0.24f,
        animationSpec = infiniteRepeatable(tween(5200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "crimson-pulse"
    )
    val drift by transition.animateFloat(
        initialValue = 180f,
        targetValue = 920f,
        animationSpec = infiniteRepeatable(tween(11000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "gold-drift"
    )

    Box(Modifier.fillMaxSize().background(AqyalDarkObsidian)) {
        Image(
            painter = painterResource(R.drawable.red_sovereign_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.48f
        )
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    listOf(Color(0xAA030712), Color(0x7A071522), Color(0xE6030712))
                )
            )
        )
        Box(
            Modifier.fillMaxSize().background(
                Brush.radialGradient(
                    colors = listOf(RedCrimsonGlow.copy(alpha = pulse), Color.Transparent),
                    center = Offset(1050f, drift),
                    radius = 880f
                )
            )
        )
        Box(
            Modifier.fillMaxSize().background(
                Brush.radialGradient(
                    colors = listOf(AqyalGoldLight.copy(alpha = 0.10f), Color.Transparent),
                    center = Offset(80f, 120f),
                    radius = 620f
                )
            )
        )
        content()
    }
}
