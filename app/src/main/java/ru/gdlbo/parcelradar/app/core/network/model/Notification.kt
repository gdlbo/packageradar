package ru.gdlbo.parcelradar.app.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Notification(
    @SerialName("title") val title: String,
    @SerialName("text") val text: String,
    @SerialName("date") val date: String
)