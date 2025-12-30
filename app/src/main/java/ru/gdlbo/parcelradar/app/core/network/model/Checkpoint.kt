package ru.gdlbo.parcelradar.app.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Checkpoint(
    @SerialName("id") val id: Long,
    @SerialName("time") val time: String,
    @SerialName("courier") val courier: Courier,
    @SerialName("status_code") val statusCode: String?,
    @SerialName("status_name") val statusName: String?,
    @SerialName("status_raw") val statusRaw: String?,
    @SerialName("location_translated") val locationTranslated: String?,
    @SerialName("location_raw") val locationRaw: String?,
    @SerialName("location_zip_code") val locationZipCode: String?,
    @SerialName("location_color_light") val locationColorLight: String?,
    @SerialName("location_color_dark") val locationColorDark: String?,
    @SerialName("delivered") val delivered: Boolean? = false,
    @SerialName("last") val last: Boolean? = false,
    @SerialName("first") val first: Boolean? = false
) {
    fun isDelivered() = statusCode == "delivered"

    fun isArrived() = listOf("arrived", "arrived_locker", "arrived_pickup").contains(statusCode)
}