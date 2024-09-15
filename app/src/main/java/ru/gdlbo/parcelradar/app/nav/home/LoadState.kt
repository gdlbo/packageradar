package ru.gdlbo.parcelradar.app.nav.home

sealed class LoadState {
    data object Loading : LoadState()
    data object Success : LoadState()
    data class Error(val message: Any) : LoadState()
}