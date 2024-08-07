package ru.parcel.app.nav.about

import com.arkivanov.decompose.ComponentContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class AboutComponent(
    val popBack: () -> Unit,
    componentContext: ComponentContext
) : ComponentContext by componentContext {
    val viewModelScope = CoroutineScope(Dispatchers.Main.immediate)

}