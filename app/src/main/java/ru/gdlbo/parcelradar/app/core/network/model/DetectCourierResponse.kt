package ru.gdlbo.parcelradar.app.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DetectCourierResponse(
    @SerialName("result") val result: String,
    @SerialName("length") val length: Int,
    @SerialName("trackingNumber") val trackingNumber: String,
    @SerialName("data") val data: List<DetectedCourier>
)