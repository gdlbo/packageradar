package ru.parcel.app.di.sync

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import io.ktor.client.call.body
import io.ktor.http.isSuccess
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.parcel.app.R
import ru.parcel.app.core.network.ApiHandler
import ru.parcel.app.core.network.api.entity.Profile
import ru.parcel.app.core.network.api.entity.TrackingList
import ru.parcel.app.core.network.api.response.BaseResponse
import ru.parcel.app.core.network.model.Tracking
import ru.parcel.app.core.network.retryRequest
import ru.parcel.app.di.room.RoomManager
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DataSyncManager : KoinComponent {
    val roomManager: RoomManager by inject()
    val apiService: ApiHandler by inject()

    suspend fun syncData(context: Context) {
        try {
            val isNotificationsEnabled = roomManager.loadNotifySettings().second

            if (!isNotificationsEnabled) {
                Log.d("DataSyncManager", "Notifications are disabled by server")
                return
            }

            Log.d("DataSyncManager", "Notifications are enabled")

            val (serverTrackingItems, profile) = fetchFromServer()
            val localTrackingItems = roomManager.loadParcels()

            syncNewAndUpdatedParcels(context, serverTrackingItems, localTrackingItems)

            Log.d("DataSyncManager", "Putting data to database")
            roomManager.insertParcels(serverTrackingItems)
            profile?.let { roomManager.insertProfile(it) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun fetchFromServer(): Pair<List<Tracking>, Profile?> {
        return try {
            val response = retryRequest { apiService.getTrackingList() }

            if (!response.status.isSuccess()) {
                Log.d("DataSync", "Error while request")
                return Pair(emptyList(), null)
            }

            val feedBody = response.body<BaseResponse<TrackingList>>()
            feedBody.let {
                val trackingItems = it.result?.trackings?.map { item ->
                    item.copy(checkpoints = item.checkpoints.sortedBy { checkpoint ->
                        checkpoint.time.toDate()
                    })
                } ?: emptyList()

                return Pair(trackingItems, it.result?.user)
            }

            Pair(emptyList(), null)
        } catch (e: Exception) {
            Log.d("DataSync", "Fetch from server failed: ${e.message}")
            Pair(emptyList(), null)
        }
    }

    private fun syncNewAndUpdatedParcels(context: Context, serverTrackingItems: List<Tracking>, localTrackingItems: List<Tracking>) {
        val nonArchivedServerItems = serverTrackingItems.filter { it.isArchived == false }
        val nonArchivedLocalItems = localTrackingItems.filter { it.isArchived == false }

        val newParcels = nonArchivedServerItems.filter { serverItem ->
            nonArchivedLocalItems.none { it.id == serverItem.id }
        }

        val updatedParcels = nonArchivedLocalItems.filter { localItem ->
            nonArchivedServerItems.any { it.id == localItem.id && it.checkpoints.lastOrNull() != localItem.checkpoints.lastOrNull() }
        }

        newParcels.forEach { parcel ->
            parcel.checkpoints.lastOrNull()?.let { lastCheckpoint ->
                showNotification(context, parcel.id, parcel.title ?: parcel.trackingNumber , lastCheckpoint.statusName.toString())
            }
        }

        updatedParcels.forEach { localItem ->
            nonArchivedServerItems.find { it.id == localItem.id }?.let { serverItem ->
                serverItem.checkpoints.lastOrNull()?.let { lastCheckpoint ->
                    showNotification(context, localItem.id, localItem.title ?: localItem.trackingNumber, lastCheckpoint.statusName.toString())
                }
            }
        }
    }

    private fun String.toDate(): Date? {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            format.parse(this)
        } catch (e: ParseException) {
            e.printStackTrace()
            null
        }
    }

    private fun showNotification(context: Context, parcelId: Long, parcelName: String, status: String) {
        val channelId = "parcel_updates"
        val notificationId = parcelId.toInt()

        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.package_2_24)
            .setContentTitle("Parcel Update")
            .setContentText("$parcelName has a new status: $status")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create the notification channel for Android Oreo and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "Parcel Updates"
            val channelDescription = "Notifications about parcel updates"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = channelDescription
            }
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(notificationId, notificationBuilder.build())
    }
}