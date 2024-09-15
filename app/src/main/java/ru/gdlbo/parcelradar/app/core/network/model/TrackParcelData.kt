package ru.gdlbo.parcelradar.app.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TrackParcelData(
    @SerialName("id") val id: Int,
    @SerialName("trackingNumber") val trackingNumber: String,
    @SerialName("courier") val courier: Courier,
    @SerialName("isActive") val isActive: Boolean,
    @SerialName("isDelivered") val isDelivered: Boolean,
    @SerialName("lastCheck") val lastCheck: String?,
    @SerialName("checkpoints") val checkpoints: List<Checkpoint>,
    @SerialName("extra") val extra: List<ExtraInfo>
)