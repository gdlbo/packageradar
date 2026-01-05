package ru.gdlbo.parcelradar.app.ui.components.status

import androidx.compose.animation.*
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.gdlbo.parcelradar.app.R
import ru.gdlbo.parcelradar.app.core.network.model.Checkpoint
import ru.gdlbo.parcelradar.app.core.utils.TimeFormatter
import ru.gdlbo.parcelradar.app.di.prefs.SettingsManager
import ru.gdlbo.parcelradar.app.di.theme.ThemeManager
import ru.gdlbo.parcelradar.app.ui.theme.ThemeColors
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs

@Composable
fun ParcelCheckpointsSection(
    checkpoints: List<Checkpoint>,
    themeManager: ThemeManager,
    settingsManager: SettingsManager,
    isTablet: Boolean = false
) {
    var isExpanded by remember { mutableStateOf(false) }
    val showExpandButton = checkpoints.size > 3 && !isTablet

    val trackingStats = rememberTrackingStats(checkpoints)

    // Custom smooth easing for a premium feel without "jelly" bounce
    val smoothEasing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)
    val duration = 500

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .animateContentSize(
                    animationSpec = tween(
                        durationMillis = duration,
                        easing = smoothEasing
                    )
                ),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            TrackingHeader(
                checkpointsCount = checkpoints.size,
                trackingStats = trackingStats
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            if (checkpoints.isEmpty()) {
                EmptyCheckpointsView()
            } else {
                CheckpointTimeline(
                    checkpoints = checkpoints.reversed(),
                    isExpanded = isExpanded || !showExpandButton,
                    themeManager = themeManager,
                    settingsManager = settingsManager,
                    isTablet = isTablet,
                    animationDuration = duration,
                    easing = smoothEasing
                )

                if (showExpandButton) {
                    ExpandCollapseButton(
                        isExpanded = isExpanded,
                        hiddenCount = checkpoints.size - 3,
                        onClick = { isExpanded = !isExpanded }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyCheckpointsView() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = CircleShape,
            modifier = Modifier.size(56.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        Text(
            text = stringResource(id = R.string.no_checkpoints),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun TrackingHeader(
    checkpointsCount: Int,
    trackingStats: TrackingStats
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(id = R.string.checkpoints),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = trackingStats.statusText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = trackingStats.badgeColor.copy(alpha = 0.12f),
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Text(
                text = "$checkpointsCount",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = trackingStats.badgeColor
            )
        }
    }
}

@Composable
private fun CheckpointTimeline(
    checkpoints: List<Checkpoint>,
    isExpanded: Boolean,
    themeManager: ThemeManager,
    settingsManager: SettingsManager,
    isTablet: Boolean,
    animationDuration: Int,
    easing: androidx.compose.animation.core.Easing
) {
    val isDark = themeManager.isDarkTheme.value ?: isSystemInDarkTheme()

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (checkpoints.size <= 3 || isTablet) {
            checkpoints.forEachIndexed { index, checkpoint ->
                CheckpointTimelineItem(
                    checkpoint = checkpoint,
                    isFirst = index == 0,
                    isLast = index == checkpoints.size - 1,
                    isDark = isDark,
                    settingsManager = settingsManager
                )
            }
        } else {
            // Newest checkpoint
            CheckpointTimelineItem(
                checkpoint = checkpoints.first(),
                isFirst = true,
                isLast = false,
                isDark = isDark,
                settingsManager = settingsManager
            )

            // Middle checkpoints with synchronized size and fade transitions
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(
                    animationSpec = tween(animationDuration, easing = easing)
                ) + fadeIn(
                    animationSpec = tween(animationDuration / 2, delayMillis = animationDuration / 4)
                ),
                exit = shrinkVertically(
                    animationSpec = tween(animationDuration, easing = easing)
                ) + fadeOut(
                    animationSpec = tween(animationDuration / 2)
                )
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    checkpoints.subList(1, checkpoints.size - 2).forEach { checkpoint ->
                        CheckpointTimelineItem(
                            checkpoint = checkpoint,
                            isFirst = false,
                            isLast = false,
                            isDark = isDark,
                            settingsManager = settingsManager
                        )
                    }
                }
            }

            // Oldest two checkpoints
            val lastTwo = checkpoints.takeLast(2)
            lastTwo.forEachIndexed { index, checkpoint ->
                CheckpointTimelineItem(
                    checkpoint = checkpoint,
                    isFirst = false,
                    isLast = index == 1,
                    isDark = isDark,
                    settingsManager = settingsManager
                )
            }
        }
    }
}

@Composable
private fun CheckpointTimelineItem(
    checkpoint: Checkpoint,
    isFirst: Boolean,
    isLast: Boolean,
    isDark: Boolean,
    settingsManager: SettingsManager
) {
    val colorScheme = MaterialTheme.colorScheme
    val isDelivered = checkpoint.isDelivered() || checkpoint.isArrived()

    val statusColor = when {
        isDelivered -> if (isDark) ThemeColors.LightGreen else ThemeColors.DarkGreen
        isFirst -> colorScheme.primary
        else -> colorScheme.onSurfaceVariant
    }

    val dotSize = if (isFirst) 24.dp else 16.dp
    val innerDotSize = if (isFirst) 12.dp else 8.dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .semantics(mergeDescendants = true) {
                contentDescription = buildCheckpointDescription(checkpoint)
            }
    ) {
        Box(
            modifier = Modifier
                .width(40.dp)
                .padding(top = if (isFirst) 4.dp else 0.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!isFirst) {
                    DottedConnectingLine(
                        modifier = Modifier.height(8.dp),
                        color = statusColor
                    )
                }

                Box(
                    modifier = Modifier
                        .size(dotSize)
                        .clip(CircleShape)
                        .background(
                            if (isFirst) statusColor.copy(alpha = 0.15f)
                            else Color.Transparent
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        isDelivered -> {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = statusColor,
                                modifier = Modifier.size(dotSize)
                            )
                        }

                        isFirst -> {
                            Box(
                                modifier = Modifier
                                    .size(innerDotSize)
                                    .clip(CircleShape)
                                    .background(statusColor)
                            )
                        }

                        else -> {
                            Box(
                                modifier = Modifier
                                    .size(innerDotSize)
                                    .clip(CircleShape)
                                    .background(colorScheme.outlineVariant)
                            )
                        }
                    }
                }

                if (!isLast) {
                    DottedConnectingLine(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        color = statusColor
                    )
                }
            }
        }

        CheckpointContent(
            checkpoint = checkpoint,
            isFirst = isFirst,
            statusColor = statusColor,
            isLast = isLast,
            settingsManager = settingsManager
        )
    }
}

