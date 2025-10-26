package ru.gdlbo.parcelradar.app.nav.login

import com.arkivanov.decompose.ComponentContext
import io.ktor.client.call.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.gdlbo.parcelradar.app.core.network.ApiHandler
import ru.gdlbo.parcelradar.app.core.network.api.entity.Auth
import ru.gdlbo.parcelradar.app.core.network.api.response.BaseResponse
import ru.gdlbo.parcelradar.app.core.network.retryRequest
import ru.gdlbo.parcelradar.app.di.prefs.AccessTokenManager
import ru.gdlbo.parcelradar.app.nav.RootComponent

class LoginComponent(
    val navigateTo: (RootComponent.TopLevelConfiguration) -> Unit,
    val navigateToHome: () -> Unit,
    componentContext: ComponentContext
) : ComponentContext by componentContext, KoinComponent {
    private val viewModelScope = CoroutineScope(Dispatchers.Main.immediate)
    val apiService: ApiHandler by inject()
    val atm: AccessTokenManager by inject()

    sealed class LoginState {
        data object Idle : LoginState()
        data object Loading : LoginState()
        data object Success : LoginState()
        data class Error(val message: Any) : LoginState()
        data object RemindPasswordSuccess : LoginState()
        data class RemindPasswordError(val message: Any) : LoginState()
    }

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState = _loginState.asStateFlow()

    fun resetState() {
        _loginState.value = LoginState.Idle
    }

    fun remindPassword(email: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading

            val trimmedEmail = email.trim()

            val response: HttpResponse = retryRequest {
                apiService.remindPassword(trimmedEmail)
            }

            if (!response.status.isSuccess()) {
                _loginState.value = LoginState.RemindPasswordError(response.status.description)
                return@launch
            }

            val authBody = response.body<BaseResponse<Auth>>()

            if (authBody.error != null) {
                _loginState.value = LoginState.Error(authBody.error?.message ?: "error")
                return@launch
            }

            _loginState.value = LoginState.RemindPasswordSuccess
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading

            val trimmedEmail = email.trim()
            val trimmedPassword = password.trim()

            val response: HttpResponse = retryRequest {
                apiService.auth(trimmedEmail, trimmedPassword)
            }

            if (!response.status.isSuccess()) {
                _loginState.value = LoginState.Error(response.status.description)
                return@launch
            }

            val authBody = response.body<BaseResponse<Auth>>()

            if (authBody.error != null) {
                _loginState.value = LoginState.Error(authBody.error?.message ?: "error")
                return@launch
            }

            authBody.result?.accessToken?.let { atm.saveAccessToken(it) }
            _loginState.value = LoginState.Success
        }
    }
}