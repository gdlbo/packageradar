package ru.gdlbo.parcelradar.app.core.network.api.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class SocialType {
    @SerialName("vk")
    VK,

    @SerialName("google")
    GOOGLE
}