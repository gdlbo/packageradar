package ru.parcel.app.ui.components.status

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.parcel.app.R
import ru.parcel.app.core.network.model.Checkpoint
import ru.parcel.app.core.utils.TimeFormatter
import ru.parcel.app.di.theme.ThemeManager
import ru.parcel.app.ui.components.noRippleClickable
import ru.parcel.app.ui.theme.ThemeColors
import ru.parcel.app.ui.theme.darker
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun ParcelCheckpointsSection(
    checkpoints: List<Checkpoint>,
    themeManager: ThemeManager,
    isTablet: Boolean = false
) {
    val lineColor = MaterialTheme.colorScheme.onSurface.copy()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(if (isTablet) 12.dp else 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(id = R.string.checkpoints),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.width(16.dp))

            CheckpointDottedColumn(
                checkpointList = checkpoints.reversed(),
                dotColor = lineColor,
                isDark = themeManager.isDarkTheme.value ?: isSystemInDarkTheme(),
                isTablet = isTablet
            )
        }
    }
}

@Composable
fun CheckpointDottedColumn(
    checkpointList: List<Checkpoint>, dotColor: Color, isDark: Boolean, isTablet: Boolean
) {
    val isExpanded = remember { mutableStateOf(false) }
    val visibleList = if (isExpanded.value || checkpointList.size < 4 || isTablet) {
        checkpointList
    } else {
        listOf(checkpointList.first()) + checkpointList.takeLast(2)
    }

    val firstCheckpoint = checkpointList.lastOrNull()
    val lastCheckpoint = checkpointList.firstOrNull()

    val dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    val timeDifferenceText = if (firstCheckpoint != null && lastCheckpoint != null) {
        val firstTime = dateFormatter.parse(firstCheckpoint.time)
        val lastTime = dateFormatter.parse(lastCheckpoint.time)
        if (firstTime != null && lastTime != null) {
            val diffInMillis = lastTime.time - firstTime.time
            val days = TimeUnit.MILLISECONDS.toDays(diffInMillis)

            if (lastCheckpoint.isDelivered()) {
                stringResource(R.string.delivered_after_days, days)
            } else {
                stringResource(R.string.tracking_duration_days, days)
            }
        } else {
            stringResource(R.string.tracking_duration_na)
        }
    } else {
        stringResource(R.string.tracking_duration_na)
    }

    Column {
        visibleList.forEachIndexed { index, item ->
            CheckpointRow(
                changeExpandedState = {
                    if (checkpointList.size >= 4) {
                        isExpanded.value = !isExpanded.value
                    }
                },
                checkpointItem = item,
                color = dotColor,
                isLast = index == visibleList.size - 1,
                isFirst = index == 0,
                isDark = isDark,
                isTablet = isTablet,
                isExpander = index == 1 && !isExpanded.value && checkpointList.size >= 4 && !isTablet,
                timeDifferenceText = if (index == 1 && !isExpanded.value && checkpointList.size >= 4 && !isTablet) timeDifferenceText else null
            )
        }
    }
}

