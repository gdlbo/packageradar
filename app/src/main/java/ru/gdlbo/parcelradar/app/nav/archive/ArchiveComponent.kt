package ru.gdlbo.parcelradar.app.nav.archive

import android.util.Log
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnResume
import io.ktor.client.call.*
import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.gdlbo.parcelradar.app.core.network.ApiHandler
import ru.gdlbo.parcelradar.app.core.network.api.entity.TrackingList
import ru.gdlbo.parcelradar.app.core.network.api.response.BaseResponse
import ru.gdlbo.parcelradar.app.core.network.model.Tracking
import ru.gdlbo.parcelradar.app.core.network.retryRequest
import ru.gdlbo.parcelradar.app.di.room.RoomManager
import ru.gdlbo.parcelradar.app.di.theme.ThemeManager
import ru.gdlbo.parcelradar.app.nav.RootComponent
import ru.gdlbo.parcelradar.app.nav.home.LoadState
import java.text.SimpleDateFormat
import java.util.*

class ArchiveComponent(
    val navigateTo: (RootComponent.TopLevelConfiguration) -> Unit,
    componentContext: ComponentContext
) : ComponentContext by componentContext, KoinComponent, IScrollToUpComp {
    val viewModelScope = CoroutineScope(Dispatchers.Main.immediate)
    val themeManager: ThemeManager by inject()
    private val roomManager: RoomManager by inject()
    private val apiHandler: ApiHandler by inject()

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
                    (parcel.isArchived == isArchive) && (parcel.title?.contains(
                        query,
                        ignoreCase = true
                    ) == true || parcel.trackingNumber.contains(query, ignoreCase = true))
                }

                trackingItemList.value = filteredParcels
            } else {
                trackingItemList.value = loadedParcels.filter {
                    it.isArchived == isArchive
                }
            }
        }
    }

    fun archiveParcel(item: Tracking) {
        item.isArchived?.let {
            viewModelScope.launch {
                try {
                    retryRequest {
                        apiHandler.updateTrackingById(
                            id = item.id,
                            name = item.title ?: "",
                            isArchive = false,
                            isDeleted = false,
                            isNotify = true,
                            date = null
                        )
                    }
                    trackingItemList.value = trackingItemList.value.filter { it.id != item.id }
                    roomManager.removeTrackingById(item)

                    val newItem = item.copy(isArchived = false)

                    roomManager.insertParcel(newItem)
                    getFeedItems(false)
                } catch (e: Exception) {
                    e.fillInStackTrace()
                }
            }
        }
    }

    fun getFeedItems(forceUpdate: Boolean) {
        viewModelScope.launch {
            Log.d(
                "ArchiveScreenViewModel",
                "Started fetching items with forceUpdate = $forceUpdate"
            )
            loadState.value = LoadState.Loading

            if (forceUpdate) {
                Log.d("ArchiveScreenViewModel", "Dropping parcels from Room database")
                roomManager.dropParcels()
            }

            val profile = roomManager.loadProfile()
            val parcels = roomManager.loadParcels()

            if (profile != null && !forceUpdate) {
                Log.d("ArchiveScreenViewModel", "Loaded ${parcels.size} parcels from database")
                handleLoadedParcels(parcels)
            } else {
                Log.d(
                    "ArchiveScreenViewModel",
                    "No parcels or profile found in database, fetching from network"
                )
                fetchAndStoreDataFromNetwork()
            }
        }
    }

    private fun handleLoadedParcels(parcels: List<Tracking>) {
        val activeTrackingItems = parcels.filter { it.isArchived == true }
        Log.d("ArchiveScreenViewModel", "Filtered active parcels: ${activeTrackingItems.size}")

        if (needsForceUpdate(activeTrackingItems)) {
            Log.d("ArchiveScreenViewModel", "Force update required due to outdated parcels")
            getFeedItems(true)
        } else {
            Log.d("ArchiveScreenViewModel", "No force update required, updating tracking list")
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
                Log.d("ArchiveScreenViewModel", "Parcel with ID ${parcel.id} requires update")
            }
            needsUpdate
        }
        Log.d("ArchiveScreenViewModel", "Force update check result: $result")
        return result
    }

    private suspend fun fetchAndStoreDataFromNetwork() {
        Log.d("ArchiveScreenViewModel", "Starting network request for tracking list")
        val response = retryRequest {
            apiHandler.getTrackingList()
        }

        if (!response.status.isSuccess()) {
            val errorMsg = "Network request failed with status: ${response.status.description}"
            Log.e("ArchiveScreenViewModel", errorMsg)
            loadState.value = LoadState.Error(errorMsg)
            return
        }

        val feedBody = response.body<BaseResponse<TrackingList>>()
        val error = feedBody.error
        if (error != null) {
            val errorMsg = "API error: ${error.message}"
            Log.e("ArchiveScreenViewModel", errorMsg)
            loadState.value = LoadState.Error(errorMsg)
            return
        }

        Log.d("ArchiveScreenViewModel", "Successfully fetched tracking list from network")
        val profile = feedBody.result!!.user
        val trackingItems = feedBody.result.trackings?.map { item ->
            item.copy(checkpoints = item.checkpoints.sortedBy { checkpoint ->
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(checkpoint.time)
            })
        } ?: emptyList()

        Log.d(
            "ArchiveScreenViewModel",
            "Saving profile and ${trackingItems.size} tracking items to database"
        )
        roomManager.insertProfile(profile)
        roomManager.insertParcels(trackingItems)

        updateTrackingList(trackingItems.filter { it.isArchived == true })
    }

    private fun updateTrackingList(trackingItems: List<Tracking>) {
        Log.d("ArchiveScreenViewModel", "Updating tracking list with ${trackingItems.size} items")
        trackingItemList.value = trackingItems
        loadState.value = LoadState.Success
        Log.d("ArchiveScreenViewModel", "Tracking list updated successfully")
    }

    override val isScrollable = MutableStateFlow(false)

    override fun scrollUp() {
        isScrollable.value = isScrollable.value.not()
    }
}