@Composable
fun DottedConnectingLine(
    modifier: Modifier = Modifier,
    color: Color,
    dotSize: Dp = 4.dp,
    gap: Dp = 4.dp,
    alpha: Float = 0.8f
) {
    Canvas(
        modifier = modifier
            .width(dotSize)
    ) {
        val stroke = dotSize.toPx()
        val interval = (dotSize + gap).toPx()

        val dotted = PathEffect.dashPathEffect(
            intervals = floatArrayOf(0f, interval),
            phase = 0f
        )

        drawLine(
            color = color,
            start = Offset(size.width / 2f, 0f),
            end = Offset(size.width / 2f, size.height),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
            pathEffect = dotted,
            alpha = alpha
        )
    }
}

@Composable
private fun RowScope.CheckpointContent(
    checkpoint: Checkpoint,
    isFirst: Boolean,
    statusColor: Color,
    isLast: Boolean,
    settingsManager: SettingsManager
) {
    Surface(
        modifier = Modifier
            .weight(1f),
        shape = RoundedCornerShape(16.dp),
        color = if (isFirst) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        } else {
            Color.Transparent
        }
    ) {
        Column(
            modifier = Modifier.padding(
                start = 12.dp,
                end = 12.dp,
                top = 8.dp,
                bottom = 8.dp
            )
        ) {
            Text(
                text = checkpoint.statusName ?: checkpoint.statusRaw
                ?: stringResource(id = R.string.unknown_status),
                style = if (isFirst) {
                    MaterialTheme.typography.titleMedium
                } else {
                    MaterialTheme.typography.bodyMedium
                },
                fontWeight = if (isFirst) FontWeight.SemiBold else FontWeight.Medium,
                color = if (isFirst) statusColor else MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = TimeFormatter().formatTimeString(
                        checkpoint.time,
                        LocalContext.current,
                        useLocalTime = settingsManager.isUseLocalTime
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            checkpoint.locationTranslated?.takeIf { it.isNotBlank() }?.let { location ->
                LocationRow(location = location)
            } ?: checkpoint.locationRaw?.takeIf { it.isNotBlank() }?.let { location ->
                LocationRow(location = location)
            }
        }
    }
}


@Composable
private fun LocationRow(location: String) {
    Row(
        modifier = Modifier.padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
        Text(
            text = location,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f)
        )
    }
}

@Composable
private fun ExpandCollapseButton(
    isExpanded: Boolean,
    hiddenCount: Int,
    onClick: () -> Unit
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Icon(
            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = if (isExpanded) {
                stringResource(R.string.show_less)
            } else {
                stringResource(R.string.show_all) + " (+$hiddenCount)"
            },
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private data class TrackingStats(
    val statusText: String,
    val badgeColor: Color,
    val durationDays: Long?
)

private fun computeDurationDays(checkpoints: List<Checkpoint>): Long? {
    val firstCheckpoint = checkpoints.firstOrNull() ?: return null
    val lastCheckpoint = checkpoints.lastOrNull() ?: return null

    val dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    dateFormatter.timeZone = TimeZone.getTimeZone("UTC")

    val firstTime = dateFormatter.parse(firstCheckpoint.time) ?: return null
    val lastTime = dateFormatter.parse(lastCheckpoint.time) ?: return null

    return TimeUnit.MILLISECONDS.toDays(abs(lastTime.time - firstTime.time))
}

@Composable
private fun rememberTrackingStats(checkpoints: List<Checkpoint>): TrackingStats {
    val durationDays = remember(checkpoints) { computeDurationDays(checkpoints) }

    val lastCheckpoint = checkpoints.firstOrNull()
    val statusText = when {
        lastCheckpoint?.isDelivered() == true && durationDays != null ->
            stringResource(R.string.delivered_after_days, durationDays)

        durationDays != null ->
            stringResource(R.string.tracking_duration_days, durationDays)

        else ->
            stringResource(R.string.tracking_duration_na)
    }

    val badgeColor = if (lastCheckpoint?.isDelivered() == true) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.secondary
    }

    return TrackingStats(
        statusText = statusText,
        badgeColor = badgeColor,
        durationDays = durationDays
    )
}

private fun buildCheckpointDescription(checkpoint: Checkpoint): String {
    return buildString {
        append(checkpoint.statusName ?: checkpoint.statusRaw ?: "Unknown status")
        append(", ")
        append(checkpoint.time)
        checkpoint.locationTranslated?.let { append(", at $it") }
            ?: checkpoint.locationRaw?.let { append(", at $it") }
    }
}