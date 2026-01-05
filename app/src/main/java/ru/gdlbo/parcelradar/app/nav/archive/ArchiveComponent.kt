package ru.gdlbo.parcelradar.app.nav.archive

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

    // Scroll state persistence
    var listScrollIndex = 0
    var listScrollOffset = 0
    var gridScrollIndex = 0
    var gridScrollOffset = 0

    init {
        lifecycle.doOnResume {
            if (trackingItemList.value.isEmpty()) {
                getFeedItems(false)
            }
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

    fun toggleArchive(item: Tracking) {
        scope.launch {
            val shouldArchive = !(item.isArchived ?: false)
            
            // Optimistic update
            val previousList = trackingItemList.value
            trackingItemList.value = previousList.filter { it.id != item.id }

            try {
                withContext(Dispatchers.IO) {
                    roomManager.updateArchiveStatus(item.id, shouldArchive)
                }

                retryRequest {
                    apiHandler.updateTrackingById(
                        id = item.id,
                        name = item.title ?: "",
                        isArchive = shouldArchive,
                        isDeleted = false,
                        isNotify = true,
                        date = null
                    )
                }
            } catch (e: Exception) {
                Log.e("ArchiveComponent", "Error toggling archive for parcel", e)
                // Revert optimistic update on failure
                trackingItemList.value = previousList
            }
        }
    }

    fun getFeedItems(forceUpdate: Boolean) {
        scope.launch {
            Log.d(
                "ArchiveComponent",
                "Started fetching items with forceUpdate = $forceUpdate"
            )

            if (trackingItemList.value.isEmpty()) {
                loadState.value = LoadState.Loading
            } else if (forceUpdate) {
                loadState.value = LoadState.Refreshing
            }

            val profile = withContext(Dispatchers.IO) { roomManager.loadProfile() }
            val parcels = withContext(Dispatchers.IO) { roomManager.loadParcels() }

            val archivedParcels = parcels.filter { it.isArchived == true }

            if (archivedParcels.isNotEmpty()) {
                Log.d("ArchiveComponent", "Loaded ${archivedParcels.size} archived parcels from database")
                updateTrackingList(archivedParcels)
            }

            val shouldFetch = forceUpdate ||
                    profile == null ||
                    parcels.isEmpty() ||
                    (archivedParcels.isNotEmpty() && needsForceUpdate(archivedParcels))

            if (shouldFetch) {
                Log.d(
                    "ArchiveComponent",
                    "Fetching from network (force=$forceUpdate, profile=${profile != null}, parcels=${parcels.size})"
                )
                fetchAndStoreDataFromNetwork()
            } else if (archivedParcels.isEmpty()) {
                Log.d("ArchiveComponent", "No archived parcels and no update needed")
                updateTrackingList(emptyList())
            }
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
                if (trackingItemList.value.isEmpty()) {
                    loadState.value = LoadState.Error(errorMsg)
                } else {
                    loadState.value = LoadState.Success
                }
                return
            }

            val result = response.result
            if (result == null) {
                val errorMsg = "API returned empty result"
                Log.e("ArchiveComponent", errorMsg)
                if (trackingItemList.value.isEmpty()) {
                    loadState.value = LoadState.Error(errorMsg)
                } else {
                    loadState.value = LoadState.Success
                }
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
            if (trackingItemList.value.isEmpty()) {
                loadState.value = LoadState.Error(errorMsg)
            } else {
                loadState.value = LoadState.Success
            }
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