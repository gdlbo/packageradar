package ru.gdlbo.parcelradar.app.nav.selected.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalPostOffice
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.gdlbo.parcelradar.app.R
import ru.gdlbo.parcelradar.app.core.network.model.Tracking
import ru.gdlbo.parcelradar.app.core.utils.TimeFormatter
import ru.gdlbo.parcelradar.app.ui.theme.ThemeColors

@Composable
fun ParcelReadyForPickupStatus(tracking: Tracking?, isDarkTheme: Boolean) {
    val lastCheckpoint = tracking?.checkpoints?.lastOrNull() ?: return
    val date =
        TimeFormatter().formatTimeString(lastCheckpoint.time, LocalContext.current)

    val backgroundColor =
        if (isDarkTheme) ThemeColors.DarkBlue.copy(alpha = 0.2f) else ThemeColors.LightBlue.copy(
            alpha = 0.15f
        )
    val contentColor = if (isDarkTheme) ThemeColors.LightBlue else ThemeColors.DarkBlue

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                color = contentColor.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Filled.LocalPostOffice,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = stringResource(id = R.string.parcel_ready_for_pickup),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(id = R.string.ready_for_pickup_on, date),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun ParcelDeliveredStatus(tracking: Tracking?, isDarkTheme: Boolean) {
    val lastCheckpoint = tracking?.checkpoints?.lastOrNull() ?: return
    val date =
        TimeFormatter().formatTimeString(lastCheckpoint.time, LocalContext.current)

    val backgroundColor =
        if (isDarkTheme) ThemeColors.DarkGreen.copy(alpha = 0.2f) else ThemeColors.LightGreenTransparent.copy(
            alpha = 0.15f
        )
    val contentColor = if (isDarkTheme) ThemeColors.LightGreen else ThemeColors.DarkGreen

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                color = contentColor.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = stringResource(id = R.string.parcel_delivered),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(id = R.string.delivered_on, date),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}