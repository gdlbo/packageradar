package ru.gdlbo.parcelradar.app.nav

import androidx.compose.ui.graphics.vector.ImageVector

data class NavItem(
    val configuration: RootComponent.TopLevelConfiguration,
    val icon: ImageVector,
    val textId: Int,
)