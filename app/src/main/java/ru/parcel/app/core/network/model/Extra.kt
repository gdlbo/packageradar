package ru.parcel.app.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Extra(
    @SerialName("id") val id: Long,
    @SerialName("courier") val courier: Courier,
    @SerialName("values") val values: List<ExtraValue>
)