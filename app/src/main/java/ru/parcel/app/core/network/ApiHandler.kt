package ru.parcel.app.core.network

import io.ktor.client.HttpClient
import io.ktor.client.statement.HttpResponse
import ru.parcel.app.core.network.api.ApiServiceImpl
import ru.parcel.app.core.network.api.request.BaseRequest
import ru.parcel.app.core.network.api.request.EmptyParams
import ru.parcel.app.core.network.api.request.auth.AuthParams
import ru.parcel.app.core.network.api.request.auth.RemindPasswordParams
import ru.parcel.app.core.network.api.request.settings.SettingsParams
import ru.parcel.app.core.network.api.request.tracking.AddTrackingParams
import ru.parcel.app.core.network.api.request.tracking.DetectParams
import ru.parcel.app.core.network.api.request.tracking.RefreshTrackingParams
import ru.parcel.app.core.network.api.request.tracking.UpdateTracking
import ru.parcel.app.core.network.api.request.tracking.UpdateTrackingListParams
import ru.parcel.app.core.utils.DeviceUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    suspend fun register(login: String, password: String): HttpResponse {
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

    suspend fun auth(login: String, password: String): HttpResponse {
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

    suspend fun resendConfirmation(): HttpResponse {
        return api.resendConfirmation(
            BaseRequest(
                "id",
                "2.0",
                RESEND_CONFIRMATION,
                EmptyParams()
            )
        )
    }

    suspend fun remindPassword(login: String): HttpResponse {
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
    ): Any? {
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
                                return format.format(it)
                            }
                        )
                    )
                )
            )
        )
    }

    suspend fun updateTrackingList(updateTrackingListParams: UpdateTrackingListParams): HttpResponse {
        return api.updateTrackingList(
            BaseRequest(
                "id",
                "2.0",
                UPDATE_TRACKING_LIST,
                updateTrackingListParams
            )
        )
    }

    suspend fun getTrackingList(): HttpResponse {
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
    ): HttpResponse {
        return api.addTracking(
            BaseRequest(
                "id",
                "2.0",
                ADD_TRACKING,
                AddTrackingParams(track, courierSlug, name)
            )
        )
    }

    suspend fun refreshTracking(id: Long): HttpResponse {
        return api.refreshTracking(
            BaseRequest(
                "id",
                "2.0",
                REFRESH_TRACKING,
                RefreshTrackingParams(id)
            )
        )
    }

    suspend fun detect(track: String): HttpResponse {
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
    ): HttpResponse {
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