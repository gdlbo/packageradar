package ru.gdlbo.parcelradar.app.core.network

import io.ktor.client.*
import ru.gdlbo.parcelradar.app.core.network.api.ApiServiceImpl
import ru.gdlbo.parcelradar.app.core.network.api.entity.Auth
import ru.gdlbo.parcelradar.app.core.network.api.entity.Detection
import ru.gdlbo.parcelradar.app.core.network.api.entity.TrackingList
import ru.gdlbo.parcelradar.app.core.network.api.request.BaseRequest
import ru.gdlbo.parcelradar.app.core.network.api.request.EmptyParams
import ru.gdlbo.parcelradar.app.core.network.api.request.auth.AuthParams
import ru.gdlbo.parcelradar.app.core.network.api.request.auth.RemindPasswordParams
import ru.gdlbo.parcelradar.app.core.network.api.request.settings.SettingsParams
import ru.gdlbo.parcelradar.app.core.network.api.request.tracking.*
import ru.gdlbo.parcelradar.app.core.network.api.response.BaseResponse
import ru.gdlbo.parcelradar.app.core.network.api.response.TrackingResponse
import ru.gdlbo.parcelradar.app.core.network.model.Tracking
import ru.gdlbo.parcelradar.app.core.utils.DeviceUtils
import java.text.SimpleDateFormat
import java.util.*

class ApiHandler(client: HttpClient) {
    private val api = ApiServiceImpl(client)

    companion object {
        const val ADD_TRACKING = "profile.addTracking"
        const val AUTH = "auth.getTokenByCredentials"
        const val DETECT = "tracking.detect"
        const val GET_TRACKING_LIST = "profile.getTrackingList"
        const val REFRESH_TRACKING = "profile.refreshTracking"
        const val REGISTER = "auth.register"
        const val REMIND_PASSWORD = "auth.remindPassword"
        const val RESEND_CONFIRMATION = "auth.resendConfirmation"
        const val SET_NOTIFICATION_SETTINGS = "profile.setNotificationSettings"
        const val UPDATE_TRACKING_LIST = "profile.updateTrackingList"
    }

    suspend fun register(login: String, password: String): BaseResponse<Auth> {
        return api.register(
            BaseRequest(
                "id",
                "2.0",
                REGISTER,
                AuthParams(
                    login,
                    password,
                    DeviceUtils.getDeviceName(),
                    DeviceUtils.getAndroidId(),
                    "android"
                )
            )
        )
    }

    suspend fun auth(login: String, password: String): BaseResponse<Auth> {
        return api.authenticate(
            BaseRequest(
                "id",
                "2.0",
                AUTH,
                AuthParams(
                    login,
                    password,
                    DeviceUtils.getDeviceName(),
                    DeviceUtils.getAndroidId(),
                    "android"
                )
            )
        )
    }

    suspend fun resendConfirmation(): BaseResponse<Any> {
        return api.resendConfirmation(
            BaseRequest(
                "id",
                "2.0",
                RESEND_CONFIRMATION,
                EmptyParams()
            )
        )
    }

    suspend fun remindPassword(login: String): BaseResponse<Any> {
        return api.remindPassword(
            BaseRequest(
                "id",
                "2.0",
                REMIND_PASSWORD,
                RemindPasswordParams(
                    login,
                    DeviceUtils.getDeviceName(),
                    DeviceUtils.getAndroidId(),
                    "android"
                )
            )
        )
    }

    suspend fun updateTrackingById(
        id: Long,
        name: String,
        isArchive: Boolean,
        isDeleted: Boolean,
        isNotify: Boolean,
        date: Date?
    ): BaseResponse<Any> {
        return api.updateTrackingList(
            BaseRequest(
                "id",
                "2.0",
                UPDATE_TRACKING_LIST,
                UpdateTrackingListParams(
                    listOf(
                        UpdateTracking(
                            id,
                            name,
                            isArchive,
                            isDeleted,
                            isNotify,
                            date?.let {
                                val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                return@let format.format(it)
                            }
                        )
                    )
                )
            )
        )
    }

    suspend fun updateTrackingList(updateTrackingListParams: UpdateTrackingListParams): BaseResponse<Any> {
        return api.updateTrackingList(
            BaseRequest(
                "id",
                "2.0",
                UPDATE_TRACKING_LIST,
                updateTrackingListParams
            )
        )
    }

    suspend fun getTrackingList(): BaseResponse<TrackingList> {
        return api.getTrackingList(
            BaseRequest(
                "id",
                "2.0",
                GET_TRACKING_LIST,
                EmptyParams()
            )
        )
    }

    suspend fun addTracking(
        track: String,
        courierSlug: String,
        name: String
    ): BaseResponse<TrackingResponse> {
        return api.addTracking(
            BaseRequest(
                "id",
                "2.0",
                ADD_TRACKING,
                AddTrackingParams(track, courierSlug, name)
            )
        )
    }

    suspend fun refreshTracking(id: Long): BaseResponse<Tracking> {
        return api.refreshTracking(
            BaseRequest(
                "id",
                "2.0",
                REFRESH_TRACKING,
                RefreshTrackingParams(id)
            )
        )
    }

    suspend fun detect(track: String): BaseResponse<Detection> {
        return api.detect(
            BaseRequest(
                "id",
                "2.0",
                DETECT,
                DetectParams(track)
            )
        )
    }

    suspend fun setNotificationsSettings(
        isPushEnabled: Boolean,
        isEmailEnabled: Boolean
    ): BaseResponse<Any> {
        return api.setNotificationsSettings(
            BaseRequest(
                "id",
                "2.0",
                SET_NOTIFICATION_SETTINGS,
                SettingsParams(isEmailEnabled, isPushEnabled)
            )
        )
    }
}