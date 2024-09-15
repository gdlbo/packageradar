package ru.gdlbo.parcelradar.app.core.network.api.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "profile")
data class Profile(
    @PrimaryKey(autoGenerate = false)
    @SerialName("id") val id: Long,
    @SerialName("email") val email: String,
    @SerialName("is_email_confirmed") val isEmailConfirmed: Boolean,
    @SerialName("apple_connected") val appleConnected: Boolean? = false,
    @SerialName("notify_email") val notifyEmail: Boolean,
    @SerialName("notify_push") val notifyPush: Boolean,
    @SerialName("country_code") val countryCode: String? = null,
    @SerialName("country_name_en") val countryNameEn: String? = null,
    @SerialName("country_name_ru") val countryNameRu: String? = null
)