package ru.gdlbo.parcelradar.app.nav.selected

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Create
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.gdlbo.parcelradar.app.R
import ru.gdlbo.parcelradar.app.core.network.model.Tracking
import ru.gdlbo.parcelradar.app.core.utils.TimeFormatter
import ru.gdlbo.parcelradar.app.di.theme.ThemeManager
import ru.gdlbo.parcelradar.app.nav.WindowWidthSizeClass
import ru.gdlbo.parcelradar.app.nav.calculateWindowSizeClass
import ru.gdlbo.parcelradar.app.ui.components.CopyableText
import ru.gdlbo.parcelradar.app.ui.components.CustomHorizontalDivider
import ru.gdlbo.parcelradar.app.ui.components.status.Barcode
import ru.gdlbo.parcelradar.app.ui.components.status.CourierName
import ru.gdlbo.parcelradar.app.ui.components.status.CourierRating
import ru.gdlbo.parcelradar.app.ui.components.status.ParcelCheckpointsSection
import ru.gdlbo.parcelradar.app.ui.theme.ThemeColors
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SelectedElementComponentImpl(selectedElementComponent: SelectedElementComponent) {
    val roomManager = selectedElementComponent.roomManager
    val themeManager = selectedElementComponent.themeManager
    val parcelId = selectedElementComponent.id
    val tracking by selectedElementComponent.currentTracking.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val scope = rememberCoroutineScope()
    val pullState = rememberPullToRefreshState()
    var isRefreshing by remember { mutableStateOf(false) }
    var isArchived by remember { mutableStateOf(false) }

    var needToShowArchiveBar by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    val isDarkTheme = themeManager.isDarkTheme.value ?: isSystemInDarkTheme()
    val windowSizeClass = calculateWindowSizeClass(LocalConfiguration.current.screenWidthDp.dp)

    LaunchedEffect(parcelId) {
        val loadedTracking = roomManager.getTrackingById(parcelId)
        selectedElementComponent.currentTracking.value = loadedTracking
        isArchived = tracking?.isArchived == true
        isLoading = false
    }

    val onRefresh: () -> Unit = {
        isRefreshing = true
        scope.launch {
            selectedElementComponent.updateParcelStatus(tracking)
            delay(300)
            isRefreshing = false
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    AnimatedVisibility(
                        visible = !isLoading,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Text(
                            text = if (tracking?.title.isNullOrBlank()) {
                                stringResource(id = R.string.parcel_status)
                            } else {
                                tracking?.title.toString()
                            },
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .fillMaxWidth()
                                .basicMarquee()
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { selectedElementComponent.popBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    var expanded by remember { mutableStateOf(false) }
                    IconButton(onClick = { expanded = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "More options")
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .width(200.dp)
                    ) {
                        DropdownMenuItem(
                            onClick = {
                                tracking?.let {
                                    val link = selectedElementComponent.getOpenSiteLink(it)

                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))

                                    context.startActivity(intent)
                                }
                                expanded = false
                            },
                            text = {
                                Text(text = stringResource(R.string.open_in_browser))
                            }
                        )
                        DropdownMenuItem(
                            onClick = {
                                selectedElementComponent.archiveParcel(tracking!!, !isArchived)
                                isArchived = !isArchived
                                needToShowArchiveBar = true
                                expanded = false
                            },
                            text = {
                                Text(
                                    text = if (isArchived) stringResource(R.string.unarchive_parcel) else stringResource(
                                        R.string.archive_parcel
                                    )
                                )
                            }
                        )
                        DropdownMenuItem(
                            onClick = {
                                val shareText = context.getString(
                                    R.string.copy_text,
                                    tracking?.title ?: context.getString(R.string.empty),
                                    tracking?.trackingNumberCurrent ?: tracking?.trackingNumber,
                                    tracking?.courier?.name
                                )

                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                }

                                context.startActivity(
                                    Intent.createChooser(
                                        shareIntent,
                                        "Share tracking info"
                                    )
                                )
                                expanded = false
                            },
                            text = {
                                Text(text = stringResource(R.string.share))
                            }
                        )
                    }
                }
            )
        }, contentWindowInsets = WindowInsets(0.dp)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            PullToRefreshBox(
                state = pullState,
                onRefresh = onRefresh,
                isRefreshing = isRefreshing,
            ) {
                CompositionLocalProvider(
                    LocalOverscrollConfiguration provides null
                ) {
                    if (isRefreshing) {
                        RefreshingIndicator()
                    } else {
                        TrackingContent(
                            tracking = tracking,
                            windowSizeClass = windowSizeClass,
                            isDarkTheme = isDarkTheme,
                            themeManager = themeManager,
                            selectedElementComponent = selectedElementComponent
                        )
                    }
                }
            }

            if (needToShowArchiveBar) {
                LaunchedEffect(snackbarHostState) {
                    val message = context.getString(
                        if (isArchived) R.string.package_added_to_archive else R.string.package_removed_from_archive
                    )

                    snackbarHostState.showSnackbar(message)
                    needToShowArchiveBar = false
                }
            }
        }
    }
}

