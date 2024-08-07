package ru.parcel.app.core.network.api.request.tracking

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.parcel.app.core.network.api.request.Params

@Serializable
data class AddTrackingParams(
    @SerialName("trackingNumber") val tracking: String,
    @SerialName("courierSlug") val couruerSlug: String,
    @SerialName("title") val title: String
) : Params