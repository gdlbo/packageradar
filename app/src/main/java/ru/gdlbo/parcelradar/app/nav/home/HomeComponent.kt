package ru.gdlbo.parcelradar.app.nav.home

import android.util.Log
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnResume
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.gdlbo.parcelradar.app.core.network.ApiHandler
import ru.gdlbo.parcelradar.app.core.network.api.entity.Detection
import ru.gdlbo.parcelradar.app.core.network.api.entity.TrackingList
import ru.gdlbo.parcelradar.app.core.network.api.request.tracking.UpdateTrackingListParams
import ru.gdlbo.parcelradar.app.core.network.api.response.BaseResponse
import ru.gdlbo.parcelradar.app.core.network.api.response.TrackingResponse
import ru.gdlbo.parcelradar.app.core.network.model.Tracking
import ru.gdlbo.parcelradar.app.core.network.retryRequest
import ru.gdlbo.parcelradar.app.di.room.RoomManager
import ru.gdlbo.parcelradar.app.di.theme.ThemeManager
import ru.gdlbo.parcelradar.app.nav.RootComponent
import ru.gdlbo.parcelradar.app.nav.archive.IScrollToUpComp
import java.text.SimpleDateFormat
import java.util.Locale

class HomeComponent(
    componentContext: ComponentContext,
    val navigateTo: (RootComponent.TopLevelConfiguration) -> Unit
) : ComponentContext by componentContext, KoinComponent, IScrollToUpComp {
    val viewModelScope = CoroutineScope(Dispatchers.Main.immediate)
    val themeManager: ThemeManager by inject()
    val roomManager: RoomManager by inject()
    val apiService: ApiHandler by inject()

    val loadState = MutableStateFlow<LoadState>(LoadState.Loading)
    val trackingItemList = MutableStateFlow<List<Tracking>>(emptyList())

    init {
        lifecycle.doOnResume {
            getFeedItems(false)
        }
    }

    fun search(query: String, isArchive: Boolean) {
        viewModelScope.launch {
            val loadedParcels = roomManager.loadParcels()

            if (loadedParcels.isNotEmpty() && query.isNotEmpty() && query.isNotBlank()) {
                val filteredParcels = loadedParcels.filter { parcel ->
                    (parcel.isArchived == isArchive) && (
                            parcel.title?.contains(query, ignoreCase = true) == true ||
                                    parcel.trackingNumber.contains(query, ignoreCase = true) == true
                            )
                }

                trackingItemList.value = filteredParcels
            } else {
                trackingItemList.value = loadedParcels.filter {
                    it.isArchived == isArchive
                }
            }
        }
    }

    fun readAllParcels() {
        viewModelScope.launch {
            try {
                val loadedParcels = roomManager.loadParcels()

                val parcels = loadedParcels.filter { parcel ->
                    parcel.isArchived == false
                }

                val updateTrackingList = parcels.mapNotNull { parcel ->
                    if (parcel.isUnread == true) {
                        parcel.copy(isUnread = false).toUpdateTracking()
                    } else {
                        null
                    }
                }

                if (updateTrackingList.isNotEmpty()) {
                    retryRequest {
                        apiService.updateTrackingList(
                            UpdateTrackingListParams(updateTrackingList)
                        )

                        trackingItemList.value = parcels.map { parcel ->
                            if (parcel.isUnread == true) {
                                parcel.copy(isUnread = false)
                            } else {
                                parcel
                            }
                        }
                    }
                } else {
                    trackingItemList.value = parcels
                }
            } catch (e: Exception) {
                e.fillInStackTrace()
            }
        }
    }

    fun updateReadParcel(item: Tracking) {
        item.isUnread?.let {
            viewModelScope.launch {
                try {
                    retryRequest {
                        apiService.updateTrackingById(
                            id = item.id,
                            name = item.title ?: "",
                            isArchive = false,
                            isDeleted = false,
                            isNotify = false,
                            date = null
                        )

                        trackingItemList.value = trackingItemList.value.filter { it != item }

                        val updatedItem = item.copy(isUnread = false)
                        trackingItemList.value =
                            trackingItemList.value.toTypedArray().plus(updatedItem).toList()

                        delay(1000)

                        getFeedItems(false)
                    }
                } catch (e: Exception) {
                    e.fillInStackTrace()
                }
            }
        }
    }

    fun archiveParcel(item: Tracking) {
        item.isArchived?.let {
            viewModelScope.launch {
                try {
                    retryRequest {
                        apiService.updateTrackingById(
                            id = item.id,
                            name = item.title ?: "",
                            isArchive = true,
                            isDeleted = false,
                            isNotify = true,
                            date = null
                        )

                        trackingItemList.value = trackingItemList.value.filter { it != item }
                        roomManager.removeTrackingById(item)

                        val newItem = item.copy(isArchived = true)

                        roomManager.insertParcel(newItem)
                        getFeedItems(false)
                    }
                } catch (e: Exception) {
                    e.fillInStackTrace()
                }
            }
        }
    }

    fun addTracking(parcelName: String, trackingNumber: String) {
        viewModelScope.launch {
            val response: HttpResponse = retryRequest {
                apiService.detect(trackingNumber.trim())
            }

            if (!response.status.isSuccess()) {
                loadState.value = LoadState.Error(response.status.description)
                return@launch
            }

            val detection = response.body<BaseResponse<Detection>>()
            val slug = detection.result?.couriers?.get(0)?.slug
            val trackingNum = detection.result?.couriers?.get(0)?.trackingNumber

            val response2: HttpResponse = retryRequest {
                apiService.addTracking(
                    trackingNum.toString(),
                    slug.toString(),
                    parcelName
                )
            }

            if (!response2.status.isSuccess()) {
                loadState.value = LoadState.Error(response2.status.description)
                return@launch
            }

            val addTracking = response2.body<BaseResponse<TrackingResponse>>()

            addTracking.result?.let {
                val newTracking = it.tracking.copy(isNew = true)
                roomManager.insertParcel(newTracking)
                trackingItemList.value = trackingItemList.value + newTracking
            }
        }
    }

    fun getFeedItems(forceUpdate: Boolean) {
        viewModelScope.launch {
            Log.d("HomeScreenViewModel", "Started fetching items with forceUpdate = $forceUpdate")
            loadState.value = LoadState.Loading

            if (forceUpdate) {
                Log.d("HomeScreenViewModel", "Dropping parcels from Room database")
                roomManager.dropParcels()
            }

            val profile = roomManager.loadProfile()
            val parcels = roomManager.loadParcels()

            if (profile != null && !forceUpdate && parcels.isNotEmpty()) {
                Log.d("HomeScreenViewModel", "Loaded ${parcels.size} parcels from database")
                handleLoadedParcels(parcels)
            } else {
                Log.d(
                    "HomeScreenViewModel",
                    "No parcels or profile found in database, fetching from network"
                )
                fetchAndStoreDataFromNetwork()
            }
        }
    }

    private fun handleLoadedParcels(parcels: List<Tracking>) {
        val activeTrackingItems = parcels.filter { it.isArchived == false }
        Log.d("HomeScreenViewModel", "Filtered active parcels: ${activeTrackingItems.size}")

        if (needsForceUpdate(activeTrackingItems)) {
            Log.d("HomeScreenViewModel", "Force update required due to outdated parcels")
            getFeedItems(true)
        } else {
            Log.d("HomeScreenViewModel", "No force update required, updating tracking list")
            updateTrackingList(activeTrackingItems)
        }
    }

    private fun needsForceUpdate(trackingItems: List<Tracking>): Boolean {
        val currentTime = System.currentTimeMillis()
        val result = trackingItems.any { parcel ->
            val nextCheckTime = parcel.nextCheck?.let {
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(it)?.time
            }
            val needsUpdate = nextCheckTime != null && nextCheckTime >= currentTime
            if (needsUpdate) {
                Log.d("HomeScreenViewModel", "Parcel with ID ${parcel.id} requires update")
            }
            needsUpdate
        }
        Log.d("HomeScreenViewModel", "Force update check result: $result")
        return result
    }

    private suspend fun fetchAndStoreDataFromNetwork() {
        Log.d("HomeScreenViewModel", "Starting network request for tracking list")
        val response = retryRequest {
            apiService.getTrackingList()
        }

        if (!response.status.isSuccess()) {
            val errorMsg = "Network request failed with status: ${response.status.description}"
            Log.e("HomeScreenViewModel", errorMsg)
            loadState.value = LoadState.Error(errorMsg)
            return
        }

        val feedBody = response.body<BaseResponse<TrackingList>>()
        val error = feedBody.error
        if (error != null) {
            val errorMsg = "API error: ${error.message}"
            Log.e("HomeScreenViewModel", errorMsg)
            loadState.value = LoadState.Error(errorMsg)
            return
        }

        Log.d("HomeScreenViewModel", "Successfully fetched tracking list from network")
        val profile = feedBody.result!!.user
        val trackingItems = feedBody.result.trackings?.map { item ->
            item.copy(checkpoints = item.checkpoints.sortedBy { checkpoint ->
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(checkpoint.time)
            })
        } ?: emptyList()

        Log.d(
            "HomeScreenViewModel",
            "Saving profile and ${trackingItems.size} tracking items to database"
        )
        roomManager.insertProfile(profile)
        roomManager.insertParcels(trackingItems)

        updateTrackingList(trackingItems.filter { it.isArchived == false })
    }

    private fun updateTrackingList(trackingItems: List<Tracking>) {
        Log.d("HomeScreenViewModel", "Updating tracking list with ${trackingItems.size} items")
        trackingItemList.value = trackingItems
        loadState.value = LoadState.Success
        Log.d("HomeScreenViewModel", "Tracking list updated successfully")

        if (trackingItems.any { it.isNew == true }) {
            Log.d("HomeScreenViewModel", "New parcels detected, fetching updated items after delay")
            viewModelScope.launch {
                delay(10000)
                getFeedItems(true)
            }
        }
    }

    override val isScrollable = MutableStateFlow(false)

    override fun scrollUp() {
        isScrollable.value = isScrollable.value.not()
    }
}