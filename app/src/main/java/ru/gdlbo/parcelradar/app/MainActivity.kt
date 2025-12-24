package ru.gdlbo.parcelradar.app

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.arkivanov.decompose.DecomposeExperimentFlags
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.retainedComponent
import org.koin.android.ext.android.inject
import ru.gdlbo.parcelradar.app.di.theme.ThemeManager
import ru.gdlbo.parcelradar.app.nav.RootComponent
import ru.gdlbo.parcelradar.app.nav.RootComponentImpl
import ru.gdlbo.parcelradar.app.ui.theme.TrackerTheme

class MainActivity : ComponentActivity() {
    private val themeManager: ThemeManager by inject()
    private lateinit var rootComponent: RootComponent

    @OptIn(ExperimentalDecomposeApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT)
        )

        super.onCreate(savedInstanceState)

        DecomposeExperimentFlags.duplicateConfigurationsEnabled = true

        rootComponent = retainedComponent { RootComponent(it) }

        handleIntent(this.intent)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            TrackerTheme(
                themeManager = themeManager
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RootComponentImpl(rootComponent)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        handleIntent(intent)
        super.onNewIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        val isLogged = rootComponent.isLogged()
        val action = intent.action
        val data: Uri? = intent.data

        if (isLogged) {
            when (action) {
                Intent.ACTION_APPLICATION_PREFERENCES -> {
                    rootComponent.navigateTo(RootComponent.TopLevelConfiguration.SettingsScreenConfiguration)
                }

                Intent.ACTION_VIEW -> {
                    if (data != null) {
                        val trackingNumber = extractTrackingNumber(data)
                        if (trackingNumber != null) {
                            rootComponent.navigateTo(
                                RootComponent.TopLevelConfiguration.HomeScreenConfigurationWithTracking(
                                    trackingNumber
                                )
                            )
                        }
                    }
                }

                else -> {
                    val parcelId = intent.getLongExtra("parcelId", -1)
                    if (parcelId != -1L) {
                        rootComponent.navigateTo(
                            RootComponent.TopLevelConfiguration.SelectedElementScreenConfiguration(
                                parcelId
                            )
                        )
                    }
                }
            }
        }
    }

    private fun extractTrackingNumber(uri: Uri): String? {
        val host = uri.host
        val path = uri.path

        return when {
            (host == "www.pochta.ru" || host == "pochta.ru") && uri.fragment != null -> {
                uri.fragment
            }

            (host == "gdeposylka.ru" || host == "www.gdeposylka.ru") && path != null -> {
                path.trimEnd('/').substringAfterLast("/")
            }

            (host == "parcelsapp.com" || host == "www.parcelsapp.com") && path != null -> {
                path.trimEnd('/').substringAfterLast("/")
            }

            (host == "packageradar.com" || host == "www.packageradar.com") && path != null -> {
                path.trimEnd('/').substringAfterLast("/")
            }

            (host == "www.cdek.ru" || host == "cdek.ru") && uri.getQueryParameter("order_id") != null -> {
                uri.getQueryParameter("order_id")
            }

            (host == "rocket.ozon.ru" || host == "www.rocket.ozon.ru") && uri.getQueryParameter("SearchId") != null -> {
                uri.getQueryParameter("SearchId")
            }

            (host == "track24.net" || host == "www.track24.net" || host == "track24.ru" || host == "www.track24.ru") && uri.getQueryParameter(
                "code"
            ) != null -> {
                uri.getQueryParameter("code")
            }

            (host == "belpost.by" || host == "www.belpost.by") && uri.getQueryParameter("number") != null -> {
                uri.getQueryParameter("number")
            }

            else -> null
        }
    }
}