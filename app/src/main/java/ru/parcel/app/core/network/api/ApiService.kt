package ru.parcel.app.core.network.api

import io.ktor.client.statement.HttpResponse
import ru.parcel.app.core.network.api.request.BaseRequest
import ru.parcel.app.core.network.api.request.EmptyParams
import ru.parcel.app.core.network.api.request.auth.AuthParams
import ru.parcel.app.core.network.api.request.auth.RemindPasswordParams
import ru.parcel.app.core.network.api.request.settings.SettingsParams
import ru.parcel.app.core.network.api.request.tracking.AddTrackingParams
import ru.parcel.app.core.network.api.request.tracking.DetectParams
import ru.parcel.app.core.network.api.request.tracking.RefreshTrackingParams
import ru.parcel.app.core.network.api.request.tracking.UpdateTrackingListParams

interface ApiService {
    suspend fun addTracking(baseRequest: BaseRequest<AddTrackingParams>): HttpResponse

    suspend fun authenticate(baseRequest: BaseRequest<AuthParams>): HttpResponse

    suspend fun detect(baseRequest: BaseRequest<DetectParams>): HttpResponse

    suspend fun getTrackingList(baseRequest: BaseRequest<EmptyParams>): HttpResponse

    suspend fun refreshTracking(baseRequest: BaseRequest<RefreshTrackingParams>): HttpResponse

    suspend fun register(baseRequest: BaseRequest<AuthParams>): HttpResponse

    suspend fun remindPassword(baseRequest: BaseRequest<RemindPasswordParams>): HttpResponse

    suspend fun resendConfirmation(baseRequest: BaseRequest<EmptyParams>): HttpResponse

    suspend fun setNotificationsSettings(baseRequest: BaseRequest<SettingsParams>): HttpResponse

    suspend fun updateTrackingList(baseRequest: BaseRequest<UpdateTrackingListParams>): HttpResponse

    companion object {
        const val API = "v5/jsonrpc"
    }
}