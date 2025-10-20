package ru.gdlbo.parcelradar.app.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ru.gdlbo.parcelradar.app.R
import ru.gdlbo.parcelradar.app.core.network.model.Tracking
import ru.gdlbo.parcelradar.app.core.utils.TimeFormatter
import ru.gdlbo.parcelradar.app.di.prefs.SettingsManager
import ru.gdlbo.parcelradar.app.nav.WindowWidthSizeClass
import ru.gdlbo.parcelradar.app.ui.theme.ThemeColors
import ru.gdlbo.parcelradar.app.ui.theme.darker
import kotlin.math.roundToInt

@Composable
fun FeedCard(
    isDark: Boolean,
    tracking: Tracking,
    onSwipe: () -> Unit,
    onClick: () -> Unit,
    windowSizeClass: WindowWidthSizeClass
) {
    val isUnread = tracking.isUnread == true
    val context = LocalContext.current
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val offsetX = remember { Animatable(0f) }
    val swipeThreshold = 200f
    val scrollState = rememberNestedScrollInteropConnection()

    val coroutineScope = rememberCoroutineScope()

    val settingsManager = SettingsManager()

    val cardModifier = when (windowSizeClass) {
        WindowWidthSizeClass.Compact -> Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .padding(bottom = 4.dp)

        WindowWidthSizeClass.Medium -> Modifier
            .fillMaxWidth(0.5f)
            .padding(start = 4.dp, end = 2.dp, bottom = 4.dp)

        WindowWidthSizeClass.Expanded -> Modifier
            .fillMaxWidth(0.33f)
            .padding(start = 4.dp, end = 2.dp, bottom = 4.dp)
    }

    Box(
        modifier = cardModifier
            .alpha(if (isUnread) 1f else 0.6f)
            .nestedScroll(scrollState)
            .offset { IntOffset(offsetX.value.roundToInt(), 0) }
            .then(
                if (settingsManager.isGestureSwipeEnabled && !tracking.isNew) {
                    Modifier.pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                coroutineScope.launch {
                                    if (offsetX.value < -swipeThreshold || offsetX.value > swipeThreshold) {
                                        onSwipe()
                                        offsetX.animateTo(
                                            targetValue = 0f,
                                            animationSpec = tween(durationMillis = 300)
                                        )
                                    } else {
                                        offsetX.animateTo(
                                            targetValue = 0f,
                                            animationSpec = tween(durationMillis = 300)
                                        )
                                    }
                                }
                            }
                        ) { change, dragAmount ->
                            change.consume()
                            coroutineScope.launch {
                                offsetX.snapTo(offsetX.value + dragAmount)
                            }
                        }
                    }
                } else {
                    Modifier
                }
            )
    ) {
        Card(
            modifier = Modifier
                .clickable(enabled = !tracking.isNew) { onClick() }
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (tracking.title.isNullOrBlank()) {
                            stringResource(id = R.string.empty)
                        } else {
                            tracking.title
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .basicMarquee()
                    )
                    Text(
                        text = if (tracking.lastCheckpointTime != null) {
                            TimeFormatter().formatTimeString(
                                tracking.lastCheckpointTime,
                                context
                            )
                        } else {
                            stringResource(R.string.no_time_avalible)
                        },
                        style = TextStyle(
                            fontSize = MaterialTheme.typography.titleSmall.fontSize
                        ),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                if (tracking.isNew) {
                    Box(
                        modifier = Modifier
                            .height(16.dp)
                            .width(120.dp)
                            .shimmerEffect()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .height(16.dp)
                            .width(100.dp)
                            .shimmerEffect()
                    )
                } else {
                    Text(
                        text = tracking.checkpoints.lastOrNull()?.statusName
                            ?: stringResource(id = R.string.unknown_status),
                        style = TextStyle(
                            fontSize = MaterialTheme.typography.titleSmall.fontSize,
                            fontWeight = FontWeight.SemiBold
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = when {
                            tracking.checkpoints.lastOrNull()
                                ?.isDelivered() == true || tracking.checkpoints.lastOrNull()
                                ?.isArrived() == true -> if (isDark) ThemeColors.LightGreen.copy(
                                alpha = 0.75f
                            ) else Color.Green.copy(alpha = 0.75f).darker()

                            else -> LocalContentColor.current
                        }
                    )
                    tracking.courier?.let {
                        Text(
                            text = it.name,
                            style = TextStyle(
                                fontSize = MaterialTheme.typography.titleSmall.fontSize,
                                fontWeight = FontWeight.Normal
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Text(
                    text = tracking.trackingNumber,
                    style = TextStyle(
                        fontSize = MaterialTheme.typography.titleSmall.fontSize,
                        fontWeight = FontWeight.W300,
                        fontFamily = FontFamily.Monospace
                    ),
                    modifier = Modifier.clickable {
                        val clip = ClipData.newPlainText(
                            "Tracking Number",
                            tracking.trackingNumberCurrent ?: tracking.trackingNumber
                        )
                        clipboardManager.setPrimaryClip(clip)
                        Toast.makeText(
                            context,
                            context.getString(R.string.copy_to_clipboard),
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}