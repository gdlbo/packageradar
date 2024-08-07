package ru.parcel.app.nav

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import ru.parcel.app.R
import ru.parcel.app.nav.RootComponent.TopLevelChild.About
import ru.parcel.app.nav.RootComponent.TopLevelChild.Archive
import ru.parcel.app.nav.RootComponent.TopLevelChild.Home
import ru.parcel.app.nav.RootComponent.TopLevelChild.Login
import ru.parcel.app.nav.RootComponent.TopLevelChild.Register
import ru.parcel.app.nav.RootComponent.TopLevelChild.SelectedElement
import ru.parcel.app.nav.RootComponent.TopLevelChild.Settings
import ru.parcel.app.nav.RootComponent.TopLevelConfiguration.ArchiveScreenConfiguration
import ru.parcel.app.nav.RootComponent.TopLevelConfiguration.HomeScreenConfiguration
import ru.parcel.app.nav.RootComponent.TopLevelConfiguration.SettingsScreenConfiguration
import ru.parcel.app.nav.about.AboutComponentImpl
import ru.parcel.app.nav.archive.ArchiveComponentImpl
import ru.parcel.app.nav.home.HomeComponentImpl
import ru.parcel.app.nav.login.LoginComponentImpl
import ru.parcel.app.nav.register.RegisterComponentImpl
import ru.parcel.app.nav.selected.SelectedElementComponentImpl
import ru.parcel.app.nav.settings.SettingsComponentImpl
import ru.parcel.app.nav.vectorres.Archive24
import ru.parcel.app.nav.vectorres.Package24

@OptIn(ExperimentalDecomposeApi::class)
@Composable
fun RootComponentImpl(rootComponent: RootComponent) {
    val stack by rootComponent.childStack.subscribeAsState()

    Scaffold(
        bottomBar = {
            val currentInstance = stack.items.last().instance
            when (currentInstance) {
                is Home, is Archive, is Settings -> {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        navItemList.forEach { item ->
                            val selected = stack.items.last().configuration == item.configuration
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    if (!selected) rootComponent.navigateTo(item.configuration)
                                },
                                icon = {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = null
                                    )
                                },
                                label = { Text(text = stringResource(id = item.textId)) }
                            )
                        }
                    }
                }

                else -> {}
            }
        }, contentWindowInsets = WindowInsets(top = 15.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxSize(),
        ) {
            Children(
                animation = stackAnimation(fade()),
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                stack = stack
            ) { child ->
                when (val instance = child.instance) {
                    is Home -> HomeComponentImpl(instance.component)
                    is Archive -> ArchiveComponentImpl(instance.component)
                    is Settings -> SettingsComponentImpl(instance.component)
                    is Login -> LoginComponentImpl(instance.component)
                    is Register -> RegisterComponentImpl(instance.component)
                    is SelectedElement -> SelectedElementComponentImpl(instance.component)
                    is About -> AboutComponentImpl(instance.component)
                }
            }
        }
    }
}

val navItemList = listOf(
    NavItem(
        configuration = HomeScreenConfiguration,
        icon = Package24,
        textId = R.string.parcels,
    ),
    NavItem(
        configuration = ArchiveScreenConfiguration,
        icon = Archive24,
        textId = R.string.archive,
    ),
    NavItem(
        configuration = SettingsScreenConfiguration,
        icon = Icons.Outlined.Settings,
        textId = R.string.settings,
    )
)