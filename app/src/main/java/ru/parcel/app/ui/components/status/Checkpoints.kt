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
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.parcel.app.R
import ru.parcel.app.core.network.model.Checkpoint
import ru.parcel.app.di.theme.ThemeManager
import ru.parcel.app.ui.components.noRippleClickable
import ru.parcel.app.ui.theme.ThemeColors
import ru.parcel.app.ui.theme.darker
import kotlin.collections.first
import kotlin.collections.forEachIndexed
import kotlin.collections.plus
import kotlin.collections.takeLast

@Composable
fun ParcelCheckpointsSection(checkpoints: List<Checkpoint>, themeManager: ThemeManager) {
    val lineColor = MaterialTheme.colorScheme.onSurface.copy()

    Card(
        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(4.dp)
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
                checkpointList = checkpoints,
                dotColor = lineColor,
                isDark = themeManager.isDarkTheme.value ?: isSystemInDarkTheme()
            )
        }
    }
}

@Composable
fun CheckpointDottedColumn(
    checkpointList: List<Checkpoint>, dotColor: Color, isDark: Boolean
) {
    val isExpanded = remember { mutableStateOf(false) }
    val visibleList = if (isExpanded.value || checkpointList.size < 4) {
        checkpointList
    } else {
        (listOf(checkpointList.first()) + checkpointList.takeLast(2))
    }

    val textColor = MaterialTheme.colorScheme.primary
    Column {
        visibleList.forEachIndexed { index, item ->
            if (index == 1 && !isExpanded.value && checkpointList.size >= 4) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Spacer(
                        modifier = Modifier
                            .size(1.dp)
                            .weight(1f)
                    )
                    Text(text = stringResource(R.string.show_all),
                        color = textColor,
                        modifier = Modifier
                            .padding(vertical = 16.dp)
                            .weight(4f)
                            .noRippleClickable {
                                isExpanded.value = true
                            })
                }
            }
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
                isDark = isDark
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
    isDark: Boolean
) {
    Row(modifier = Modifier.height(IntrinsicSize.Max)) {
        Box(
            modifier = Modifier
                .weight(1f)
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

                drawCircle(
                    color = color,
                    center = Offset(x = canvasWidth / 2 - offsetX, y = canvasHeight / 2),
                    radius = 4.dp.toPx()
                )

                if (!isFirst) {
                    drawLine(
                        color = color,
                        start = Offset(x = canvasWidth / 2 - offsetX, y = 0f),
                        end = Offset(x = canvasWidth / 2 - offsetX, y = canvasHeight / 2),
                        strokeWidth = 2.0f
                    )
                }

                if (!isLast) {
                    drawLine(
                        color = color,
                        start = Offset(x = canvasWidth / 2 - offsetX, y = canvasHeight / 2),
                        end = Offset(x = canvasWidth / 2 - offsetX, y = canvasHeight),
                        strokeWidth = 2.0f
                    )
                }
            }
        }
        CheckpointItem(
            checkpoint = checkpointItem,
            modifier = Modifier.weight(4f),
            isLast = isLast,
            isDark = isDark
        )
    }
}

@Composable
fun CheckpointItem(
    checkpoint: Checkpoint, modifier: Modifier = Modifier, isLast: Boolean, isDark: Boolean
) {
    Column(modifier = modifier) {
        Text(
            text = checkpoint.statusName ?: checkpoint.statusRaw
            ?: stringResource(id = R.string.unknown_status),
            style = if (isLast) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.W500,
            color = when {
                checkpoint.isDelivered() || checkpoint.isArrived() -> if (isDark) ThemeColors.LightGreen.copy(
                    alpha = 0.75f
                ) else Color.Green.copy(alpha = 0.75f).darker()

                else -> LocalContentColor.current
            }
        )
        Text(
            text = checkpoint.time, style = MaterialTheme.typography.bodySmall
        )
        Text(
            text = checkpoint.locationTranslated ?: checkpoint.locationRaw
            ?: stringResource(id = R.string.unknown_location),
            style = MaterialTheme.typography.bodySmall
        )
    }
}