package ru.parcel.app.core.network.api.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BaseResponse<T>(
    @SerialName("id") val id: String,
    @SerialName("result") val result: T? = null,
    @SerialName("error") var error: ApiError? = null,
    @SerialName("jsonrpc") var jsonrpc: String? = "2.0"
)