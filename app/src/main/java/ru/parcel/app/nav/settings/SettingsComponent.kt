package ru.parcel.app.nav.settings

import android.app.ActivityManager
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
import ru.parcel.app.core.network.ApiHandler
import ru.parcel.app.core.network.retryRequest
import ru.parcel.app.di.prefs.AccessTokenManager
import ru.parcel.app.di.prefs.SettingsManager
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

    fun isServiceRunning(context: Context, serviceClassName: String): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val services = manager?.getRunningServices(Integer.MAX_VALUE)
        return services?.any { serviceClassName == it.service.className } == true
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