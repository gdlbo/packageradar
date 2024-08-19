package ru.parcel.app

import android.content.Intent
import android.graphics.Color
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
import ru.parcel.app.di.theme.ThemeManager
import ru.parcel.app.nav.RootComponent
import ru.parcel.app.nav.RootComponentImpl
import ru.parcel.app.ui.theme.TrackerTheme

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

        if (isLogged) {
            when (action) {
                Intent.ACTION_APPLICATION_PREFERENCES -> {
                    rootComponent.navigateTo(RootComponent.TopLevelConfiguration.SettingsScreenConfiguration)
                }
                else -> {
                    val parcelId = intent.getLongExtra("parcelId", -1)
                    if (parcelId != -1L) {
                        rootComponent.navigateTo(RootComponent.TopLevelConfiguration.SelectedElementScreenConfiguration(parcelId))
                    }
                }
            }
        }
    }
}