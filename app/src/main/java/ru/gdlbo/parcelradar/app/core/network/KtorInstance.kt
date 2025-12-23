package ru.gdlbo.parcelradar.app.core.network

import android.os.Build
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.cache.*
import io.ktor.client.plugins.compression.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import org.koin.dsl.module
import ru.gdlbo.parcelradar.app.BuildConfig
import ru.gdlbo.parcelradar.app.core.utils.DeviceUtils
import ru.gdlbo.parcelradar.app.di.prefs.AccessTokenManager
import java.util.*

data class KtorConfig(
    val baseUrl: String = "https://api-gp.com/api/",
    val appVersion: String = "94",
    val timeoutMillis: Long = 15_000L,
    val enableLogging: Boolean = BuildConfig.DEBUG,
    val logLevel: LogLevel = LogLevel.BODY,
    val retryTimes: Int = 3,
    val retryInitialDelay: Long = 100,
    val retryMaxDelay: Long = 1000,
    val retryFactor: Double = 2.0
)

private const val HEADER_APP_VERSION = "X-App-Version"
private const val HEADER_OS_VERSION = "X-OS-Version"
private const val HEADER_APP_LOCALE = "X-App-Locale"

class KtorClientProvider(
    private val config: KtorConfig = KtorConfig(),
    private val accessTokenManager: AccessTokenManager
) {
    fun createClient(): HttpClient = HttpClient(OkHttp) {
        engine {
            config {
                addInterceptor(AuthInterceptor(accessTokenManager))
            }
        }

        install(HttpTimeout) {
            requestTimeoutMillis = config.timeoutMillis
            connectTimeoutMillis = config.timeoutMillis
            socketTimeoutMillis = config.timeoutMillis
        }

        install(ContentEncoding) {
            gzip()
        }

        if (config.enableLogging) {
            install(Logging) {
                logger = Logger.ANDROID
                level = config.logLevel
            }
        }

        install(HttpCache)

        install(ContentNegotiation) {
            json(Json {
                isLenient = true
                prettyPrint = true
                ignoreUnknownKeys = true
                coerceInputValues = true
            })
        }

        defaultRequest {
            url(config.baseUrl)
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            header(HEADER_APP_VERSION, config.appVersion)
            header(HEADER_OS_VERSION, Build.VERSION.SDK_INT)
            header(HEADER_APP_LOCALE, getAppLanguage())
            header(HttpHeaders.UserAgent, getUserAgent())
        }
    }

    private fun getAppLanguage(): String = when (Locale.getDefault().language) {
        "ru" -> "ru"
        else -> "en"
    }

    private fun getUserAgent(): String =
        "GdePosylka/${config.appVersion} (${DeviceUtils.getDeviceName()}; Android ${Build.VERSION.SDK_INT})"
}

val ktorModule = module {
    single { KtorConfig() }
    single { KtorClientProvider(get(), get()).createClient() }
    single { ApiHandler(get()) }
}

suspend fun <T> retryRequest(
    times: Int = 3,
    initialDelay: Long = 100,
    maxDelay: Long = 1000,
    factor: Double = 2.0,
    predicate: (Throwable) -> Boolean = { true },
    request: suspend () -> T
): T {
    var currentDelay = initialDelay
    var lastException: Exception? = null

    repeat(times) { attempt ->
        try {
            return request()
        } catch (e: Exception) {
            lastException = e

            if (!predicate(e)) {
                throw e
            }

            android.util.Log.w("Retry", "Attempt ${attempt + 1} failed: ${e.message}")

            if (attempt < times - 1) {
                delay(currentDelay)
                currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
            }
        }
    }

    throw lastException ?: IllegalStateException("All $times retry attempts failed")
}
