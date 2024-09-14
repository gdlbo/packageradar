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
        primary = Color(0xFFA4C9FF),
        onPrimary = Color(0xFF003060),
        primaryContainer = Color(0xFF17487D),
        onPrimaryContainer = Color(0xFFD3E3FF),
        inversePrimary = Color(0xFF355F97),
        secondary = Color(0xFFBCC7DB),
        onSecondary = Color(0xFF263141),
        secondaryContainer = Color(0xFF3D4758),
        onSecondaryContainer = Color(0xFFD8E3F8),
        tertiary = Color(0xFFDABCE2),
        onTertiary = Color(0xFF3D2846),
        tertiaryContainer = Color(0xFF553F5D),
        onTertiaryContainer = Color(0xFFF7D9FF),
        error = Color(0xFFF2B8B5),
        onError = Color(0xFF601410),
        errorContainer = Color(0xFF8C1D18),
        onErrorContainer = Color(0xFFF9DEDC),
        background = Color(0xFF1B1B1B),
        onBackground = Color(0xFFE2E2E2),
        surface = Color(0xFF1B1B1B),
        onSurface = Color(0xFFE2E2E2),
        inverseSurface = Color(0xFFE2E2E2),
        inverseOnSurface = Color(0xFF303030),
        surfaceVariant = Color(0xFF474747),
        onSurfaceVariant = Color(0xFFC6C6C6),
        outline = Color(0xFF919191)
    )

    val lightColor = lightColorScheme(
        primary = Color(0xFF5393E4),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFB3D5F5),
        onPrimaryContainer = Color(0xFF002A60),
        inversePrimary = Color(0xFF3A6BA1),
        secondary = Color(0xFF7F91B5),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFC7D4E8),
        onSecondaryContainer = Color(0xFF25354F),
        tertiary = Color(0xFF9FA5C2),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFE1E3F0),
        onTertiaryContainer = Color(0xFF313347),
        error = Color(0xFFD32F2F),
        onError = Color(0xFFFFFFFF),
        errorContainer = Color(0xFFF9D9D9),
        onErrorContainer = Color(0xFF8A1D1D),
        background = Color(0xFFFAFBFD),
        onBackground = Color(0xFF1E1E1F),
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF1E1E1F),
        inverseSurface = Color(0xFF313336),
        inverseOnSurface = Color(0xFFEFEFF2),
        surfaceVariant = Color(0xFFE0E2E5),
        onSurfaceVariant = Color(0xFF4B4E50),
        outline = Color(0xFF8C9193)
    )
}

fun Color.darker(): Color {
    val factor = 0.5f
    return copy(red = red * factor, green = green * factor, blue = blue * factor)
}