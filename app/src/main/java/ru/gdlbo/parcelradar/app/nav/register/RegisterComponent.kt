package ru.gdlbo.parcelradar.app.nav.register

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

class RegisterComponent(
    val popBack: () -> Unit,
    val navigateTo: (RootComponent.TopLevelConfiguration) -> Unit,
    contextComponent: ComponentContext
) : ComponentContext by contextComponent, KoinComponent {
    private val viewModelScope = CoroutineScope(Dispatchers.Main.immediate)
    val apiService: ApiHandler by inject()
    val atm: AccessTokenManager by inject()

    sealed class RegisterState {
        data object Idle : RegisterState()
        data object Loading : RegisterState()
        data object Success : RegisterState()
        data class Error(val message: Any) : RegisterState()
    }

    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val registerState = _registerState.asStateFlow()

    fun register(email: String, password: String) {
        viewModelScope.launch {
            _registerState.value = RegisterState.Loading

            val trimmedEmail = email.trim()
            val trimmedPassword = password.trim()

            val response: HttpResponse = retryRequest {
                apiService.register(trimmedEmail, trimmedPassword)
            }

            if (!response.status.isSuccess()) {
                _registerState.value = RegisterState.Error(response.status.description)
                return@launch
            }

            val registerBody = response.body<BaseResponse<Auth>>()

            if (registerBody.error != null) {
                _registerState.value = RegisterState.Error(registerBody.error?.message ?: "error")
                return@launch
            }

            registerBody.result?.accessToken?.let { atm.saveAccessToken(it) }
            _registerState.value = RegisterState.Success
        }
    }
}