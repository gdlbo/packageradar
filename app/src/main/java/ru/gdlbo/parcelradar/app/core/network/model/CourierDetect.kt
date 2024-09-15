package ru.gdlbo.parcelradar.app.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CourierDetect(
    @SerialName("slug") val slug: String,
    @SerialName("name") val name: String?,
    @SerialName("name_alt") val nameAlt: String?,
    @SerialName("country_code") val countryCode: String?,
    @SerialName("tracking_number") val trackingNumber: String
)