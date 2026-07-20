package com.example.yiyuezhiming.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color

private val LightScheme = lightColorScheme(
    primary = AccentHotPink,
    onPrimary = Color.White,
    primaryContainer = PrimaryPink,
    onPrimaryContainer = DeepRose,
    secondary = SecondaryPink,
    onSecondary = TextBrown,
    secondaryContainer = CreamPink,
    onSecondaryContainer = TextBrown,
    tertiary = LavenderMist,
    onTertiary = TextBrown,
    background = BackgroundPink,
    onBackground = TextBrown,
    surface = SoftBlush,
    onSurface = TextBrown,
    surfaceVariant = CloudWhite,
    onSurfaceVariant = TextSecondary,
    outline = PaleRose,
    outlineVariant = PaleRose.copy(alpha = 0.5f)
)

private val DarkScheme = darkColorScheme(
    primary = DarkAccent,
    onPrimary = DarkBackground,
    primaryContainer = DarkWine,
    onPrimaryContainer = DarkText,
    secondary = DarkAccent.copy(alpha = 0.7f),
    onSecondary = DarkText,
    secondaryContainer = DarkSurfaceVariant,
    onSecondaryContainer = DarkText,
    tertiary = DarkWine,
    onTertiary = DarkText,
    background = DarkBackground,
    onBackground = DarkText,
    surface = DarkSurface,
    onSurface = DarkText,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkWine,
    outlineVariant = DarkWine.copy(alpha = 0.5f)
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
