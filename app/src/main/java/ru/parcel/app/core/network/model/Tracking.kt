package ru.parcel.app.core.network.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.parcel.app.core.network.api.request.tracking.UpdateTracking

@Entity(tableName = "tracking")
@Serializable
data class Tracking(
    @PrimaryKey(autoGenerate = false)
    @SerialName("checkpoints") val checkpoints: List<Checkpoint> = emptyList(),
    @SerialName("courier") val courier: Courier? = null,
    @SerialName("tracking_number_current") val trackingNumberCurrent: String? = null,
    @SerialName("extra") val extra: List<Extra>? = emptyList(),
    @SerialName("hash") val hash: String,
    @SerialName("id") val id: Long,
    @SerialName("is_active") val isActive: Boolean? = false,
    @SerialName("is_archived") val isArchived: Boolean? = false,
    @SerialName("is_delivered") val isDelivered: Boolean? = false,
    @SerialName("is_ready_for_pickup") val isReadyForPickup: Boolean? = false,
    @SerialName("is_restorable") val isRestorable: Boolean? = false,
    @SerialName("is_unread") val isUnread: Boolean? = false,
    @SerialName("is_updating_now") val isUpdatingNow: Boolean? = false,
    @SerialName("last_check") val lastCheck: String? = null,
    @SerialName("last_checkpoint_id") val lastCheckpointId: Long? = 0,
    @SerialName("last_checkpoint_time") val lastCheckpointTime: String? = null,
    @SerialName("next_check") val nextCheck: String? = null,
    @SerialName("notification_time") val notificationTime: String? = null,
    @SerialName("tracking_number_secondary") val trackingNumberSecondary: String? = null,
    @SerialName("started_time") val startedTime: String,
    @SerialName("title") val title: String? = "Parcel",
    @SerialName("tracking_id") val trackingId: Long,
    @SerialName("tracking_number") val trackingNumber: String,
    var isNew: Boolean? = false
) {
    companion object {
        const val MAX_NOTIFICATION_DATE = 8640000000L
        const val MIN_NOTIFICATION_DATE = 86400000
        val TRACKING_REGEX = Regex("^[A-Z\\d\\-]{4,20}$")
    }

    fun toUpdateTracking(): UpdateTracking {
        return UpdateTracking(
            id = id,
            title = title,
            isArchived = isArchived,
            isDeleted = false,
            isUnread = isUnread,
            notificationTime = null
        )
    }
}