package ru.gdlbo.parcelradar.app.core.network.api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import ru.gdlbo.parcelradar.app.core.network.api.ApiService.Companion.API
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

class ApiServiceImpl(private val client: HttpClient) : ApiService {
    override suspend fun addTracking(baseRequest: BaseRequest<AddTrackingParams>): BaseResponse<TrackingResponse> {
        return client.post(API) {
            setBody(baseRequest)
        }.body()
    }

    override suspend fun authenticate(baseRequest: BaseRequest<AuthParams>): BaseResponse<Auth> {
        return client.post(API) {
            setBody(baseRequest)
        }.body()
    }

    override suspend fun detect(baseRequest: BaseRequest<DetectParams>): BaseResponse<Detection> {
        return client.post(API) {
            setBody(baseRequest)
        }.body()
    }

    override suspend fun getTrackingList(baseRequest: BaseRequest<EmptyParams>): BaseResponse<TrackingList> {
        return client.post(API) {
            setBody(baseRequest)
        }.body()
    }

    override suspend fun refreshTracking(baseRequest: BaseRequest<RefreshTrackingParams>): BaseResponse<Tracking> {
        return client.post(API) {
            setBody(baseRequest)
        }.body()
    }

    override suspend fun register(baseRequest: BaseRequest<AuthParams>): BaseResponse<Auth> {
        return client.post(API) {
            setBody(baseRequest)
        }.body()
    }

    override suspend fun remindPassword(baseRequest: BaseRequest<RemindPasswordParams>): BaseResponse<Any> {
        return client.post(API) {
            setBody(baseRequest)
        }.body()
    }

    override suspend fun resendConfirmation(baseRequest: BaseRequest<EmptyParams>): BaseResponse<Any> {
        return client.post(API) {
            setBody(baseRequest)
        }.body()
    }

    override suspend fun setNotificationsSettings(baseRequest: BaseRequest<SettingsParams>): BaseResponse<Any> {
        return client.post(API) {
            setBody(baseRequest)
        }.body()
    }

    override suspend fun updateTrackingList(baseRequest: BaseRequest<UpdateTrackingListParams>): BaseResponse<Any> {
        return client.post(API) {
            setBody(baseRequest)
        }.body()
    }
}