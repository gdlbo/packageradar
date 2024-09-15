package ru.gdlbo.parcelradar.app.core.network.api.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiError(
    @SerialName("code") val code: Int,
    @SerialName("message") var message: String,
    @SerialName("data") val data: Map<String, String>? = null,
    @SerialName("apiError") var apiError: Boolean? = false
)