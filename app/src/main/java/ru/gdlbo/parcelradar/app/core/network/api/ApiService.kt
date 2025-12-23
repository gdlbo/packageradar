package ru.gdlbo.parcelradar.app.core.network.api

import ru.gdlbo.parcelradar.app.core.network.api.entity.Auth
import ru.gdlbo.parcelradar.app.core.network.api.entity.Detection
import ru.gdlbo.parcelradar.app.core.network.api.entity.TrackingList
import ru.gdlbo.parcelradar.app.core.network.api.request.BaseRequest
import ru.gdlbo.parcelradar.app.core.network.api.request.EmptyParams
import ru.gdlbo.parcelradar.app.core.network.api.request.auth.AuthParams
import ru.gdlbo.parcelradar.app.core.network.api.request.auth.RemindPasswordParams
import ru.gdlbo.parcelradar.app.core.network.api.request.settings.SettingsParams
import ru.gdlbo.parcelradar.app.core.network.api.request.tracking.AddTrackingParams
import ru.gdlbo.parcelradar.app.core.network.api.request.tracking.DetectParams
import ru.gdlbo.parcelradar.app.core.network.api.request.tracking.RefreshTrackingParams
import ru.gdlbo.parcelradar.app.core.network.api.request.tracking.UpdateTrackingListParams
import ru.gdlbo.parcelradar.app.core.network.api.response.BaseResponse
import ru.gdlbo.parcelradar.app.core.network.api.response.TrackingResponse
import ru.gdlbo.parcelradar.app.core.network.model.Tracking

interface ApiService {
    suspend fun addTracking(baseRequest: BaseRequest<AddTrackingParams>): BaseResponse<TrackingResponse>

    suspend fun authenticate(baseRequest: BaseRequest<AuthParams>): BaseResponse<Auth>

    suspend fun detect(baseRequest: BaseRequest<DetectParams>): BaseResponse<Detection>

    suspend fun getTrackingList(baseRequest: BaseRequest<EmptyParams>): BaseResponse<TrackingList>

    suspend fun refreshTracking(baseRequest: BaseRequest<RefreshTrackingParams>): BaseResponse<Tracking>

    suspend fun register(baseRequest: BaseRequest<AuthParams>): BaseResponse<Auth>

    suspend fun remindPassword(baseRequest: BaseRequest<RemindPasswordParams>): BaseResponse<Any>

    suspend fun resendConfirmation(baseRequest: BaseRequest<EmptyParams>): BaseResponse<Any>

    suspend fun setNotificationsSettings(baseRequest: BaseRequest<SettingsParams>): BaseResponse<Any>

    suspend fun updateTrackingList(baseRequest: BaseRequest<UpdateTrackingListParams>): BaseResponse<Any>

    companion object {
        const val API = "v5/jsonrpc"
    }
}