package ru.parcel.app.core.network.api.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BaseRequest<T : Params>(
    @SerialName("id") val id: String,
    @SerialName("jsonRpc") val jsonRpc: String,
    @SerialName("method") val method: String,
    @SerialName("params") val params: T
)