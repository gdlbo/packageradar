package ru.parcel.app.nav

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.DelicateDecomposeApi
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
    componentContext: ComponentContext
) : ComponentContext by componentContext, KoinComponent {
    private val stack = StackNavigation<TopLevelConfiguration>()
    private val atm: AccessTokenManager by inject()

    val childStack = childStack(
        source = stack,
        serializer = TopLevelConfiguration.serializer(),
        initialConfiguration = if (isLogged()) {
            TopLevelConfiguration.HomeScreenConfiguration
        } else {
            TopLevelConfiguration.LoginScreenConfiguration
        },
        childFactory = ::createChild,
        handleBackButton = true
    )

    @OptIn(DelicateDecomposeApi::class)
    fun navigateTo(configuration: TopLevelConfiguration) {
        stack.push(configuration)
    }

    fun isLogged() : Boolean {
        return atm.hasAccessToken()
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

    sealed class TopLevelChild(open val component: ComponentContext) {
        data class Login(override val component: LoginComponent) : TopLevelChild(component)
        data class Register(override val component: RegisterComponent) : TopLevelChild(component)
        data class Home(override val component: HomeComponent) : TopLevelChild(component)
        data class SelectedElement(override val component: SelectedElementComponent) : TopLevelChild(component)
        data class Archive(override val component: ArchiveComponent) : TopLevelChild(component)
        data class Settings(override val component: SettingsComponent) : TopLevelChild(component)
        data class About(override val component: AboutComponent) : TopLevelChild(component)
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