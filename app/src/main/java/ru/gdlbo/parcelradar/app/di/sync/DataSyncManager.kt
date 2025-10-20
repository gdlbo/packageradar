package ru.gdlbo.parcelradar.app.di.sync

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import io.ktor.client.call.body
import io.ktor.http.isSuccess
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.gdlbo.parcelradar.app.MainActivity
import ru.gdlbo.parcelradar.app.R
import ru.gdlbo.parcelradar.app.core.network.ApiHandler
import ru.gdlbo.parcelradar.app.core.network.api.entity.Profile
import ru.gdlbo.parcelradar.app.core.network.api.entity.TrackingList
import ru.gdlbo.parcelradar.app.core.network.api.response.BaseResponse
import ru.gdlbo.parcelradar.app.core.network.model.Tracking
import ru.gdlbo.parcelradar.app.core.network.retryRequest
import ru.gdlbo.parcelradar.app.di.prefs.AccessTokenManager
import ru.gdlbo.parcelradar.app.di.prefs.SettingsManager
import ru.gdlbo.parcelradar.app.di.room.RoomManager
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DataSyncManager : KoinComponent {
    val roomManager: RoomManager by inject()
    val apiService: ApiHandler by inject()
    val settingsManager = SettingsManager()
    val atm: AccessTokenManager by inject()

    suspend fun syncData(context: Context) {
        try {
            val isNotificationsEnabled = settingsManager.arePushNotificationsEnabled
            val areSystemNotificationsEnabled =
                NotificationManagerCompat.from(context).areNotificationsEnabled()

            if (isNotificationsEnabled && areSystemNotificationsEnabled && atm.hasAccessToken()) {
                val (serverTrackingItems, profile) = fetchFromServer()
                val localTrackingItems = roomManager.loadParcels()

                syncNewAndUpdatedParcels(context, serverTrackingItems, localTrackingItems)

                if (serverTrackingItems.isNotEmpty()) {
                    Log.d("DataSync", "Putting data to database")
                    roomManager.dropParcels()
                    roomManager.insertParcels(serverTrackingItems)
                    roomManager.insertProfile(profile!!)
                }
            } else {
                Log.d("DataSync", "Notifications are disabled")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun fetchFromServer(): Pair<List<Tracking>, Profile?> {
        return try {
            val response = retryRequest { apiService.getTrackingList() }

            if (!response.status.isSuccess()) {
                Log.e("DataSync", "Error while request: ${response.status}")
                return Pair(emptyList(), null)
            }

            val feedBody = response.body<BaseResponse<TrackingList>>()

            feedBody.let {
                val trackingItems = it.result?.trackings?.map { item ->
                    item.copy(checkpoints = item.checkpoints.sortedBy { checkpoint ->
                        checkpoint.time.toDate()
                    })
                } ?: emptyList()

                Log.d("DataSync", "Fetched ${trackingItems.size} tracking items")
                return Pair(trackingItems, it.result?.user)
            }

            Pair(emptyList(), null)
        } catch (e: Exception) {
            Log.e("DataSync", "Fetch from server failed: ${e.message}", e)
            Pair(emptyList(), null)
        }
    }

    private fun syncNewAndUpdatedParcels(
        context: Context,
        serverTrackingItems: List<Tracking>,
        localTrackingItems: List<Tracking>
    ) {
        val nonArchivedServerItems = serverTrackingItems.filter { it.isArchived == false }
        val nonArchivedLocalItems = localTrackingItems.filter { it.isArchived == false }
        val archivedLocalItems = localTrackingItems.filter { it.isArchived == true }

        Log.d("DataSync", "Non-archived server items: ${nonArchivedServerItems.size}")
        Log.d("DataSync", "Non-archived local items: ${nonArchivedLocalItems.size}")

        val newParcels = nonArchivedServerItems.filter { serverItem ->
            nonArchivedLocalItems.none { it.id == serverItem.id } &&
                    archivedLocalItems.none { it.id == serverItem.id }
        }

        val updatedParcels = nonArchivedLocalItems.filter { localItem ->
            nonArchivedServerItems.any { serverItem ->
                serverItem.id == localItem.id &&
                        serverItem.checkpoints.size != localItem.checkpoints.size &&
                        serverItem.checkpoints.lastOrNull() != localItem.checkpoints.lastOrNull()
            }
        }

        Log.d("DataSync", "New parcels: ${newParcels.size}")
        Log.d("DataSync", "Updated parcels: ${updatedParcels.size}")

        newParcels.forEach { parcel ->
            parcel.checkpoints.lastOrNull()?.let { lastCheckpoint ->
                showNotification(
                    context,
                    parcel.id,
                    parcel.title ?: parcel.trackingNumber,
                    lastCheckpoint.statusName.toString(),
                    true
                )
            }
        }

        updatedParcels.forEach { localItem ->
            nonArchivedServerItems.find { it.id == localItem.id }?.let { serverItem ->
                serverItem.checkpoints.lastOrNull()?.let { lastCheckpoint ->
                    showNotification(
                        context,
                        localItem.id,
                        localItem.title ?: localItem.trackingNumber,
                        lastCheckpoint.statusName.toString(),
                        false
                    )
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

    private fun showNotification(
        context: Context,
        parcelId: Long,
        parcelName: String,
        status: String,
        isNewParcel: Boolean
    ) {
        val channelId = "parcel_updates"
        val notificationId = parcelId.toInt()

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("parcelId", parcelId)
        }

        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.package_2_24)
            .setContentTitle(context.getString(R.string.parcel_update_title))
            .setContentText(
                if (isNewParcel) {
                    context.getString(R.string.parcel_new_text, parcelName)
                } else {
                    context.getString(R.string.parcel_update_text, parcelName, status)
                }
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create the notification channel for Android Oreo and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = context.getString(R.string.parcel_updates_channel_name)
            val channelDescription = context.getString(R.string.parcel_updates_channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = channelDescription
            }
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(notificationId, notificationBuilder.build())
    }
}