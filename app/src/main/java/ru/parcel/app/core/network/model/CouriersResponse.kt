package ru.parcel.app.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CouriersResponse(
    @SerialName("result") val result: String,
    @SerialName("length") val length: Int,
    @SerialName("data") val data: List<Courier>
)