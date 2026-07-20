package com.example.yiyuezhiming.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue

private val LightScheme = lightColorScheme(
    primary = AccentHotPink,
    secondary = PrimaryPink,
    tertiary = LavenderMist,
    background = BackgroundPink,
    surface = SoftBlush,
    surfaceVariant = CloudWhite,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    onSecondary = TextBrown,
    onBackground = TextBrown,
    onSurface = TextBrown,
    onSurfaceVariant = TextBrown
)

private val DarkScheme = darkColorScheme(
    primary = PrimaryPink,
    secondary = DarkAccent,
    tertiary = DarkWine,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkWine,
    onPrimary = DarkBackground,
    onSecondary = DarkText,
    onBackground = DarkText,
    onSurface = DarkText,
    onSurfaceVariant = DarkText
)

@Composable
fun YiYueTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    val scheme = if (darkTheme) DarkScheme else LightScheme
    val background by animateColorAsState(scheme.background, label = "theme-background")
    MaterialTheme(
        colorScheme = scheme.copy(background = background),
        typography = KawaiiTypography,
        shapes = KawaiiShapes,
        content = content
    )
}
