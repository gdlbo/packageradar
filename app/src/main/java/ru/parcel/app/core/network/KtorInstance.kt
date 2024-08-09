package ru.parcel.app.core.network

import android.os.Build
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.plugins.compression.ContentEncoding
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.ANDROID
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import org.koin.dsl.module
import ru.parcel.app.core.utils.DeviceUtils
import ru.parcel.app.di.prefs.AccessTokenManager
import java.util.Locale

object KtorInstance {
    val ktorModule = module {
        single {
            createHttpClient(get())
        }
        single {
            ApiHandler(get())
        }
    }
    private const val APP_VERSION = "94"
    private val userAgent = "GdePosylka/$APP_VERSION (${DeviceUtils.getDeviceName()}; Android ${Build.VERSION.SDK_INT})"

    private fun createHttpClient(atm: AccessTokenManager): HttpClient {
        var language = Locale.getDefault().language

        if (language != "ru") {
            language = "en" // server support only russian and english languages
        }

        return HttpClient(OkHttp) {
            engine {
                config {
                    addInterceptor(AuthInterceptor(atm))
                }
            }

            install(ContentEncoding) {
                gzip()
            }

            install(Logging) {
                logger = Logger.ANDROID
                level = LogLevel.BODY
            }

            install(HttpCache)

            install(ContentNegotiation) {
                json(Json {
                    isLenient = true
                    prettyPrint = true
                    ignoreUnknownKeys = true
                })
            }

            defaultRequest {
                url("https://api-gp.com/api/")
                header("Content-Type", "application/json")
                header("X-App-Version", APP_VERSION)
                header("X-OS-Version", Build.VERSION.SDK_INT)
                header("X-App-Locale", language)
                header("User-Agent", userAgent)
            }
        }
    }
}

suspend fun <T> retryRequest(
    times: Int = 3,
    initialDelay: Long = 100, // milliseconds
    maxDelay: Long = 1000,
    factor: Double = 2.0,
    request: suspend () -> T
): T {
    var currentDelay = initialDelay
    repeat(times - 1) {
        try {
            return request()
        } catch (e: Exception) {
            delay(currentDelay)
            currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
            e.fillInStackTrace()
        }
    }
    return request()
}