@Composable
fun CheckpointRow(
    changeExpandedState: () -> Unit,
    checkpointItem: Checkpoint,
    color: Color,
    isLast: Boolean = false,
    isFirst: Boolean = false,
    isDark: Boolean,
    isTablet: Boolean,
    isExpander: Boolean = false,
    timeDifferenceText: String? = null
) {
    val colorScheme = MaterialTheme.colorScheme
    Row(modifier = Modifier.height(IntrinsicSize.Max)) {
        Box(
            modifier = Modifier
                .weight(if (isTablet) 0.8f else 1f)
                .fillMaxHeight()
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .noRippleClickable(changeExpandedState)
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height

                val offsetX = 8.dp.toPx()

                if (!isFirst) {
                    drawLine(
                        color = color,
                        start = Offset(x = canvasWidth / 2 - offsetX, y = 0f),
                        end = Offset(x = canvasWidth / 2 - offsetX, y = canvasHeight / 2),
                        strokeWidth = 2.0f,
                        alpha = 0.5f,
                        pathEffect = if (isExpander) {
                            PathEffect.dashPathEffect(
                                intervals = floatArrayOf(
                                    10f,
                                    10f,
                                ),
                            )
                        } else null
                    )
                }

                if (!isLast) {
                    drawLine(
                        color = color,
                        start = Offset(x = canvasWidth / 2 - offsetX, y = canvasHeight / 2),
                        end = Offset(x = canvasWidth / 2 - offsetX, y = canvasHeight),
                        strokeWidth = 2.0f,
                        alpha = 0.5f,
                        pathEffect = if (isExpander) {
                            PathEffect.dashPathEffect(
                                intervals = floatArrayOf(
                                    10f,
                                    10f
                                ),
                            )
                        } else null
                    )
                }

                if (!isExpander) {
                    if (isLast) {
                        drawCircle(
                            color = color,
                            center = Offset(x = canvasWidth / 2 - offsetX, y = canvasHeight / 2),
                            radius = (if (!isTablet) 8.dp else 12.dp).toPx()
                        )
                        drawCircle(
                            color = colorScheme.surface,
                            center = Offset(x = canvasWidth / 2 - offsetX, y = canvasHeight / 2),
                            radius = (if (!isTablet) 7.dp else 10.5.dp).toPx()
                        )
                        drawCircle(
                            color = color,
                            center = Offset(x = canvasWidth / 2 - offsetX, y = canvasHeight / 2),
                            radius = (if (!isTablet) 5.dp else 7.5.dp).toPx()
                        )
                    } else {
                        drawCircle(
                            color = color,
                            center = Offset(x = canvasWidth / 2 - offsetX, y = canvasHeight / 2),
                            radius = (if (!isTablet) 8.dp else 12.dp).toPx()
                        )
                    }
                }
            }
        }
        if (isExpander) {
            ExpanderItem(
                modifier = Modifier.weight(4f),
                timeDifferenceText = timeDifferenceText
            )
        } else {
            CheckpointItem(
                checkpoint = checkpointItem,
                modifier = Modifier.weight(4f),
                isFirst = isFirst,
                isDark = isDark
            )
        }
    }
}

@Composable
fun ExpanderItem(timeDifferenceText: String?, modifier: Modifier) {
    val textColor = MaterialTheme.colorScheme.primary
    Column(
        modifier = modifier,
    ) {
        Spacer(
            modifier = Modifier
                .height(12.dp)
        )

        Text(
            text = timeDifferenceText ?: "",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(vertical = 2.dp)
        )

        Text(
            text = stringResource(R.string.show_all),
            color = textColor,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .padding(vertical = 2.dp)
                .padding(bottom = 8.dp)
        )

        Spacer(
            modifier = Modifier
                .height(8.dp)
        )
    }
}

@Composable
fun CheckpointItem(
    checkpoint: Checkpoint, modifier: Modifier = Modifier, isFirst: Boolean, isDark: Boolean
) {
    Column(modifier = modifier) {
        Spacer(Modifier.height(4.dp))

        Text(
            text = checkpoint.statusName ?: checkpoint.statusRaw
            ?: stringResource(id = R.string.unknown_status),
            style = if (isFirst) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.W500,
            color = when {
                checkpoint.isDelivered() || checkpoint.isArrived() -> if (isDark) ThemeColors.LightGreen.copy(
                    alpha = 0.75f
                ) else Color.Green.copy(alpha = 0.75f).darker()

                else -> LocalContentColor.current
            }
        )

        Text(
            text = TimeFormatter().formatTimeString(
                checkpoint.time,
                LocalContext.current
            ), style = MaterialTheme.typography.bodySmall
        )

        Text(
            text = checkpoint.locationTranslated ?: checkpoint.locationRaw
            ?: stringResource(id = R.string.unknown_location),
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(Modifier.height(4.dp))
    }
}