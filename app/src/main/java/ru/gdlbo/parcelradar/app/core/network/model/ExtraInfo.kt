package ru.gdlbo.parcelradar.app.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ExtraInfo(
    @SerialName("courierSlug") val courierSlug: String,
    @SerialName("data") val data: Map<String, String>
)