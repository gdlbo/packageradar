package ru.parcel.app.ui.theme

import android.app.Activity
import android.os.Build
import android.view.View
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import ru.parcel.app.di.theme.ThemeManager
import ru.parcel.app.ui.theme.ThemeColors.darkColor
import ru.parcel.app.ui.theme.ThemeColors.lightColor

@Composable
fun TrackerTheme(
    themeManager: ThemeManager,
    content: @Composable () -> Unit
) {
    val managerDynamic by themeManager.isDynamicColor.collectAsState()
    val managerDark by themeManager.isDarkTheme.collectAsState()

    // Determine if dark theme should be used
    val isDark = managerDark == true || (managerDark == null && isSystemInDarkTheme())

    // Check if dynamic color scheme should be used (Android 12+)
    val isSystemDynamic = managerDynamic != false && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    // Check if system dark theme should be used (Android 10+)
    val isSystemDarkTheme = managerDark == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

    // Get the appropriate color scheme
    val colorScheme = getColorScheme(isDark, isSystemDynamic, isSystemDarkTheme)

    // Update window settings if not in edit mode
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            configureWindow(view, isDark)
        }
    }

    // Apply MaterialTheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
private fun getColorScheme(
    isDark: Boolean,
    isSystemDynamic: Boolean,
    isSystemDarkTheme: Boolean
): ColorScheme {
    val context = LocalContext.current
    return when {
        isSystemDynamic && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        isSystemDarkTheme && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
            if (isDark) darkColor else lightColor
        }

        else -> {
            if (isDark) darkColor else lightColor
        }
    }
}

private fun configureWindow(view: View, isDark: Boolean) {
    val window = (view.context as Activity).window
    window.statusBarColor = Color.Transparent.toArgb()
    window.navigationBarColor = Color.Transparent.toArgb()

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        window.isStatusBarContrastEnforced = false
        window.isNavigationBarContrastEnforced = false
    }

    WindowCompat.setDecorFitsSystemWindows(window, false)
    WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDark
}