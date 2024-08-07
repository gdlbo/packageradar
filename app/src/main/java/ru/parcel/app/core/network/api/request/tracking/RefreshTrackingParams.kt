package ru.parcel.app.core.network.api.request.tracking

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.parcel.app.core.network.api.request.Params

@Serializable
data class RefreshTrackingParams(
    @SerialName("id") val id: Long
) : Params