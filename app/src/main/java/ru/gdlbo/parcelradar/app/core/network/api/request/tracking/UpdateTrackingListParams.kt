package ru.gdlbo.parcelradar.app.core.network.api.request.tracking

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.gdlbo.parcelradar.app.core.network.api.request.Params

@Serializable
data class UpdateTrackingListParams(
    @SerialName("trackingList") val trackingList: List<UpdateTracking>
) : Params