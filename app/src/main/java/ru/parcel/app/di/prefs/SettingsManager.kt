package ru.parcel.app.di.prefs

import android.content.SharedPreferences
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SettingsManager : KoinComponent {
    private val prefs: SharedPreferences by inject()

    private val BOOL_GESTURE_SWIPE = "gesture_swipe"
    private val BOOL_PUSH_NOTIFICATIONS = "push_notifications"
    private val BOOL_SKIP_OPTIMIZATION = "skip_optimization"
    private val BOOL_NOTIFICAIONS_DISABLE = "notification_disable"

    var isGestureSwipeEnabled: Boolean
        get() = prefs.getBoolean(BOOL_GESTURE_SWIPE, true)
        set(value) = prefs.edit().putBoolean(BOOL_GESTURE_SWIPE, value).apply()

    var arePushNotificationsEnabled: Boolean
        get() = prefs.getBoolean(BOOL_PUSH_NOTIFICATIONS, true)
        set(value) = prefs.edit().putBoolean(BOOL_PUSH_NOTIFICATIONS, value).apply()

    var areOptimizationDialogSkipped: Boolean
        get() = prefs.getBoolean(BOOL_SKIP_OPTIMIZATION, false)
        set(value) = prefs.edit().putBoolean(BOOL_SKIP_OPTIMIZATION, value).apply()

    var areNotificationDialogSkipped: Boolean
        get() = prefs.getBoolean(BOOL_NOTIFICAIONS_DISABLE, false)
        set(value) = prefs.edit().putBoolean(BOOL_NOTIFICAIONS_DISABLE, value).apply()
}