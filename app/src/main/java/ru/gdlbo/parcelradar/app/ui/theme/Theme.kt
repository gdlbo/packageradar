package ru.gdlbo.parcelradar.app.ui.theme

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import ru.gdlbo.parcelradar.app.di.theme.ThemeManager
import ru.gdlbo.parcelradar.app.ui.theme.ThemeColors.darkColor
import ru.gdlbo.parcelradar.app.ui.theme.ThemeColors.lightColor

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

    WindowCompat.setDecorFitsSystemWindows(window, false)

    val insetsController = WindowCompat.getInsetsController(window, view)
    insetsController.apply {
        isAppearanceLightStatusBars = !isDark
        isAppearanceLightNavigationBars = !isDark
    }

    ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
        val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        v.updatePadding(
            top = systemBars.top,
            bottom = systemBars.bottom
        )
        WindowInsetsCompat.CONSUMED
    }
}