@Composable
fun RefreshingIndicator() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun TrackingContent(
    tracking: Tracking?,
    windowSizeClass: WindowWidthSizeClass,
    isDarkTheme: Boolean,
    themeManager: ThemeManager,
    selectedElementComponent: SelectedElementComponent
) {
    tracking?.let { trackingData ->
        if (windowSizeClass == WindowWidthSizeClass.Expanded && trackingData.checkpoints.isNotEmpty()) {
            TrackingContentTablet(
                trackingData = trackingData,
                isDarkTheme = isDarkTheme,
                themeManager = themeManager,
                selectedElementComponent = selectedElementComponent,
                isTablet = true
            )
        } else {
            TrackingContentPhone(
                trackingData = trackingData,
                isDarkTheme = isDarkTheme,
                themeManager = themeManager,
                selectedElementComponent = selectedElementComponent,
                isTablet = false
            )
        }
    }
}

@Composable
fun TrackingContentTablet(
    trackingData: Tracking,
    isDarkTheme: Boolean,
    themeManager: ThemeManager,
    selectedElementComponent: SelectedElementComponent,
    isTablet: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1.1f)
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                item(key = "parcelInfoBlock") {
                    TrackingContentColumn(
                        trackingData = trackingData,
                        isDarkTheme = isDarkTheme,
                        selectedElementComponent = selectedElementComponent,
                        themeManager = themeManager,
                        isTablet = isTablet
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(2f)
                .padding(vertical = 8.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                item(key = "checkpointsSection") {
                    if (trackingData.checkpoints.isNotEmpty()) {
                        ParcelCheckpointsSection(
                            checkpoints = trackingData.checkpoints,
                            themeManager = themeManager,
                            isTablet = true
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun TrackingContentPhone(
    trackingData: Tracking,
    isDarkTheme: Boolean,
    themeManager: ThemeManager,
    selectedElementComponent: SelectedElementComponent,
    isTablet: Boolean
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp)
    ) {
        item {
            TrackingContentColumn(
                trackingData = trackingData,
                isDarkTheme = isDarkTheme,
                selectedElementComponent = selectedElementComponent,
                isTablet = isTablet,
                themeManager = themeManager
            )
        }
    }
}

@Composable
fun TrackingContentColumn(
    trackingData: Tracking,
    isDarkTheme: Boolean,
    themeManager: ThemeManager,
    selectedElementComponent: SelectedElementComponent,
    isTablet: Boolean
) {
    if (trackingData.isDelivered == true) {
        ParcelDeliveredStatus(trackingData, isDarkTheme)

        Spacer(Modifier.height(4.dp))
    } else if (trackingData.isReadyForPickup == true) {
        ParcelReadyForPickupStatus(trackingData, isDarkTheme)

        Spacer(Modifier.height(4.dp))
    }

    ParcelInfoSection(
        tracking = trackingData,
        isDarkTheme = isDarkTheme,
    )

    Spacer(Modifier.height(4.dp))

    ParcelActionsSection(
        tracking = trackingData,
        forceUpdateDB = selectedElementComponent::forceUpdateDB,
        deleteItem = selectedElementComponent::deleteItem,
        popBack = selectedElementComponent.popBack,
        updateItem = { tracking, title ->
            tracking?.let {
                selectedElementComponent.updateItem(
                    it,
                    title
                )
            }
        },
    )

    if (trackingData.checkpoints.isNotEmpty() && !isTablet) {
        Spacer(Modifier.height(4.dp))

        ParcelCheckpointsSection(
            checkpoints = trackingData.checkpoints,
            themeManager = themeManager,
            isTablet = isTablet
        )
    }

    Spacer(Modifier.height(4.dp))

    ParcelLastCheck(trackingData)

    Spacer(modifier = Modifier.height(24.dp))
}

@Composable
fun ParcelReadyForPickupStatus(tracking: Tracking?, isDarkTheme: Boolean) {
    val lastCheckpoint = tracking?.checkpoints?.lastOrNull() ?: return
    val date =
        TimeFormatter().formatTimeString(lastCheckpoint.time.toString(), LocalContext.current)

    val backgroundColor =
        if (isDarkTheme) ThemeColors.DarkBlue.copy(alpha = 0.6f) else ThemeColors.LightBlue.copy(
            alpha = 0.4f
        )
    val textColor = if (isDarkTheme) Color.White else Color.Black

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(id = R.string.parcel_ready_for_pickup),
                style = MaterialTheme.typography.titleMedium.copy(color = textColor),
            )
            Text(
                text = stringResource(id = R.string.ready_for_pickup_on, date),
                style = MaterialTheme.typography.bodyLarge.copy(color = textColor),
            )
        }
    }
}

@Composable
fun ParcelDeliveredStatus(tracking: Tracking?, isDarkTheme: Boolean) {
    val lastCheckpoint = tracking?.checkpoints?.lastOrNull() ?: return
    val date =
        TimeFormatter().formatTimeString(lastCheckpoint.time.toString(), LocalContext.current)

    val backgroundColor =
        if (isDarkTheme) ThemeColors.DarkGreen.copy(alpha = 0.6f) else ThemeColors.LightGreenTransparent.copy(
            alpha = 0.4f
        )
    val textColor = if (isDarkTheme) Color.White else Color.Black

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(id = R.string.parcel_delivered),
                style = MaterialTheme.typography.titleMedium.copy(color = textColor),
            )
            Text(
                text = stringResource(id = R.string.delivered_on, date),
                style = MaterialTheme.typography.bodyLarge.copy(color = textColor),
            )
        }
    }
}

