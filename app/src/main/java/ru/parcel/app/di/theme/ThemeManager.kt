package ru.parcel.app.di.theme

import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.flow.MutableStateFlow

class ThemeManager(private val sharedPref: SharedPreferences) {
    val isDynamicColor = MutableStateFlow(getThemeValue("dynamic_color"))
    val isDarkTheme = MutableStateFlow(getThemeValue("dark_theme"))

    private fun getThemeValue(key: String): Boolean? {
        return if (sharedPref.contains(key)) {
            sharedPref.getBoolean(key, true)
        } else {
            null
        }
    }

    fun setDynamicColorValue(value: Boolean?) {
        setThemeValue("dynamic_color", value)
    }

    fun setDarkThemeValue(value: Boolean?) {
        setThemeValue("dark_theme", value)
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