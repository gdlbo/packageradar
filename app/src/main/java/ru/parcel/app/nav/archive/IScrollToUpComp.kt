package ru.parcel.app.nav.archive

import kotlinx.coroutines.flow.MutableStateFlow

interface IScrollToUpComp {
    val isScrollable: MutableStateFlow<Boolean>

    fun scrollUp()
}