@Composable
fun ParcelInfoSection(tracking: Tracking, isDarkTheme: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(
            topStart = 12.dp,
            topEnd = 12.dp,
            bottomStart = 4.dp,
            bottomEnd = 4.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ParcelName(tracking.title)

            CustomHorizontalDivider()

            TrackingNumber(tracking)

            CustomHorizontalDivider()

            CourierName(tracking.courier)

            val reviewScore = tracking.courier?.reviewScore
            if (reviewScore != null) {
                CustomHorizontalDivider()

                CourierRating(
                    courier = tracking.courier,
                    isDarkTheme = isDarkTheme
                )
            }

            Spacer(Modifier.width(4.dp))
        }
    }
}

@Composable
fun ParcelLastCheck(tracking: Tracking) {
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    val lastCheckDate = tracking.lastCheck?.let { formatter.parse(it) }
    val nextCheckDate = tracking.nextCheck?.let { formatter.parse(it) }
    val now = Calendar.getInstance().time

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(
            topStart = 4.dp,
            topEnd = 4.dp,
            bottomStart = 12.dp,
            bottomEnd = 12.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val lastCheckText = if (lastCheckDate != null) {
                val lastCheckDuration = now.time - lastCheckDate.time
                if (lastCheckDuration > TimeUnit.HOURS.toMillis(24)) {
                    formatter.format(lastCheckDate)
                } else {
                    val hoursAgo = TimeUnit.MILLISECONDS.toHours(lastCheckDuration)
                    stringResource(id = R.string.hours_ago, hoursAgo)
                }
            } else {
                stringResource(R.string.updating_information)
            }

            Column {
                Text(
                    text = stringResource(id = R.string.last_check),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = lastCheckText,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            if (nextCheckDate != null) {
                val nextCheckDuration = nextCheckDate.time - now.time
                val nextCheckText = if (nextCheckDuration > TimeUnit.HOURS.toMillis(24)) {
                    formatter.format(nextCheckDate)
                } else {
                    val hours = TimeUnit.MILLISECONDS.toHours(nextCheckDuration)
                    stringResource(id = R.string.after_hours, hours)
                }

                CustomHorizontalDivider()
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(id = R.string.next_check),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    text = nextCheckText,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
fun ParcelName(title: String?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.baseline_label_24),
            contentDescription = "Parcel Name Icon",
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = stringResource(id = R.string.parcel_name),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = if (title.isNullOrBlank()) {
                    stringResource(id = R.string.empty)
                } else {
                    title
                },
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun TrackingNumber(tracking: Tracking) {
    val currentTrackingNumber = tracking.trackingNumberCurrent ?: tracking.trackingNumber
    val trackingNumberSecondary = tracking.trackingNumberSecondary
    val originalTrackNumber = tracking.trackingNumber

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Filled.Info,
            contentDescription = "Parcel Icon",
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = stringResource(id = R.string.tracking_number),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            CopyableText(
                prefix = if (trackingNumberSecondary != null) stringResource(id = R.string.current) + " " else "",
                text = currentTrackingNumber,
            )
            if (trackingNumberSecondary != null) {
                CopyableText(
                    prefix = stringResource(id = R.string.old) + " ",
                    text = originalTrackNumber
                )
            }
        }
    }
}

@Composable
fun ParcelActionsSection(
    tracking: Tracking,
    deleteItem: (Tracking?) -> Unit,
    forceUpdateDB: () -> Unit,
    updateItem: (tracking: Tracking?, title: String) -> Unit,
    popBack: () -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showBarcodeDialog by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(4.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .padding(vertical = 8.dp)
                .padding(horizontal = 16.dp)
        ) {
            // Barcode button
            Button(
                onClick = {
                    showBarcodeDialog = true
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.outline_barcode_24),
                    contentDescription = "Barcode"
                )
            }

            // Edit button
            Button(
                onClick = {
                    showEditDialog = true
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(Icons.Outlined.Create, contentDescription = "Share")
            }

            // Remove button
            Button(
                onClick = {
                    showDeleteDialog = true
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(Icons.Outlined.Delete, contentDescription = "Remove")
            }
        }
    }

    if (showEditDialog) {
        EditTitleDialog(
            title = tracking.title ?: "",
            tracking = tracking,
            onDismissRequest = { showEditDialog = false },
            focusRequester = focusRequester,
            focusManager = focusManager,
            updateItem = updateItem,
        )
    }

    if (showDeleteDialog) {
        DeleteDialog(
            tracking,
            deleteItem = deleteItem,
            forceUpdateDB = forceUpdateDB,
            onDismissRequest = { showDeleteDialog = false },
            popBack = popBack
        )
    }

    if (showBarcodeDialog) {
        Barcode(
            tracking.trackingNumberCurrent ?: tracking.trackingNumber,
            showBarcodeDialog,
            updateShowBarcodeBottomSheet = { showBarcodeDialog = it }
        )
    }
}

@Composable
fun DeleteDialog(
    tracking: Tracking?,
    deleteItem: (Tracking?) -> Unit,
    forceUpdateDB: () -> Unit,
    onDismissRequest: () -> Unit,
    popBack: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = stringResource(R.string.are_you_sure_remove)) },
        text = { Text(text = stringResource(R.string.are_you_sure_remove_subtitle)) },
        confirmButton = {
            Button(onClick = {
                deleteItem(tracking)
                onDismissRequest()
                forceUpdateDB()
                popBack()
            }) {
                Text(stringResource(R.string.yes))
            }
        },
        dismissButton = {
            Button(
                onClick = onDismissRequest
            ) {
                Text(stringResource(R.string.no))
            }
        }
    )
}

@Composable
fun EditTitleDialog(
    title: String,
    tracking: Tracking?,
    onDismissRequest: () -> Unit,
    updateItem: (tracking: Tracking?, title: String) -> Unit,
    focusRequester: FocusRequester,
    focusManager: FocusManager
) {
    var titleState by remember { mutableStateOf(TextFieldValue(title)) }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        titleState = TextFieldValue(
            text = titleState.text,
            selection = TextRange(titleState.text.length)
        )
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = stringResource(R.string.edit_title)) },
        text = {
            TextField(
                value = titleState,
                onValueChange = { newText ->
                    titleState = TextFieldValue(
                        text = newText.text,
                        selection = TextRange(newText.text.length)
                    )
                },
                placeholder = { Text(text = stringResource(R.string.edit_title_placeholder)) },
                textStyle = TextStyle(fontSize = 18.sp),
                modifier = Modifier.focusRequester(focusRequester)
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    updateItem(tracking, titleState.text)

                    onDismissRequest()
                },
                enabled = titleState.text.isNotEmpty()
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            Button(
                onClick = {
                    focusManager.clearFocus()

                    onDismissRequest()
                }
            ) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}