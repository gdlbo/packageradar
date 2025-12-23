package ru.gdlbo.parcelradar.app.nav.selected.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.gdlbo.parcelradar.app.core.network.model.Tracking
import ru.gdlbo.parcelradar.app.di.theme.ThemeManager
import ru.gdlbo.parcelradar.app.nav.WindowWidthSizeClass
import ru.gdlbo.parcelradar.app.nav.selected.SelectedElementComponent
import ru.gdlbo.parcelradar.app.ui.components.status.ParcelCheckpointsSection

@Composable
fun RefreshingIndicator() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun TrackingContent(
    tracking: Tracking?,
    windowSizeClass: WindowWidthSizeClass,
    isDarkTheme: Boolean,
    themeManager: ThemeManager,
    selectedElementComponent: SelectedElementComponent
) {
    tracking?.let { trackingData ->
        if (windowSizeClass == WindowWidthSizeClass.Expanded && trackingData.checkpoints.isNotEmpty()) {
            TrackingContentTablet(
                trackingData = trackingData,
                isDarkTheme = isDarkTheme,
                themeManager = themeManager,
                selectedElementComponent = selectedElementComponent,
                isTablet = true
            )
        } else {
            TrackingContentPhone(
                trackingData = trackingData,
                isDarkTheme = isDarkTheme,
                themeManager = themeManager,
                selectedElementComponent = selectedElementComponent,
                isTablet = false
            )
        }
    }
}

@Composable
fun TrackingContentTablet(
    trackingData: Tracking,
    isDarkTheme: Boolean,
    themeManager: ThemeManager,
    selectedElementComponent: SelectedElementComponent,
    isTablet: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1.1f)
                .padding(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item(key = "parcelInfoBlock") {
                    TrackingContentColumn(
                        trackingData = trackingData,
                        isDarkTheme = isDarkTheme,
                        selectedElementComponent = selectedElementComponent,
                        themeManager = themeManager,
                        isTablet = isTablet
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(2f)
                .padding(vertical = 12.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item(key = "checkpointsSection") {
                    if (trackingData.checkpoints.isNotEmpty()) {
                        ParcelCheckpointsSection(
                            checkpoints = trackingData.checkpoints,
                            themeManager = themeManager,
                            isTablet = true
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun TrackingContentPhone(
    trackingData: Tracking,
    isDarkTheme: Boolean,
    themeManager: ThemeManager,
    selectedElementComponent: SelectedElementComponent,
    isTablet: Boolean
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            TrackingContentColumn(
                trackingData = trackingData,
                isDarkTheme = isDarkTheme,
                selectedElementComponent = selectedElementComponent,
                isTablet = isTablet,
                themeManager = themeManager
            )
        }
    }
}

@Composable
fun TrackingContentColumn(
    trackingData: Tracking,
    isDarkTheme: Boolean,
    themeManager: ThemeManager,
    selectedElementComponent: SelectedElementComponent,
    isTablet: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (trackingData.isDelivered == true) {
            ParcelDeliveredStatus(trackingData, isDarkTheme)
        } else if (trackingData.isReadyForPickup == true) {
            ParcelReadyForPickupStatus(trackingData, isDarkTheme)
        }

        ParcelInfoSection(
            tracking = trackingData,
            isDarkTheme = isDarkTheme,
        )

        ParcelActionsSection(
            tracking = trackingData,
            forceUpdateDB = selectedElementComponent::forceUpdateDB,
            deleteItem = selectedElementComponent::deleteItem,
            popBack = selectedElementComponent.popBack,
            updateItem = { tracking, title ->
                tracking?.let {
                    selectedElementComponent.updateItem(
                        it,
                        title
                    )
                }
            },
        )

        if (trackingData.checkpoints.isNotEmpty() && !isTablet) {
            ParcelCheckpointsSection(
                checkpoints = trackingData.checkpoints,
                themeManager = themeManager,
                isTablet = false
            )
        }

        ParcelLastCheck(trackingData)

        Spacer(modifier = Modifier.height(16.dp))
    }
}