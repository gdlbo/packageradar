package ru.gdlbo.parcelradar.app.core.network

import okhttp3.Interceptor
import okhttp3.Interceptor.Chain
import okhttp3.Response
import ru.gdlbo.parcelradar.app.di.prefs.AccessTokenManager

class AuthInterceptor(private val atm: AccessTokenManager) : Interceptor {
    override fun intercept(chain: Chain): Response {
        val origRequest = chain.request()
        return if (!atm.hasAccessToken()) {
            chain.proceed(origRequest)
        } else {
            val newRequest = origRequest.newBuilder()
                .header("X-Authorization-Token", atm.getAccessToken()!!)
                .build()
            chain.proceed(newRequest)
        }
    }
}