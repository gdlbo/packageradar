package ru.parcel.app.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TrackParcelResponse(
    @SerialName("result") val result: String,
    @SerialName("data") val data: TrackParcelData,
    @SerialName("messages") val messages: List<String>
)
