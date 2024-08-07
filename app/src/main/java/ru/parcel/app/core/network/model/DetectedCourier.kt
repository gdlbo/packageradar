package ru.parcel.app.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DetectedCourier(
    @SerialName("trackingNumber") val trackingNumber: String,
    @SerialName("courier") val courier: Courier,
    @SerialName("trackerUrl") val trackerUrl: String
)