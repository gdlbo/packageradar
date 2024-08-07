package ru.parcel.app.core.network.api.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Auth(
    @SerialName("id") val id: Long,
    @SerialName("email") val email: String,
    @SerialName("username") val username: String? = null,
    @SerialName("access_token") val accessToken: String,
    @SerialName("is_email_confirmed") val isEmailConfirmed: Boolean
)