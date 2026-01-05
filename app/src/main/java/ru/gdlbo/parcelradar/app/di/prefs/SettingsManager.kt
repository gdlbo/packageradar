package ru.gdlbo.parcelradar.app.di.prefs

import android.content.SharedPreferences
import androidx.core.content.edit
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SettingsManager : KoinComponent {
    private val prefs: SharedPreferences by inject()

    private val BOOL_GESTURE_SWIPE = "gesture_swipe"
    private val BOOL_PUSH_NOTIFICATIONS = "push_notifications"
    private val BOOL_SKIP_OPTIMIZATION = "skip_optimization"
    private val BOOL_NOTIFICAIONS_DISABLE = "notification_disable"
    private val BOOL_LINKS_DIALOG_SKIPPED = "links_dialog_skipped"
    private val BOOL_USE_LOCAL_TIME = "use_local_time"

    var isGestureSwipeEnabled: Boolean
        get() = prefs.getBoolean(BOOL_GESTURE_SWIPE, true)
        set(value) = prefs.edit { putBoolean(BOOL_GESTURE_SWIPE, value) }

    var arePushNotificationsEnabled: Boolean
        get() = prefs.getBoolean(BOOL_PUSH_NOTIFICATIONS, true)
        set(value) = prefs.edit { putBoolean(BOOL_PUSH_NOTIFICATIONS, value) }

    var areOptimizationDialogSkipped: Boolean
        get() = prefs.getBoolean(BOOL_SKIP_OPTIMIZATION, false)
        set(value) = prefs.edit { putBoolean(BOOL_SKIP_OPTIMIZATION, value) }

    var areNotificationDialogSkipped: Boolean
        get() = prefs.getBoolean(BOOL_NOTIFICAIONS_DISABLE, false)
        set(value) = prefs.edit { putBoolean(BOOL_NOTIFICAIONS_DISABLE, value) }

    var areLinksDialogSkipped: Boolean
        get() = prefs.getBoolean(BOOL_LINKS_DIALOG_SKIPPED, false)
        set(value) = prefs.edit { putBoolean(BOOL_LINKS_DIALOG_SKIPPED, value) }

    var isUseLocalTime: Boolean
        get() = prefs.getBoolean(BOOL_USE_LOCAL_TIME, true)
        set(value) = prefs.edit { putBoolean(BOOL_USE_LOCAL_TIME, value) }
}