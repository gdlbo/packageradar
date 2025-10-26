package ru.gdlbo.parcelradar.app.nav.selected

import android.util.Log
import com.arkivanov.decompose.ComponentContext
import io.ktor.client.call.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.gdlbo.parcelradar.app.core.network.ApiHandler
import ru.gdlbo.parcelradar.app.core.network.api.response.BaseResponse
import ru.gdlbo.parcelradar.app.core.network.model.Tracking
import ru.gdlbo.parcelradar.app.core.network.retryRequest
import ru.gdlbo.parcelradar.app.di.prefs.AccessTokenManager
import ru.gdlbo.parcelradar.app.di.room.RoomManager
import ru.gdlbo.parcelradar.app.di.theme.ThemeManager
import java.util.*

class SelectedElementComponent(
    componentContext: ComponentContext,
    val id: Long,
    val popBack: () -> Unit,
) : ComponentContext by componentContext, KoinComponent {
    val viewModelScope = CoroutineScope(Dispatchers.Main.immediate)
    val roomManager: RoomManager by inject()
    val themeManager: ThemeManager by inject()
    val apiService: ApiHandler by inject()
    val atm: AccessTokenManager by inject()

    val currentTracking = MutableStateFlow<Tracking?>(null)

    fun deleteItem(tracking: Tracking?) {
        viewModelScope.launch {
            try {
                retryRequest {
                    apiService.updateTrackingById(
                        id = tracking!!.id,
                        name = tracking.title.toString(),
                        isArchive = tracking.isArchived!!,
                        isDeleted = true,
                        isNotify = false,
                        date = null
                    )

                    roomManager.removeTrackingById(tracking)
                }

            } catch (e: Exception) {
                e.fillInStackTrace()
            }
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
            roomManager.dropParcels()
        }
    }

    fun archiveParcel(item: Tracking, needToBeArchived: Boolean) {
        item.isArchived?.let {
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

                        roomManager.removeTrackingById(item)

                        val newItem = item.copy(isArchived = needToBeArchived)

                        roomManager.insertParcel(newItem)
                    }
                } catch (e: Exception) {
                    e.fillInStackTrace()
                }
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

                    roomManager.insertParcel(updatedTracking)
                    currentTracking.value = updatedTracking
                }
            } catch (e: Exception) {
                e.fillInStackTrace()
            }
        }
    }

    fun updateParcelStatus(tracking: Tracking?) {
        viewModelScope.launch {
            try {
                val req: HttpResponse = retryRequest {
                    apiService.refreshTracking(tracking!!.id)
                }

                if (!req.status.isSuccess()) {
                    Log.d("ParcelUpdate", "updateParcelStatus failed")
                    return@launch
                }

                val reqBody = req.body<BaseResponse<Tracking>>()

                roomManager.insertParcel(reqBody.result!!)
            } catch (e: Exception) {
                e.fillInStackTrace()
            }
        }
    }
}