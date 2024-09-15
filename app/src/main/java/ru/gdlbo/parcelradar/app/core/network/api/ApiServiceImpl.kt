package ru.gdlbo.parcelradar.app.core.network.api

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import ru.gdlbo.parcelradar.app.core.network.api.ApiService.Companion.API
import ru.gdlbo.parcelradar.app.core.network.api.request.BaseRequest
import ru.gdlbo.parcelradar.app.core.network.api.request.EmptyParams
import ru.gdlbo.parcelradar.app.core.network.api.request.auth.AuthParams
import ru.gdlbo.parcelradar.app.core.network.api.request.auth.RemindPasswordParams
import ru.gdlbo.parcelradar.app.core.network.api.request.settings.SettingsParams
import ru.gdlbo.parcelradar.app.core.network.api.request.tracking.AddTrackingParams
import ru.gdlbo.parcelradar.app.core.network.api.request.tracking.DetectParams
import ru.gdlbo.parcelradar.app.core.network.api.request.tracking.RefreshTrackingParams
import ru.gdlbo.parcelradar.app.core.network.api.request.tracking.UpdateTrackingListParams

class ApiServiceImpl(private val client: HttpClient) : ApiService {
    override suspend fun addTracking(baseRequest: BaseRequest<AddTrackingParams>): HttpResponse {
        return client.post(API) {
            setBody(baseRequest)
        }
    }

    override suspend fun authenticate(baseRequest: BaseRequest<AuthParams>): HttpResponse {
        return client.post(API) {
            setBody(baseRequest)
        }
    }

    override suspend fun detect(baseRequest: BaseRequest<DetectParams>): HttpResponse {
        return client.post(API) {
            setBody(baseRequest)
        }
    }

    override suspend fun getTrackingList(baseRequest: BaseRequest<EmptyParams>): HttpResponse {
        return client.post(API) {
            setBody(baseRequest)
        }
    }

    override suspend fun refreshTracking(baseRequest: BaseRequest<RefreshTrackingParams>): HttpResponse {
        return client.post(API) {
            setBody(baseRequest)
        }
    }

    override suspend fun register(baseRequest: BaseRequest<AuthParams>): HttpResponse {
        return client.post(API) {
            setBody(baseRequest)
        }
    }

    override suspend fun remindPassword(baseRequest: BaseRequest<RemindPasswordParams>): HttpResponse {
        return client.post(API) {
            setBody(baseRequest)
        }
    }

    override suspend fun resendConfirmation(baseRequest: BaseRequest<EmptyParams>): HttpResponse {
        return client.post(API) {
            setBody(baseRequest)
        }
    }

    override suspend fun setNotificationsSettings(baseRequest: BaseRequest<SettingsParams>): HttpResponse {
        return client.post(API) {
            setBody(baseRequest)
        }
    }

    override suspend fun updateTrackingList(baseRequest: BaseRequest<UpdateTrackingListParams>): HttpResponse {
        return client.post(API) {
            setBody(baseRequest)
        }
    }
}