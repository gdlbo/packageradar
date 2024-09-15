package ru.gdlbo.parcelradar.app.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Courier(
    @SerialName("slug") val slug: String,
    @SerialName("name") val name: String,
    @SerialName("name_alt") val nameAlt: String?,
    @SerialName("country_code") val countryCode: String?,
    @SerialName("review_count") val reviewCount: Int? = 0,
    @SerialName("review_score") val reviewScore: String? = null,
    @SerialName("review_url") val reviewUrl: String? = null
)