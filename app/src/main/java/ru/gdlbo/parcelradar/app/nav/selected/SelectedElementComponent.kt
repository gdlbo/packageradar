package ru.gdlbo.parcelradar.app.nav.selected

import android.util.Log
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.gdlbo.parcelradar.app.core.network.ApiHandler
import ru.gdlbo.parcelradar.app.core.network.model.Tracking
import ru.gdlbo.parcelradar.app.core.network.retryRequest
import ru.gdlbo.parcelradar.app.di.prefs.AccessTokenManager
import ru.gdlbo.parcelradar.app.di.prefs.SettingsManager
import ru.gdlbo.parcelradar.app.di.room.RoomManager
import ru.gdlbo.parcelradar.app.di.theme.ThemeManager
import java.util.*

class SelectedElementComponent(
    componentContext: ComponentContext,
    val id: Long,
    val popBack: () -> Unit,
) : ComponentContext by componentContext, KoinComponent {
    private val viewModelScope = CoroutineScope(Dispatchers.Main.immediate)
    val roomManager: RoomManager by inject()
    val themeManager: ThemeManager by inject()
    val settingsManager: SettingsManager by inject()
    private val apiService: ApiHandler by inject()
    private val atm: AccessTokenManager by inject()

    val currentTracking = MutableStateFlow<Tracking?>(null)

    init {
        lifecycle.doOnDestroy {
            viewModelScope.cancel()
        }
    }

    suspend fun deleteItem(tracking: Tracking?) {
        if (tracking == null) return
        try {
            retryRequest {
                apiService.updateTrackingById(
                    id = tracking.id,
                    name = tracking.title.toString(),
                    isArchive = tracking.isArchived ?: false,
                    isDeleted = true,
                    isNotify = false,
                    date = null
                )

                withContext(Dispatchers.IO) {
                    roomManager.removeTrackingById(tracking)
                }
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "Error deleting item", e)
        }
    }

    fun getOpenSiteLink(tracking: Tracking): String {
        val currentLanguage = Locale.getDefault().language
        val baseUrl =
            if (currentLanguage == "ru") "https://gdeposylka.ru" else "https://packageradar.com"
        val addr =
            "$baseUrl/courier/${tracking.courier?.slug.orEmpty()}/tracking/${tracking.trackingNumber}"

        return "$baseUrl/api/a1/go?token=${atm.getAccessToken()}&addr=${addr.replace(" ", "%20")}"
    }

    fun forceUpdateDB() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                roomManager.dropParcels()
            }
        }
    }

    fun archiveParcel(item: Tracking, needToBeArchived: Boolean) {
        viewModelScope.launch {
            try {
                retryRequest {
                    apiService.updateTrackingById(
                        id = item.id,
                        name = item.title ?: "",
                        isArchive = needToBeArchived,
                        isDeleted = false,
                        isNotify = true,
                        date = null
                    )

                    withContext(Dispatchers.IO) {
                        roomManager.removeTrackingById(item)
                        val newItem = item.copy(isArchived = needToBeArchived)
                        roomManager.insertParcel(newItem)
                    }
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.e(TAG, "Error archiving parcel", e)
            }
        }
    }

    fun updateItem(tracking: Tracking, title: String) {
        viewModelScope.launch {
            try {
                retryRequest {
                    apiService.updateTrackingById(
                        id = tracking.id,
                        name = title,
                        isArchive = tracking.isArchived == true,
                        isDeleted = false,
                        isNotify = false,
                        date = null
                    )

                    val updatedTracking = tracking.copy(title = title)

                    withContext(Dispatchers.IO) {
                        roomManager.insertParcel(updatedTracking)
                    }
                    currentTracking.value = updatedTracking
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.e(TAG, "Error updating item", e)
            }
        }
    }

    fun updateParcelStatus(tracking: Tracking?) {
        if (tracking == null) return
        viewModelScope.launch {
            try {
                val req = retryRequest {
                    apiService.refreshTracking(tracking.id)
                }

                if (req.error != null) {
                    Log.d(TAG, "updateParcelStatus failed: ${req.error?.message}")
                    return@launch
                }

                val reqBody = req.result
                if (reqBody != null) {
                    withContext(Dispatchers.IO) {
                        roomManager.insertParcel(reqBody)
                    }
                    currentTracking.value = reqBody
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.e(TAG, "Error updating parcel status", e)
            }
        }
    }

    companion object {
        private const val TAG = "SelectedElementComponent"
    }
}