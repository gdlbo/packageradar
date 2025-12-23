package ru.gdlbo.parcelradar.app.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import ru.gdlbo.parcelradar.app.R
import ru.gdlbo.parcelradar.app.core.network.model.Tracking
import ru.gdlbo.parcelradar.app.core.utils.TimeFormatter
import ru.gdlbo.parcelradar.app.di.prefs.SettingsManager
import ru.gdlbo.parcelradar.app.nav.WindowWidthSizeClass
import java.util.*

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FeedCard(
    isDark: Boolean,
    tracking: Tracking,
    onSwipe: () -> Unit,
    onClick: () -> Unit,
    windowSizeClass: WindowWidthSizeClass,
    modifier: Modifier = Modifier
) {
    val isUnread = tracking.isUnread == true
    val context = LocalContext.current
    val settingsManager = SettingsManager()

    val cardModifier = when (windowSizeClass) {
        WindowWidthSizeClass.Compact -> Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)

        WindowWidthSizeClass.Medium -> Modifier
            .fillMaxWidth(0.5f)
            .padding(horizontal = 8.dp, vertical = 6.dp)

        WindowWidthSizeClass.Expanded -> Modifier
            .fillMaxWidth(0.33f)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    }

    val finalModifier = modifier.then(cardModifier)

    if (settingsManager.isGestureSwipeEnabled && !tracking.isNew) {
        val dismissState = rememberSwipeToDismissBoxState(
            confirmValueChange = { dismissValue ->
                when (dismissValue) {
                    SwipeToDismissBoxValue.EndToStart -> {
                        onSwipe()
                        true
                    }

                    else -> false
                }
            }
        )

        SwipeToDismissBox(
            state = dismissState,
            modifier = finalModifier,
            enableDismissFromStartToEnd = false,
            backgroundContent = {
                val backgroundColor by animateColorAsState(
                    when (dismissState.dismissDirection) {
                        SwipeToDismissBoxValue.EndToStart -> if (tracking.isArchived == true) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
                        else -> Color.Transparent
                    }
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(MaterialTheme.shapes.large)
                        .background(backgroundColor)
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Icon(
                            imageVector = if (tracking.isArchived == true) Icons.Filled.Restore else Icons.Filled.Delete,
                            contentDescription = stringResource(id = if (tracking.isArchived == true) R.string.restore else R.string.delete),
                            tint = if (tracking.isArchived == true) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                }
            },
            content = {
                TrackingCardContent(
                    tracking = tracking,
                    isUnread = isUnread,
                    onClick = onClick,
                    context = context,
                    isDark = isDark
                )
            }
        )
    } else {
        Box(modifier = finalModifier) {
            TrackingCardContent(
                tracking = tracking,
                isUnread = isUnread,
                onClick = onClick,
                context = context,
                isDark = isDark
            )
        }
    }
}

@Composable
private fun TrackingCardContent(
    tracking: Tracking,
    isUnread: Boolean,
    onClick: () -> Unit,
    context: Context,
    isDark: Boolean
) {
    val isDelivered = tracking.checkpoints.lastOrNull()?.isDelivered() == true
    val isArrived = tracking.checkpoints.lastOrNull()?.isArrived() == true

    val deliveredContainerColor = if (isDark) Color(0xFF1E4620) else Color(0xFFE8F5E9)
    val deliveredContentColor = if (isDark) Color(0xFFA5D6A7) else Color(0xFF1B5E20)

    Card(
        onClick = { if (!tracking.isNew) onClick() },
        modifier = Modifier.fillMaxWidth(),
        enabled = !tracking.isNew,
        colors = CardDefaults.cardColors(
            containerColor = when {
                isDelivered || isArrived -> deliveredContainerColor
                isUnread -> MaterialTheme.colorScheme.surfaceContainerHigh
                else -> MaterialTheme.colorScheme.surfaceContainer
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isUnread) 3.dp else 1.dp,
            pressedElevation = 2.dp
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HeaderSection(
                tracking = tracking,
                isDelivered = isDelivered,
                isArrived = isArrived,
                context = context,
                statusColor = deliveredContentColor
            )

            if (tracking.isNew) {
                ShimmerLoadingContent()
            } else {
                MainContentSection(
                    tracking = tracking,
                    isDelivered = isDelivered,
                    isArrived = isArrived,
                    statusColor = deliveredContentColor
                )
            }

            TrackingNumberSection(
                tracking = tracking,
                context = context
            )
        }
    }
}

@Composable
private fun CourierRow(
    courierName: String,
    courierSlug: String
) {
    val currentLanguage = Locale.getDefault().language
    val baseUrl =
        if (currentLanguage == "ru") "https://gdeposylka.ru" else "https://packageradar.com"
    val imageUrl = "$baseUrl/img/courier/${courierSlug}.svg"

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(id = R.string.courier),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = courierName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            CourierLogo(
                imageUrl = imageUrl,
                contentDescription = courierName,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun CourierLogo(imageUrl: String, contentDescription: String?, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    SubcomposeAsyncImage(
        model = ImageRequest.Builder(context)
            .data(imageUrl)
            .decoderFactory(SvgDecoder.Factory())
            .listener(
                onError = { _, result -> result.throwable.printStackTrace() }
            )
            .build(),
        contentDescription = contentDescription,
        loading = { CircularProgressIndicator(modifier = Modifier.size(14.dp)) },
        error = { },
        modifier = modifier
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HeaderSection(
    tracking: Tracking,
    isDelivered: Boolean,
    isArrived: Boolean,
    context: Context,
    statusColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isDelivered || isArrived) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = stringResource(id = R.string.delivered),
                        tint = statusColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Text(
                    text = if (tracking.title.isNullOrBlank()) {
                        stringResource(id = R.string.empty)
                    } else {
                        tracking.title
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.basicMarquee()
                )
            }
        }

        if (tracking.lastCheckpointTime != null) {
            AssistChip(
                onClick = { },
                label = {
                    Text(
                        text = TimeFormatter().formatTimeString(
                            tracking.lastCheckpointTime,
                            context
                        ),
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                border = null,
                modifier = Modifier.height(28.dp)
            )
        }
    }
}

@Composable
private fun MainContentSection(
    tracking: Tracking,
    isDelivered: Boolean,
    isArrived: Boolean,
    statusColor: Color
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.LocalShipping,
                contentDescription = null,
                tint = when {
                    isDelivered || isArrived -> statusColor
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.size(20.dp)
            )

            Text(
                text = tracking.checkpoints.lastOrNull()?.statusName
                    ?: stringResource(id = R.string.unknown_status),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isDelivered || isArrived) FontWeight.Medium else FontWeight.Normal,
                color = when {
                    isDelivered || isArrived -> statusColor
                    else -> MaterialTheme.colorScheme.onSurface
                },
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }

        tracking.courier?.let {
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            CourierRow(
                courierName = it.name,
                courierSlug = it.slug
            )
        }
    }
}

@Composable
private fun TrackingNumberSection(
    tracking: Tracking,
    context: Context
) {
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f)
        ),
        border = CardDefaults.outlinedCardBorder(enabled = false)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.tracking_number),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = tracking.trackingNumber,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            FilledTonalIconButton(
                onClick = {
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
                modifier = Modifier
                    .size(36.dp)
                    .semantics {
                        contentDescription = "Copy tracking number"
                    },
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.ContentCopy,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun ShimmerLoadingContent() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .shimmerEffect()
            )
            Box(
                modifier = Modifier
                    .height(20.dp)
                    .fillMaxWidth(0.8f)
                    .shimmerEffect()
            )
        }
    }
}
