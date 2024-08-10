package ru.parcel.app.nav.settings

import android.content.Context
import android.content.SharedPreferences
import com.arkivanov.decompose.ComponentContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.parcel.app.core.network.ApiHandler
import ru.parcel.app.core.network.retryRequest
import ru.parcel.app.di.prefs.AccessTokenManager
import ru.parcel.app.di.room.RoomManager
import ru.parcel.app.di.sync.DataSyncManager
import ru.parcel.app.di.theme.ThemeManager
import ru.parcel.app.nav.RootComponent

class SettingsComponent(
    val navigateTo: (topLevelConfiguration: RootComponent.TopLevelConfiguration) -> Unit,
    val navToLogin: () -> Unit,
    val popBack: () -> Unit,
    componentContext: ComponentContext
) : ComponentContext by componentContext, KoinComponent {
    val viewModelScope = CoroutineScope(Dispatchers.Main.immediate)
    val roomManager: RoomManager by inject()
    val themeManager: ThemeManager by inject()
    val prefs: SharedPreferences by inject()
    val atm: AccessTokenManager by inject()
    val apiHandler: ApiHandler by inject()

    fun dropUserData() {
        viewModelScope.launch {
            atm.clearAccountToken()
            roomManager.dropDb()
        }
    }

    fun notificaionCheck(context: Context) {
        viewModelScope.launch {
            DataSyncManager().syncData(context)
        }
    }

    fun logout() {
        dropUserData()
        navToLogin()
    }

    fun updateNotification(email: Boolean, inapp: Boolean) {
        viewModelScope.launch {
            try {
                retryRequest {
                    apiHandler.setNotificationsSettings(inapp, email)
                }
                roomManager.updateNotifySettings(email, inapp)
            } catch (e: Exception) {
                e.fillInStackTrace()
            }
        }
    }

    fun approveEmail() {
        viewModelScope.launch {
            try {
                retryRequest {
                    apiHandler.resendConfirmation()
                }
            } catch (e: Exception) {
                e.fillInStackTrace()
            }
        }
    }

    private val BOOL_GESTURE_SWIPE = "gesture_swipe"

    var isGestureSwipeEnabled: Boolean
        get() = prefs.getBoolean(BOOL_GESTURE_SWIPE, true)
        set(value) = prefs.edit().putBoolean(BOOL_GESTURE_SWIPE, value).apply()
}