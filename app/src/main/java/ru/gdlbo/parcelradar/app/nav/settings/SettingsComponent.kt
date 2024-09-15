package ru.gdlbo.parcelradar.app.nav.settings

import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import com.arkivanov.decompose.ComponentContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.gdlbo.parcelradar.app.core.network.ApiHandler
import ru.gdlbo.parcelradar.app.core.network.retryRequest
import ru.gdlbo.parcelradar.app.di.prefs.AccessTokenManager
import ru.gdlbo.parcelradar.app.di.prefs.SettingsManager
import ru.gdlbo.parcelradar.app.di.room.RoomManager
import ru.gdlbo.parcelradar.app.di.sync.DataSyncManager
import ru.gdlbo.parcelradar.app.di.theme.ThemeManager
import ru.gdlbo.parcelradar.app.nav.RootComponent

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
    val settingsManager = SettingsManager()

    fun isServiceEnabled(context: Context, serviceClassName: String): Boolean {
        val componentName = ComponentName(context, serviceClassName)
        val pm = context.packageManager
        return try {
            val serviceInfo = pm.getServiceInfo(componentName, PackageManager.GET_META_DATA)
            serviceInfo.enabled
        } catch (e: PackageManager.NameNotFoundException) {
            e.fillInStackTrace()
            false
        }
    }

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
}