package ru.parcel.app.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

object ThemeColors {
    val LightGreen = Color(0xFF90EE90)
    val DarkBlue = Color(0xFF1976D2)
    val LightBlue = Color(0xFFBBDEFB)
    val DarkGreen = Color(0xFF388E3C)
    val LightGreenTransparent = Color(0xFF81C784)

    val darkColor = darkColorScheme(
        primary = Color(0xFFFF9800),
        onPrimary = Color(0xFF3E2723),
        primaryContainer = Color(0xFFFF8706),
        onPrimaryContainer = Color(0xFFFFE0B2),
        inversePrimary = Color(0xFFFFB74D),
        secondary = Color(0xFFD55E00),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFF9B4700),
        onSecondaryContainer = Color(0xFFFFD8B8),
        tertiary = Color(0xFFFF7043),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFFF8B4E),
        onTertiaryContainer = Color(0xFFFFAB91),
        error = Color(0xFFD32F2F),
        onError = Color(0xFFFFFFFF),
        errorContainer = Color(0xFFB71C1C),
        onErrorContainer = Color(0xFFFFCDD2),
        background = Color(0xFF121212),
        onBackground = Color(0xFFE0E0E0),
        surface = Color(0xFF121212),
        onSurface = Color(0xFFE0E0E0),
        inverseSurface = Color(0xFFE0E0E0),
        inverseOnSurface = Color(0xFF424242),
        surfaceVariant = Color(0xFF616161),
        onSurfaceVariant = Color(0xFFBDBDBD),
        outline = Color(0xFF9E9E9E)
    )

    val lightColor = lightColorScheme(
        primary = Color(0xFFE65100),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFFF6F1C),
        onPrimaryContainer = Color(0xFFFFFDFC),
        inversePrimary = Color(0xFFFFEBE4),
        secondary = Color(0xFFD84315),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFFF7C40),
        onSecondaryContainer = Color(0xFFFFFAF9),
        tertiary = Color(0xFFEF6C00),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFFFB74D),
        onTertiaryContainer = Color(0xFFFFCCBC),
        error = Color(0xFFD32F2F),
        onError = Color(0xFFFFFFFF),
        errorContainer = Color(0xFFEF9A9A),
        onErrorContainer = Color(0xFFB71C1C),
        background = Color(0xFFFFFBFA),
        onBackground = Color(0xFF1E1B1B),
        surface = Color(0xFFFFFBFA),
        onSurface = Color(0xFF1E1B1B),
        inverseSurface = Color(0xFF332F2F),
        inverseOnSurface = Color(0xFFF7EFEE),
        surfaceVariant = Color(0xFFEFEBE9),
        onSurfaceVariant = Color(0xFF4A4645),
        outline = Color(0xFF8D6E63)
    )
}

fun Color.darker(): Color {
    val factor = 0.5f
    return copy(red = red * factor, green = green * factor, blue = blue * factor)
}