package ru.gdlbo.parcelradar.app.nav

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector

data class NavItem(
    val configuration: RootComponent.TopLevelConfiguration,
    val icon: ImageVector,
    @StringRes val textId: Int,
)