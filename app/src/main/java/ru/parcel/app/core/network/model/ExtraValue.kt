package ru.parcel.app.core.network.model

import android.content.Context
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ExtraValue(
    @SerialName("key") val key: String,
    @SerialName("title") val title: String?,
    @SerialName("value") val value: String
) {
    companion object {
        const val ALTERNATIVE_TRACKING_NUMBER = "track.extra.fields.alternative_tracking_number"
        const val SHIPMENT_ORDER_NUMBER = "track.extra.fields.shipment_order_number"
        const val SHIPMENT_ORDER_NUMBER_INTERNAL =
            "track.extra.fields.shipment_order_number_internal"
    }

    fun getTranslatedTitle(context: Context): String {
        return when (val title = this.title) {
            ALTERNATIVE_TRACKING_NUMBER -> "Alternative number"
            SHIPMENT_ORDER_NUMBER -> "Order number"
            SHIPMENT_ORDER_NUMBER_INTERNAL -> "Internal order number"
            else -> title ?: ""
        }
    }
}