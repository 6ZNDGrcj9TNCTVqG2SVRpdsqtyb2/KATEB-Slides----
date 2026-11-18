package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val KatebDarkColorScheme = darkColorScheme(
    primary = Color(0xFF60A5FA),
    onPrimary = Color(0xFF002454),
    primaryContainer = Color(0xFF17376E),
    onPrimaryContainer = Color(0xFFDCE8FF),
    secondary = Color(0xFF2DD4BF),
    onSecondary = Color(0xFF003831),
    secondaryContainer = Color(0xFF0D4A42),
    onSecondaryContainer = Color(0xFFCFF4EC),
    tertiary = KatebGoldAccent,
    onTertiary = Color(0xFF2D1B00),
    tertiaryContainer = Color(0xFF533600),
    onTertiaryContainer = KatebGoldContainer,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkOutline,
    error = Color(0xFFF87171),
    onError = Color(0xFF450A0A)
)

private val KatebLightColorScheme = lightColorScheme(
    primary = KatebBluePrimary,
    onPrimary = Color.White,
    primaryContainer = KatebBlueContainer,
    onPrimaryContainer = KatebOnBlueContainer,
    secondary = KatebTealSecondary,
    onSecondary = Color.White,
    secondaryContainer = KatebTealContainer,
    onSecondaryContainer = KatebOnTealContainer,
    tertiary = KatebGoldAccent,
    onTertiary = Color.White,
    tertiaryContainer = KatebGoldContainer,
    onTertiaryContainer = KatebOnGoldContainer,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    outline = LightOutline,
    error = ErrorRed,
    onError = Color.White,
    errorContainer = ErrorContainerLight
)

val KatebShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

enum class ThemePreference { SYSTEM, LIGHT, DARK }

@Composable
fun KatebSlidesTheme(
    themePreference: ThemePreference = ThemePreference.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themePreference) {
        ThemePreference.SYSTEM -> isSystemInDarkTheme()
        ThemePreference.LIGHT -> false
        ThemePreference.DARK -> true
    }

    MaterialTheme(
        colorScheme = if (darkTheme) KatebDarkColorScheme else KatebLightColorScheme,
        typography = KatebTypography,
        shapes = KatebShapes,
        content = content
    )
}
