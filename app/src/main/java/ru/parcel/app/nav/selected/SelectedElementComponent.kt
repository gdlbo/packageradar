package ru.parcel.app.nav.selected

import android.util.Log
import com.arkivanov.decompose.ComponentContext
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.parcel.app.core.network.ApiHandler
import ru.parcel.app.core.network.api.response.BaseResponse
import ru.parcel.app.core.network.model.Tracking
import ru.parcel.app.core.network.retryRequest
import ru.parcel.app.di.prefs.AccessTokenManager
import ru.parcel.app.di.room.RoomManager
import ru.parcel.app.di.theme.ThemeManager
import java.util.Locale

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

    fun updateItem(tracking: Tracking?, title: String) {
        viewModelScope.launch {
            try {
                retryRequest {
                    apiService.updateTrackingById(
                        id = tracking!!.id,
                        name = title,
                        isArchive = tracking.isArchived!!,
                        isDeleted = true,
                        isNotify = false,
                        date = null
                    )

                    val updatedTracking = tracking.copy(title = title)

                    roomManager.insertParcel(updatedTracking)
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