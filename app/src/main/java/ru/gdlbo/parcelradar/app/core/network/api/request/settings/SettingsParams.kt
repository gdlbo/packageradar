package ru.gdlbo.parcelradar.app.core.network.api.request.settings

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.gdlbo.parcelradar.app.core.network.api.request.Params

@Serializable
data class SettingsParams(
    @SerialName("notifyEmail") val mailNotifications: Boolean,
    @SerialName("notifyPush") val pushNotifications: Boolean
) : Params