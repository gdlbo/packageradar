package ru.gdlbo.parcelradar.app.ui.components.status

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
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
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = stringResource(R.string.delivery_progress),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            DeliveryTimeline(progress = progress, isCancelled = isCancelled)
        }
    }
}

@Composable
private fun DeliveryTimeline(progress: Float, isCancelled: Boolean) {
    val stages = listOf(
        R.string.status_started to 0f,
        R.string.status_export to 0.25f,
        R.string.status_border to 0.5f,
        R.string.status_import to 0.75f,
        R.string.status_last_mile to 1f
    )

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val itemWidth = 60.dp
        val dotSize = 24.dp
        val trackHeight = 4.dp
        val sidePadding = itemWidth / 2

        val trackWidth = maxWidth - itemWidth

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = (dotSize - trackHeight) / 2)
                .padding(horizontal = sidePadding)
                .fillMaxWidth()
                .height(trackHeight)
                .clip(RoundedCornerShape(trackHeight / 2))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )

        val animatedProgress by animateFloatAsState(
            targetValue = progress,
            animationSpec = tween(durationMillis = 1000),
            label = "progress"
        )

        if (animatedProgress > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = (dotSize - trackHeight) / 2)
                    .padding(start = sidePadding)
                    .width(trackWidth * animatedProgress)
                    .height(trackHeight)
                    .clip(RoundedCornerShape(trackHeight / 2))
                    .background(
                        if (isCancelled) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.primary
                    )
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            stages.forEachIndexed { index, (labelRes, stageProgress) ->
                val isActive = progress >= stageProgress
                val nextStageProgress = if (index < stages.lastIndex) stages[index + 1].second else 2f
                val isLastReached = isActive && progress < nextStageProgress

                val isCompleted = if (index == stages.lastIndex) {
                    isActive && !isCancelled
                } else {
                    isActive && !isLastReached
                }
                val isFailed = isLastReached && isCancelled

                DeliveryStageItem(
                    label = stringResource(labelRes),
                    isActive = isActive,
                    isCompleted = isCompleted,
                    isFailed = isFailed,
                    isCancelled = isCancelled,
                    width = itemWidth,
                    dotSize = dotSize
                )
            }
        }
    }
}

@Composable
private fun DeliveryStageItem(
    label: String,
    isActive: Boolean,
    isCompleted: Boolean,
    isFailed: Boolean,
    isCancelled: Boolean,
    width: Dp,
    dotSize: Dp
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.width(width)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(dotSize)
                .clip(CircleShape)
                .background(
                    when {
                        isActive && isCancelled -> MaterialTheme.colorScheme.error
                        isActive -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                )
        ) {
            if (isFailed) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onError,
                    modifier = Modifier.size(14.dp)
                )
            } else if (isCompleted) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(14.dp)
                )
            } else if (isActive) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onPrimary)
                )
            }
        }

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isActive) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 2,
            modifier = Modifier.fillMaxWidth()
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
