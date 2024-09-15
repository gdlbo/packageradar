package ru.gdlbo.parcelradar.app.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.gdlbo.parcelradar.app.core.network.api.entity.SocialType

@Serializable
data class AuthToken(
    @SerialName("method") val method: SocialType,
    @SerialName("token") val token: String
)