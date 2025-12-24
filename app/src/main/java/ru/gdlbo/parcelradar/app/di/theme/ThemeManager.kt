package ru.gdlbo.parcelradar.app.di.theme

import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow

class ThemeManager(private val sharedPref: SharedPreferences) {
    val isDynamicColor = MutableStateFlow(getThemeValue("dynamic_color"))
    val isDarkTheme = MutableStateFlow(getThemeValue("dark_theme"))
    val selectedColorScheme = MutableStateFlow(getStringThemeValue("color_scheme", "Default"))

    private fun getThemeValue(key: String): Boolean? {
        return if (sharedPref.contains(key)) {
            sharedPref.getBoolean(key, true)
        } else {
            null
        }
    }

    private fun getStringThemeValue(key: String, default: String): String {
        return sharedPref.getString(key, default) ?: default
    }

    fun setDynamicColorValue(value: Boolean?) {
        setThemeValue("dynamic_color", value)
    }

    fun setDarkThemeValue(value: Boolean?) {
        setThemeValue("dark_theme", value)
    }

    fun setColorScheme(value: String) {
        sharedPref.edit().putString("color_scheme", value).apply()
        selectedColorScheme.value = value
    }

    private fun setThemeValue(key: String, value: Boolean?) {
        if (value == null) {
            sharedPref.edit().remove(key).apply()
        } else {
            sharedPref.edit().putBoolean(key, value).apply()
        }
        when (key) {
            "dynamic_color" -> isDynamicColor.value = value
            "dark_theme" -> isDarkTheme.value = value
        }
    }
}
