package ru.gdlbo.parcelradar.app.core.network.api.request.tracking

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.gdlbo.parcelradar.app.core.network.api.request.Params

@Serializable
data class UpdateTracking(
    @SerialName("id") val id: Long,
    @SerialName("title") val title: String?,
    @SerialName("is_archived") val isArchived: Boolean?,
    @SerialName("is_deleted") val isDeleted: Boolean?,
    @SerialName("is_unread") val isUnread: Boolean?,
    @SerialName("notification_time") val notificationTime: String?
) : Params