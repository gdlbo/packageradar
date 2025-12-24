package ru.gdlbo.parcelradar.app.nav.selected

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.gdlbo.parcelradar.app.R
import ru.gdlbo.parcelradar.app.nav.calculateWindowSizeClass
import ru.gdlbo.parcelradar.app.nav.selected.components.RefreshingIndicator
import ru.gdlbo.parcelradar.app.nav.selected.components.TrackingContent
import ru.gdlbo.parcelradar.app.nav.vectorres.Archive24

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SelectedElementComponentImpl(selectedElementComponent: SelectedElementComponent) {
    val roomManager = selectedElementComponent.roomManager
    val themeManager = selectedElementComponent.themeManager
    val parcelId = selectedElementComponent.id

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }
    var isArchived by remember { mutableStateOf(false) }
    var showArchiveSnackbar by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }

    val tracking by selectedElementComponent.currentTracking.collectAsState()

    val isDarkTheme = themeManager.isDarkTheme.value ?: isSystemInDarkTheme()
    val windowSizeClass = calculateWindowSizeClass(LocalConfiguration.current.screenWidthDp.dp)

    LaunchedEffect(parcelId) {
        isLoading = true
        val loaded = roomManager.getTrackingById(parcelId)
        selectedElementComponent.currentTracking.value = loaded
        isArchived = loaded?.isArchived == true
        isLoading = false
    }

    val onRefresh: () -> Unit = {
        isRefreshing = true
        scope.launch {
            selectedElementComponent.updateParcelStatus(tracking)
            delay(300)
            isRefreshing = false
            snackbarHostState.showSnackbar(context.getString(R.string.info_will_be_updated))
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
                            text = tracking?.title.takeUnless { it.isNullOrBlank() }
                                ?: stringResource(id = R.string.parcel_status),
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
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "More options")
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.width(200.dp)
                    ) {
                        DropdownMenuItem(
                            onClick = {
                                tracking?.let {
                                    val link = selectedElementComponent.getOpenSiteLink(it)
                                    context.startActivity(Intent(Intent.ACTION_VIEW, link.toUri()))
                                }
                            },
                            text = { Text(text = stringResource(R.string.open_in_browser)) },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.baseline_open_in_browser_24),
                                    contentDescription = null
                                )
                            }
                        )

                        DropdownMenuItem(
                            onClick = {
                                tracking?.let {
                                    selectedElementComponent.archiveParcel(it, !isArchived)
                                }
                            },
                            text = {
                                Text(
                                    text = if (isArchived) stringResource(R.string.unarchive_parcel)
                                    else stringResource(R.string.archive_parcel)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Archive24,
                                    contentDescription = null
                                )
                            }
                        )

                        DropdownMenuItem(
                            onClick = {
                                val shareText = context.getString(
                                    R.string.copy_text,
                                    tracking?.title ?: context.getString(R.string.empty),
                                    tracking?.trackingNumberCurrent ?: tracking?.trackingNumber,
                                    tracking?.courier?.name ?: ""
                                )
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                }
                                context.startActivity(
                                    Intent.createChooser(
                                        shareIntent,
                                        context.getString(R.string.share)
                                    )
                                )
                            },
                            text = { Text(text = stringResource(R.string.share)) },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.baseline_share_24),
                                    contentDescription = null
                                )
                            }
                        )
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets(0.dp)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            val pullState = rememberPullToRefreshState()
            PullToRefreshBox(
                state = pullState,
                onRefresh = onRefresh,
                isRefreshing = isRefreshing
            ) {
                CompositionLocalProvider(LocalOverscrollFactory provides null) {
                    if (isRefreshing) {
                        RefreshingIndicator()
                    } else {
                        key(tracking?.id) {
                            TrackingContent(
                                tracking = tracking,
                                windowSizeClass = windowSizeClass,
                                isDarkTheme = isDarkTheme,
                                themeManager = themeManager,
                                selectedElementComponent = selectedElementComponent,
                                onRefresh
                            )
                        }
                    }
                }
            }

            if (showArchiveSnackbar) {
                LaunchedEffect(snackbarHostState, isArchived) {
                    val messageRes =
                        if (isArchived) R.string.package_added_to_archive else R.string.package_removed_from_archive
                    snackbarHostState.showSnackbar(context.getString(messageRes))
                    showArchiveSnackbar = false
                }
            }
        }
    }
}