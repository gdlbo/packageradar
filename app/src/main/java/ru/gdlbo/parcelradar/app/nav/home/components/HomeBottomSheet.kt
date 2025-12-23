package ru.gdlbo.parcelradar.app.nav.home.components

import androidx.compose.runtime.Composable
import ru.gdlbo.parcelradar.app.nav.home.HomeComponent
import ru.gdlbo.parcelradar.app.ui.components.TrackingBottomSheet

@Composable
fun HomeBottomSheet(
    showDialog: Boolean,
    onShowDialogChange: (Boolean) -> Unit,
    homeComponent: HomeComponent,
    initialTrackingNumber: String? = null
) {
    TrackingBottomSheet(
        showBottomSheet = showDialog,
        onBSStateChange = onShowDialogChange,
        addTracking = homeComponent::addTracking,
        initialTrackingNumber = initialTrackingNumber
    )
}
