package ru.gdlbo.parcelradar.app.core.network.api.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.gdlbo.parcelradar.app.core.network.model.Tracking
import java.util.Collections

@Serializable
data class TrackingList(
    @SerialName("user") val user: Profile,
    @SerialName("recent_app_version") val recentAppVersion: String? = null,
    @SerialName("recent_app_build") val recentAppBuild: Int? = null,
    @SerialName("geoip_country_code") val geoipCountryCode: String? = null,
    @SerialName("advert_config") val advertConfig: String? = null,
    @SerialName("trackings") val trackings: List<Tracking>? = Collections.emptyList()
)