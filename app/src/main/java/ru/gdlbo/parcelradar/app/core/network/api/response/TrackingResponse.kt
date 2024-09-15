package ru.gdlbo.parcelradar.app.core.network.api.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.gdlbo.parcelradar.app.core.network.model.Tracking

@Serializable
data class TrackingResponse(
    @SerialName("tracking") val tracking: Tracking
)