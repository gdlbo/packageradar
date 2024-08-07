package ru.parcel.app.core.network.api.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Detection(
    @SerialName("couriers") val couriers: List<CourierDetect>
)