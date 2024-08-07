package ru.parcel.app.di.prefs

import android.content.SharedPreferences
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SettingsManager : KoinComponent {
    private val prefs: SharedPreferences by inject()

    private val BOOL_GESTURE_SWIPE = "gesture_swipe"

    var isGestureSwipeEnabled: Boolean
        get() = prefs.getBoolean(BOOL_GESTURE_SWIPE, true)
        set(value) = prefs.edit().putBoolean(BOOL_GESTURE_SWIPE, value).apply()
}