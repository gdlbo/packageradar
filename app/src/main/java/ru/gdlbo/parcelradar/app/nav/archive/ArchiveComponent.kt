package ru.gdlbo.parcelradar.app.nav.archive

import android.os.Build
import android.util.Log
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.essenty.lifecycle.doOnResume
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.gdlbo.parcelradar.app.core.network.ApiHandler
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
    private val scope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())
    val themeManager: ThemeManager by inject()
    private val roomManager: RoomManager by inject()
    private val apiHandler: ApiHandler by inject()

    val loadState = MutableStateFlow<LoadState>(LoadState.Loading)
    val trackingItemList = MutableStateFlow<List<Tracking>>(emptyList())

    init {
        lifecycle.doOnResume {
            getFeedItems(false)
        }
        lifecycle.doOnDestroy {
            scope.cancel()
        }
    }

    fun search(query: String) {
        scope.launch {
            val loadedParcels = withContext(Dispatchers.IO) {
                roomManager.loadParcels()
            }
            val isArchive = true

            val filteredParcels = withContext(Dispatchers.Default) {
                if (loadedParcels.isNotEmpty() && query.isNotBlank()) {
                    loadedParcels.filter { parcel ->
                        (parcel.isArchived == isArchive) && (parcel.title?.contains(
                            query,
                            ignoreCase = true
                        ) == true || parcel.trackingNumber.contains(query, ignoreCase = true))
                    }
                } else {
                    loadedParcels.filter {
                        it.isArchived == isArchive
                    }
                }
            }
            trackingItemList.value = sortTrackings(filteredParcels).distinctBy { it.id }
        }
    }

    fun restoreParcel(item: Tracking) {
        scope.launch {
            delay(300) // Wait for swipe animation
            // Optimistic update
            val currentList = trackingItemList.value.toMutableList()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                currentList.removeIf { it.id == item.id }
            } else {
                currentList.removeAll { it.id == item.id }
            }
            trackingItemList.value = currentList

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

                withContext(Dispatchers.IO) {
                    val newItem = item.copy(isArchived = false)
                    roomManager.updateParcel(newItem)
                }
            } catch (e: Exception) {
                Log.e("ArchiveComponent", "Error restoring parcel", e)
                // Revert optimistic update if needed, or handle error
                getFeedItems(false)
            }
        }
    }

    fun getFeedItems(forceUpdate: Boolean) {
        scope.launch {
            Log.d(
                "ArchiveComponent",
                "Started fetching items with forceUpdate = $forceUpdate"
            )
            loadState.value = LoadState.Loading

            val profile = withContext(Dispatchers.IO) { roomManager.loadProfile() }
            val parcels = withContext(Dispatchers.IO) { roomManager.loadParcels() }

            if (profile != null && !forceUpdate && parcels.isNotEmpty()) {
                Log.d("ArchiveComponent", "Loaded ${parcels.size} parcels from database")
                handleLoadedParcels(parcels)
            } else {
                Log.d(
                    "ArchiveComponent",
                    "No parcels or profile found in database or force update, fetching from network"
                )
                fetchAndStoreDataFromNetwork()
            }
        }
    }

    private suspend fun handleLoadedParcels(parcels: List<Tracking>) {
        val activeTrackingItems = withContext(Dispatchers.Default) {
            parcels.filter { it.isArchived == true }
        }
        Log.d("ArchiveComponent", "Filtered active parcels: ${activeTrackingItems.size}")

        if (needsForceUpdate(activeTrackingItems)) {
            Log.d("ArchiveComponent", "Force update required due to outdated parcels")
            fetchAndStoreDataFromNetwork()
        } else {
            Log.d("ArchiveComponent", "No force update required, updating tracking list")
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
                val needsUpdate = nextCheckTime != null && currentTime >= nextCheckTime
                if (needsUpdate) {
                    Log.d("ArchiveComponent", "Parcel with ID ${parcel.id} requires update")
                }
                needsUpdate
            }
            Log.d("ArchiveComponent", "Force update check result: $result")
            result
        }
    }

    private suspend fun fetchAndStoreDataFromNetwork() {
        Log.d("ArchiveComponent", "Starting network request for tracking list")
        try {
            val response = retryRequest {
                apiHandler.getTrackingList()
            }

            if (response.error != null) {
                val errorMsg = "Network request failed with status: ${response.error?.message}"
                Log.e("ArchiveComponent", errorMsg)
                loadState.value = LoadState.Error(errorMsg)
                return
            }

            val result = response.result
            if (result == null) {
                val errorMsg = "API returned empty result"
                Log.e("ArchiveComponent", errorMsg)
                loadState.value = LoadState.Error(errorMsg)
                return
            }

            Log.d("ArchiveComponent", "Successfully fetched tracking list from network")
            val profile = result.user
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

            val trackingItems = withContext(Dispatchers.Default) {
                result.trackings?.map { item ->
                    item.copy(checkpoints = item.checkpoints.sortedBy { checkpoint ->
                        try {
                            dateFormat.parse(checkpoint.time)?.time ?: 0L
                        } catch (e: Exception) {
                            0L
                        }
                    })
                } ?: emptyList()
            }

            Log.d(
                "ArchiveComponent",
                "Saving profile and ${trackingItems.size} tracking items to database"
            )
            withContext(Dispatchers.IO) {
                roomManager.dropParcels()
                roomManager.insertProfile(profile)
                roomManager.insertParcels(trackingItems)
            }

            updateTrackingList(trackingItems.filter { it.isArchived == true })
        } catch (e: Exception) {
            val errorMsg = "Exception during network request: ${e.message}"
            Log.e("ArchiveComponent", errorMsg, e)
            loadState.value = LoadState.Error(errorMsg)
        }
    }

    private fun updateTrackingList(trackingItems: List<Tracking>) {
        Log.d("ArchiveComponent", "Updating tracking list with ${trackingItems.size} items")
        trackingItemList.value = sortTrackings(trackingItems).distinctBy { it.id }
        loadState.value = LoadState.Success
        Log.d("ArchiveComponent", "Tracking list updated successfully")
    }

    private fun sortTrackings(items: List<Tracking>): List<Tracking> {
        return items.sortedWith(
            compareByDescending<Tracking> { it.isNew }
                .thenByDescending { it.lastCheckpointTime ?: "" }
                .thenByDescending { it.startedTime }
        )
    }

    override val isScrollable = MutableStateFlow(false)

    override fun scrollUp() {
        isScrollable.value = !isScrollable.value
    }
}
