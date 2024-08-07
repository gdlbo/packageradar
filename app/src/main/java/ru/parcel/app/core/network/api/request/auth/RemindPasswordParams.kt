package ru.parcel.app.core.network.api.request.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.parcel.app.core.network.api.request.Params

@Serializable
data class RemindPasswordParams(
    @SerialName("username") val username: String,
    @SerialName("deviceName") val deviceName: String,
    @SerialName("deviceId") val deviceId: String,
    @SerialName("platform") val platform: String
) : Params