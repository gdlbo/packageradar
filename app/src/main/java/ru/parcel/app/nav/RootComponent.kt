package ru.parcel.app.nav

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.router.stack.replaceAll
import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.parcel.app.di.prefs.AccessTokenManager
import ru.parcel.app.nav.about.AboutComponent
import ru.parcel.app.nav.archive.ArchiveComponent
import ru.parcel.app.nav.home.HomeComponent
import ru.parcel.app.nav.login.LoginComponent
import ru.parcel.app.nav.register.RegisterComponent
import ru.parcel.app.nav.selected.SelectedElementComponent
import ru.parcel.app.nav.settings.SettingsComponent

class RootComponent(
    componentContext: ComponentContext,
    isOpenSettings: Boolean = false
) : ComponentContext by componentContext, KoinComponent {
    private val stack = StackNavigation<TopLevelConfiguration>()
    private val atm: AccessTokenManager by inject()

    val childStack = childStack(
        source = stack,
        serializer = TopLevelConfiguration.serializer(),
        initialConfiguration = if (isOpenSettings && atm.hasAccessToken()) {
            TopLevelConfiguration.SettingsScreenConfiguration
        } else if (atm.hasAccessToken()) {
            TopLevelConfiguration.HomeScreenConfiguration
        } else {
            TopLevelConfiguration.LoginScreenConfiguration
        },
        childFactory = ::createChild,
        handleBackButton = true
    )

    fun navigateTo(configuration: TopLevelConfiguration) {
        stack.push(configuration)
    }

    fun popBack() {
        stack.pop()
    }

    private fun createChild(
        config: TopLevelConfiguration,
        context: ComponentContext,
    ): TopLevelChild {
        return when (config) {
            is TopLevelConfiguration.LoginScreenConfiguration -> TopLevelChild.Login(
                LoginComponent(
                    componentContext = context,
                    navigateToHome = { stack.replaceAll(TopLevelConfiguration.HomeScreenConfiguration) },
                    navigateTo = ::navigateTo
                )
            )

            is TopLevelConfiguration.AboutScreenConfiguration -> TopLevelChild.About(
                AboutComponent(componentContext = context, popBack = ::popBack)
            )

            is TopLevelConfiguration.ArchiveScreenConfiguration -> TopLevelChild.Archive(
                ArchiveComponent(componentContext = context, navigateTo = ::navigateTo)
            )

            is TopLevelConfiguration.SelectedElementScreenConfiguration -> TopLevelChild.SelectedElement(
                SelectedElementComponent(
                    componentContext = context,
                    id = config.id,
                    popBack = ::popBack
                )
            )

            is TopLevelConfiguration.SettingsScreenConfiguration -> TopLevelChild.Settings(
                SettingsComponent(
                    componentContext = context,
                    navigateTo = ::navigateTo,
                    popBack = ::popBack,
                    navToLogin = {
                        stack.replaceAll(TopLevelConfiguration.LoginScreenConfiguration)
                    }
                )
            )

            is TopLevelConfiguration.RegisterScreenConfiguration -> TopLevelChild.Register(
                RegisterComponent(
                    contextComponent = context,
                    navigateTo = ::navigateTo,
                    popBack = ::popBack
                )
            )

            is TopLevelConfiguration.HomeScreenConfiguration -> TopLevelChild.Home(
                HomeComponent(
                    componentContext = context,
                    navigateTo = ::navigateTo
                )
            )
        }
    }

    sealed class TopLevelChild {
        data class Login(val component: LoginComponent) : TopLevelChild()
        data class Register(val component: RegisterComponent) : TopLevelChild()
        data class Home(val component: HomeComponent) : TopLevelChild()
        data class SelectedElement(val component: SelectedElementComponent) : TopLevelChild()
        data class Archive(val component: ArchiveComponent) : TopLevelChild()
        data class Settings(val component: SettingsComponent) : TopLevelChild()
        data class About(val component: AboutComponent) : TopLevelChild()
    }

    @Serializable
    sealed interface TopLevelConfiguration {
        @Serializable
        data object HomeScreenConfiguration : TopLevelConfiguration

        @Serializable
        data object LoginScreenConfiguration : TopLevelConfiguration

        @Serializable
        data object RegisterScreenConfiguration : TopLevelConfiguration

        @Serializable
        data class SelectedElementScreenConfiguration(val id: Long) : TopLevelConfiguration

        @Serializable
        data object ArchiveScreenConfiguration : TopLevelConfiguration

        @Serializable
        data object SettingsScreenConfiguration : TopLevelConfiguration

        @Serializable
        data object AboutScreenConfiguration : TopLevelConfiguration
    }
}