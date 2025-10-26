package ru.gdlbo.parcelradar.app.nav

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import ru.gdlbo.parcelradar.app.R
import ru.gdlbo.parcelradar.app.nav.RootComponent.TopLevelChild.*
import ru.gdlbo.parcelradar.app.nav.RootComponent.TopLevelConfiguration.*
import ru.gdlbo.parcelradar.app.nav.about.AboutComponentImpl
import ru.gdlbo.parcelradar.app.nav.archive.ArchiveComponentImpl
import ru.gdlbo.parcelradar.app.nav.archive.IScrollToUpComp
import ru.gdlbo.parcelradar.app.nav.home.HomeComponentImpl
import ru.gdlbo.parcelradar.app.nav.login.LoginComponentImpl
import ru.gdlbo.parcelradar.app.nav.register.RegisterComponentImpl
import ru.gdlbo.parcelradar.app.nav.selected.SelectedElementComponentImpl
import ru.gdlbo.parcelradar.app.nav.settings.SettingsComponentImpl
import ru.gdlbo.parcelradar.app.nav.vectorres.Archive24
import ru.gdlbo.parcelradar.app.nav.vectorres.Package24

@Composable
fun RootComponentImpl(rootComponent: RootComponent) {
    val stack by rootComponent.childStack.subscribeAsState()
    val windowWidthSizeClass = calculateWindowSizeClass(LocalConfiguration.current.screenWidthDp.dp)
    val currentInstance = stack.items.last().instance

    Scaffold(
        bottomBar = {
            if (windowWidthSizeClass == WindowWidthSizeClass.Compact && currentInstance
                    .shouldShowNavigation()
            ) {
                when (currentInstance) {
                    is Home, is Archive, is Settings -> {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface
                        ) {
                            navItemList.forEach { item ->
                                val selected =
                                    stack.items.last().configuration == item.configuration
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = {
                                        if (!selected) {
                                            rootComponent.navigateTo(item.configuration)
                                        } else if (currentInstance.component is IScrollToUpComp) {
                                            (currentInstance.component as IScrollToUpComp).scrollUp()
                                        }
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
            }
        },
        contentWindowInsets = WindowInsets(top = 15.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
        ) {
            if (windowWidthSizeClass != WindowWidthSizeClass.Compact && currentInstance
                    .shouldShowNavigation()
            ) {
                NavigationRail(
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    navItemList.forEach { item ->
                        val selected = stack.items.last().configuration == item.configuration
                        NavigationRailItem(
                            selected = selected,
                            onClick = {
                                if (!selected) {
                                    rootComponent.navigateTo(item.configuration)
                                } else if (currentInstance.component is IScrollToUpComp) {
                                    (currentInstance.component as IScrollToUpComp).scrollUp()
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = null
                                )
                            },
                            label = { Text(text = stringResource(id = item.textId)) },
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
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
}

enum class WindowWidthSizeClass {
    Compact, Medium, Expanded
}

@Composable
fun calculateWindowSizeClass(width: Dp): WindowWidthSizeClass {
    return when {
        width < 600.dp -> WindowWidthSizeClass.Compact
        width < 840.dp -> WindowWidthSizeClass.Medium
        else -> WindowWidthSizeClass.Expanded
    }
}

private fun RootComponent.TopLevelChild.shouldShowNavigation(): Boolean {
    return this !is Login && this !is Register
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