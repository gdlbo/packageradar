package ru.gdlbo.parcelradar.app.ui.components.status

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.gdlbo.parcelradar.app.R
import ru.gdlbo.parcelradar.app.core.network.model.Tracking

@Composable
fun DeliveryProgressBar(
    tracking: Tracking,
    modifier: Modifier = Modifier
) {
    val progress = calculateProgress(tracking)
    val isCancelled = isCancelled(tracking)

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.delivery_progress),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                StatusBadge(tracking)
            }

            DeliveryTimeline(
                progress = progress,
                isCancelled = isCancelled
            )
        }
    }
}

@Composable
private fun StatusBadge(tracking: Tracking) {
    val isDelivered = tracking.isDelivered == true
    val lastCheckpoint = tracking.checkpoints
        .find { it.id == tracking.lastCheckpointId }
        ?: tracking.checkpoints.lastOrNull()

    val status = lastCheckpoint?.statusCode?.lowercase().orEmpty()

    val icon = when {
        isDelivered -> Icons.Rounded.CheckCircle
        status.contains("cancel") ||
                status.contains("exception") ||
                status.contains("failed") ||
                status.contains("expired") ->
            Icons.Rounded.Error

        status.contains("out_for_delivery") ||
                status.contains("courier") ||
                status.contains("last_mile") ->
            Icons.Rounded.LocalShipping

        status.contains("import") ||
                status.contains("customs") ->
            Icons.Rounded.Gavel

        status.contains("border") ||
                status.contains("export") ->
            Icons.Rounded.Public

        status.contains("pickup") ||
                status.contains("accepted") ->
            Icons.Rounded.Inventory

        else -> Icons.Rounded.Pending
    }

    val color = when {
        isDelivered -> MaterialTheme.colorScheme.primary
        status.contains("cancel") ||
                status.contains("exception") ||
                status.contains("failed") ||
                status.contains("expired") ->
            MaterialTheme.colorScheme.error

        status.contains("out_for_delivery") ||
                status.contains("courier") ||
                status.contains("last_mile") ->
            MaterialTheme.colorScheme.tertiary

        else -> MaterialTheme.colorScheme.secondary
    }

    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = color
            )
            Text(
                text = currentStatusLabel(tracking),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun DeliveryTimeline(
    progress: Float,
    isCancelled: Boolean
) {
    val stages = listOf(
        R.string.status_started to 0f,
        R.string.status_export to 0.25f,
        R.string.status_border to 0.5f,
        R.string.status_import to 0.75f,
        R.string.status_last_mile to 1f
    )

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(
            durationMillis = 900,
            easing = FastOutSlowInEasing
        ),
        label = "deliveryProgress"
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

        LinearProgressIndicator(
            progress = animatedProgress,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = if (isCancelled)
                MaterialTheme.colorScheme.error
            else
                MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            stages.forEachIndexed { index, (labelRes, stageProgress) ->
                val active = progress >= stageProgress
                val next = stages.getOrNull(index + 1)?.second ?: 2f
                val current = active && progress < next

                DeliveryStage(
                    label = stringResource(labelRes),
                    active = active,
                    current = current,
                    cancelled = isCancelled
                )
            }
        }
    }
}


@Composable
private fun DeliveryStage(
    label: String,
    active: Boolean,
    current: Boolean,
    cancelled: Boolean
) {
    val containerColor = when {
        active && cancelled -> MaterialTheme.colorScheme.error
        active -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val contentColor = when {
        active && cancelled -> MaterialTheme.colorScheme.onError
        active -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(64.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = containerColor,
            tonalElevation = if (current) 4.dp else 0.dp,
            modifier = Modifier.size(28.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                when {
                    active && cancelled ->
                        Icon(Icons.Rounded.Close, null, tint = contentColor, modifier = Modifier.size(16.dp))

                    active && !current ->
                        Icon(Icons.Rounded.Check, null, tint = contentColor, modifier = Modifier.size(16.dp))

                    current ->
                        Box(
                            Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(contentColor)
                        )
                }
            }
        }

        Spacer(Modifier.height(6.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (active)
                MaterialTheme.colorScheme.onSurface
            else
                MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
    }
}


private fun isCancelled(tracking: Tracking): Boolean {
    val lastCheckpoint = tracking.checkpoints.find { it.id == tracking.lastCheckpointId }
        ?: tracking.checkpoints.firstOrNull()
        ?: return false
    val status = lastCheckpoint.statusCode ?: return false
    return status == "exception" || status == "failed_attempt" || status == "expired" || status == "cancelled"
}

private fun calculateProgress(tracking: Tracking): Float {
    if (tracking.isDelivered == true) return 1f
    if (tracking.checkpoints.isEmpty()) return 0f

    val lastCheckpoint = tracking.checkpoints.find { it.id == tracking.lastCheckpointId }
        ?: tracking.checkpoints.firstOrNull()
        ?: return 0f

    if (lastCheckpoint.isDelivered() || lastCheckpoint.isArrived()) return 1f

    var maxProgress = 0f
    tracking.checkpoints.forEach { checkpoint ->
        val status = checkpoint.statusCode ?: ""
        val p = when {
            status.contains("import") || status.contains("customs") -> 0.75f
            status.contains("border") -> 0.5f
            status.contains("export") -> 0.25f
            else -> 0f
        }
        if (p > maxProgress) maxProgress = p
    }

    return if (maxProgress == 0f && tracking.checkpoints.isNotEmpty()) 0.1f else maxProgress
}

@Composable
fun currentStatusLabel(tracking: Tracking): String {
    if (tracking.isDelivered == true) {
        return stringResource(R.string.delivered)
    }

    val lastCheckpoint = tracking.checkpoints
        .find { it.id == tracking.lastCheckpointId }
        ?: tracking.checkpoints.lastOrNull()
        ?: return stringResource(R.string.unknown_status)

    val status = lastCheckpoint.statusCode
        ?.lowercase()
        .orEmpty()

    return when {
        status.contains("cancel") ||
                status.contains("exception") ||
                status.contains("failed") ||
                status.contains("expired") ->
            stringResource(R.string.status_cancelled)

        status.contains("out_for_delivery") ||
                status.contains("courier") ||
                status.contains("last_mile") ->
            stringResource(R.string.status_out_for_delivery)

        status.contains("import") ||
                status.contains("customs") ->
            stringResource(R.string.status_customs)

        status.contains("border") ->
            stringResource(R.string.status_at_border)

        status.contains("export") ->
            stringResource(R.string.status_export_processing)

        status.contains("pickup") ||
                status.contains("accepted") ->
            stringResource(R.string.status_accepted)

        else ->
            stringResource(R.string.status_in_transit)
    }
}
