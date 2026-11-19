package com.flashkit.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// FlashKit brand colors
val FlashKitBlue = Color(0xFFAFC6FF)
val FlashKitBlueDark = Color(0xFF2D4275)
val FlashKitSurface = Color(0xFF1B1F27)
val FlashKitSurfaceDim = Color(0xFF101318)
val FlashKitGreen = Color(0xFF8EE2A0)
val FlashKitYellow = Color(0xFFFFCB6B)
val FlashKitRed = Color(0xFFFF8A80)

private val DarkColorScheme = darkColorScheme(
    primary = FlashKitBlue,
    onPrimary = Color(0xFF002D6E),
    primaryContainer = FlashKitBlueDark,
    onPrimaryContainer = Color(0xFFDDE6FF),
    secondary = Color(0xFFBEC6DC),
    onSecondary = Color(0xFF283041),
    secondaryContainer = Color(0xFF3E4759),
    onSecondaryContainer = Color(0xFFDAE2F9),
    tertiary = FlashKitGreen,
    onTertiary = Color(0xFF003919),
    tertiaryContainer = Color(0xFF005227),
    onTertiaryContainer = Color(0xFFA8F5B9),
    error = FlashKitRed,
    onError = Color(0xFF601410),
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFF9DEDC),
    background = FlashKitSurfaceDim,
    onBackground = Color(0xFFE3E2E6),
    surface = FlashKitSurface,
    onSurface = Color(0xFFE3E2E6),
    surfaceVariant = Color(0xFF2A303C),
    onSurfaceVariant = Color(0xFF9AA3B2),
    outline = Color(0xFF3A4560),
    outlineVariant = Color(0xFF44474F),
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF3B5BA9),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDAE2FF),
    onPrimaryContainer = Color(0xFF001849),
    secondary = Color(0xFF575E71),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDBE2F9),
    onSecondaryContainer = Color(0xFF141B2C),
    tertiary = Color(0xFF006D35),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF97F7AE),
    onTertiaryContainer = Color(0xFF00210C),
    error = Color(0xFFBA1A1A),
    background = Color(0xFFFEFBFF),
    surface = Color(0xFFFEFBFF),
    onSurface = Color(0xFF1B1B1F),
    surfaceVariant = Color(0xFFE1E2EC),
    onSurfaceVariant = Color(0xFF44474F),
    outline = Color(0xFF757780),
)

// Material 3 Expressive shapes
val FlashKitShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

@Composable
fun FlashKitTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        shapes = FlashKitShapes,
        typography = Typography(
            headlineLarge = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                letterSpacing = 0.sp,
            ),
            titleLarge = TextStyle(
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
            ),
            titleMedium = TextStyle(
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
            ),
            bodyLarge = TextStyle(
                fontSize = 16.sp,
                lineHeight = 24.sp,
            ),
            bodyMedium = TextStyle(
                fontSize = 14.sp,
                lineHeight = 20.sp,
            ),
            labelLarge = TextStyle(
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
            ),
        ),
        content = content
    )
}
