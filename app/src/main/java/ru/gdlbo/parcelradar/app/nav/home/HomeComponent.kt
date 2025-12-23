package ru.gdlbo.parcelradar.app.nav.home

import android.util.Log
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnResume
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.gdlbo.parcelradar.app.core.network.ApiHandler
import ru.gdlbo.parcelradar.app.core.network.api.request.tracking.UpdateTrackingListParams
import ru.gdlbo.parcelradar.app.core.network.model.Tracking
import ru.gdlbo.parcelradar.app.core.network.retryRequest
import ru.gdlbo.parcelradar.app.di.room.RoomManager
import ru.gdlbo.parcelradar.app.di.theme.ThemeManager
import ru.gdlbo.parcelradar.app.nav.RootComponent
import ru.gdlbo.parcelradar.app.nav.archive.IScrollToUpComp
import java.text.SimpleDateFormat
import java.util.*

class HomeComponent(
    componentContext: ComponentContext,
    val navigateTo: (RootComponent.TopLevelConfiguration) -> Unit
) : ComponentContext by componentContext, KoinComponent, IScrollToUpComp {
    private val viewModelScope = CoroutineScope(Dispatchers.Main.immediate)
    val themeManager: ThemeManager by inject()
    private val roomManager: RoomManager by inject()
    private val apiService: ApiHandler by inject()

    val loadState = MutableStateFlow<LoadState>(LoadState.Loading)
    val trackingItemList = MutableStateFlow<List<Tracking>>(emptyList())

    override val isScrollable = MutableStateFlow(false)

    init {
        lifecycle.doOnResume {
            getFeedItems(false)
        }
    }

    fun search(query: String, isArchive: Boolean) {
        viewModelScope.launch {
            val loadedParcels = withContext(Dispatchers.IO) {
                roomManager.loadParcels()
            }

            trackingItemList.value = if (loadedParcels.isNotEmpty() && query.isNotBlank()) {
                withContext(Dispatchers.Default) {
                    loadedParcels.filter { parcel ->
                        ((parcel.isArchived ?: false) == isArchive) && (parcel.title?.contains(
                            query,
                            ignoreCase = true
                        ) == true || parcel.trackingNumber.contains(query, ignoreCase = true))
                    }
                }
            } else {
                withContext(Dispatchers.Default) {
                    loadedParcels.filter {
                        (it.isArchived ?: false) == isArchive
                    }
                }
            }
        }
    }

    fun readAllParcels() {
        viewModelScope.launch {
            try {
                val loadedParcels = withContext(Dispatchers.IO) {
                    roomManager.loadParcels()
                }
                val activeParcels = loadedParcels.filter { it.isArchived != true }

                val updateTrackingList = activeParcels.mapNotNull { parcel ->
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
                    }

                    trackingItemList.value = activeParcels.map { parcel ->
                        if (parcel.isUnread == true) parcel.copy(isUnread = false) else parcel
                    }
                } else {
                    trackingItemList.value = activeParcels
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error reading all parcels", e)
            }
        }
    }

    fun updateReadParcel(item: Tracking) {
        if (item.isUnread == true) {
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
                    }

                    val updatedItem = item.copy(isUnread = false)
                    updateLocalItem(updatedItem)

                    delay(1000)
                    getFeedItems(false)
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating read parcel", e)
                }
            }
        }
    }

    fun archiveParcel(item: Tracking) {
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
                }

                withContext(Dispatchers.IO) {
                    roomManager.removeTrackingById(item)
                    val newItem = item.copy(isArchived = true)
                    roomManager.insertParcel(newItem)
                }

                // Remove from current list
                trackingItemList.value = trackingItemList.value.filter { it.id != item.id }

                getFeedItems(false)
            } catch (e: Exception) {
                Log.e(TAG, "Error archiving parcel", e)
            }
        }
    }

    private fun updateLocalItem(newItem: Tracking) {
        val currentList = trackingItemList.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == newItem.id }
        if (index != -1) {
            currentList[index] = newItem
            trackingItemList.value = currentList
        }
    }

    fun addTracking(parcelName: String, trackingNumber: String) {
        viewModelScope.launch {
            try {
                val response = retryRequest {
                    apiService.detect(trackingNumber.trim())
                }

                if (response.error != null) {
                    loadState.value = LoadState.Error(response.error?.message ?: "Unknown error")
                    return@launch
                }

                val detection = response
                val courier = detection.result?.couriers?.firstOrNull()
                val slug = courier?.slug
                val trackingNum = courier?.trackingNumber

                if (slug == null || trackingNum == null) {
                    loadState.value = LoadState.Error("Could not detect courier")
                    return@launch
                }

                val response2 = retryRequest {
                    apiService.addTracking(
                        trackingNum.toString(),
                        slug.toString(),
                        parcelName
                    )
                }

                if (response2.error != null) {
                    loadState.value = LoadState.Error(response2.error?.message ?: "Unknown error")
                    return@launch
                }

                val addTracking = response2

                addTracking.result?.let {
                    val newTracking = it.tracking.copy(isNew = true)
                    withContext(Dispatchers.IO) {
                        roomManager.insertParcel(newTracking)
                    }
                    trackingItemList.value += newTracking
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error adding tracking", e)
                loadState.value = LoadState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun getFeedItems(forceUpdate: Boolean) {
        viewModelScope.launch {
            Log.d(TAG, "Started fetching items with forceUpdate = $forceUpdate")
            loadState.value = LoadState.Loading

            if (forceUpdate) {
                Log.d(TAG, "Dropping parcels from Room database")
                withContext(Dispatchers.IO) {
                    roomManager.dropParcels()
                }
            }

            val profile = withContext(Dispatchers.IO) { roomManager.loadProfile() }
            val parcels = withContext(Dispatchers.IO) { roomManager.loadParcels() }

            if (profile != null && !forceUpdate && parcels.isNotEmpty()) {
                Log.d(TAG, "Loaded ${parcels.size} parcels from database")
                handleLoadedParcels(parcels)
            } else {
                Log.d(TAG, "No parcels or profile found in database, fetching from network")
                fetchAndStoreDataFromNetwork()
            }
        }
    }

    private suspend fun handleLoadedParcels(parcels: List<Tracking>) {
        val activeTrackingItems = withContext(Dispatchers.Default) {
            parcels.filter { it.isArchived != true }
        }
        Log.d(TAG, "Filtered active parcels: ${activeTrackingItems.size}")

        if (needsForceUpdate(activeTrackingItems)) {
            Log.d(TAG, "Force update required due to outdated parcels")
            getFeedItems(true)
        } else {
            Log.d(TAG, "No force update required, updating tracking list")
            updateTrackingList(activeTrackingItems)
        }
    }

    private suspend fun needsForceUpdate(trackingItems: List<Tracking>): Boolean {
        return withContext(Dispatchers.Default) {
            val currentTime = System.currentTimeMillis()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

            val result = trackingItems.any { parcel ->
                val nextCheckTime = parcel.nextCheck?.let {
                    try {
                        dateFormat.parse(it)?.time
                    } catch (e: Exception) {
                        null
                    }
                }
                // Update if current time is past the next check time
                val needsUpdate = nextCheckTime != null && currentTime >= nextCheckTime
                if (needsUpdate) {
                    Log.d(TAG, "Parcel with ID ${parcel.id} requires update")
                }
                needsUpdate
            }
            Log.d(TAG, "Force update check result: $result")
            result
        }
    }

    private suspend fun fetchAndStoreDataFromNetwork() {
        Log.d(TAG, "Starting network request for tracking list")
        try {
            val response = retryRequest {
                apiService.getTrackingList()
            }

            if (response.error != null) {
                val errorMsg = "Network request failed with status: ${response.error?.message}"
                Log.e(TAG, errorMsg)
                loadState.value = LoadState.Error(errorMsg)
                return
            }

            val feedBody = response
            val error = feedBody.error
            if (error != null) {
                val errorMsg = "API error: ${error.message}"
                Log.e(TAG, errorMsg)
                loadState.value = LoadState.Error(errorMsg)
                return
            }

            Log.d(TAG, "Successfully fetched tracking list from network")
            val profile = feedBody.result!!.user
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

            val trackingItems = withContext(Dispatchers.Default) {
                feedBody.result.trackings?.map { item ->
                    item.copy(checkpoints = item.checkpoints.sortedBy { checkpoint ->
                        try {
                            dateFormat.parse(checkpoint.time)?.time ?: 0L
                        } catch (e: Exception) {
                            0L
                        }
                    })
                } ?: emptyList()
            }

            Log.d(TAG, "Saving profile and ${trackingItems.size} tracking items to database")
            withContext(Dispatchers.IO) {
                roomManager.insertProfile(profile)
                roomManager.insertParcels(trackingItems)
            }

            updateTrackingList(trackingItems.filter { it.isArchived != true })
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching data from network", e)
            loadState.value = LoadState.Error(e.message ?: "Unknown error")
        }
    }

    private fun updateTrackingList(trackingItems: List<Tracking>) {
        Log.d(TAG, "Updating tracking list with ${trackingItems.size} items")
        trackingItemList.value = trackingItems
        loadState.value = LoadState.Success
        Log.d(TAG, "Tracking list updated successfully")

        if (trackingItems.any { it.isNew }) {
            Log.d(TAG, "New parcels detected, fetching updated items after delay")
            viewModelScope.launch {
                delay(10000)
                getFeedItems(true)
            }
        }
    }

    override fun scrollUp() {
        isScrollable.value = !isScrollable.value
    }

    companion object {
        private const val TAG = "HomeScreenViewModel"
    }
}