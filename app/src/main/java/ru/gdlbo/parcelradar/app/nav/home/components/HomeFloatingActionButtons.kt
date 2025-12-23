package ru.gdlbo.parcelradar.app.nav.home.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.gdlbo.parcelradar.app.R
import ru.gdlbo.parcelradar.app.nav.home.HomeComponent

@Composable
fun HomeFloatingActionButtons(
    showUnreadButton: Boolean,
    homeComponent: HomeComponent,
    onShowDialogChange: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomEnd
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedVisibility(
                visible = showUnreadButton,
                enter = slideInVertically(
                    initialOffsetY = { fullHeight -> fullHeight }
                ),
                exit = slideOutVertically(
                    targetOffsetY = { fullHeight -> fullHeight }
                )
            ) {
                FloatingActionButton(
                    onClick = {
                        homeComponent.readAllParcels()
                    },
                    modifier = Modifier.size(45.dp),
                    containerColor = MaterialTheme.colorScheme.onSecondary
                ) {
                    Icon(
                        painterResource(R.drawable.baseline_done_all_24),
                        contentDescription = "Read all parcels"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            FloatingActionButton(
                onClick = {
                    onShowDialogChange(true)
                },
                modifier = Modifier
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = stringResource(id = R.string.add_parcel)
                )
            }
        }
    }
}
