package ru.gdlbo.parcelradar.app.nav.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import kotlinx.coroutines.launch
import ru.gdlbo.parcelradar.app.R
import ru.gdlbo.parcelradar.app.nav.RootComponent
import ru.gdlbo.parcelradar.app.nav.WindowWidthSizeClass
import ru.gdlbo.parcelradar.app.nav.calculateWindowSizeClass
import ru.gdlbo.parcelradar.app.ui.components.CheckAndDisableBatteryOptimizationDialog
import ru.gdlbo.parcelradar.app.ui.components.CheckAndEnablePushNotificationsDialog
import ru.gdlbo.parcelradar.app.ui.components.FeedCard
import ru.gdlbo.parcelradar.app.ui.components.ShimmerFeedCard
import ru.gdlbo.parcelradar.app.ui.components.TrackingBottomSheet
import ru.gdlbo.parcelradar.app.ui.components.noRippleClickable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeComponentImpl(homeComponent: HomeComponent) {
    val tappedState by homeComponent.isScrollable.collectAsState()
    val trackingItems by homeComponent.trackingItemList.collectAsState()
    val state by homeComponent.loadState.collectAsState()
    val themeManager = homeComponent.themeManager
    val windowSizeClass = calculateWindowSizeClass(LocalConfiguration.current.screenWidthDp.dp)
    val listGridState = rememberLazyGridState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }
    val refreshState = rememberPullToRefreshState()
    var showDialog by remember { mutableStateOf(false) }
    var isSearchBarVisible by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var searchBarSize by remember { mutableStateOf(Size.Zero) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val isDarkTheme = themeManager.isDarkTheme.value ?: isSystemInDarkTheme()
    val showUnreadButton = trackingItems.any { parcel ->
        parcel.isUnread == true
    }

    LaunchedEffect(tappedState) {
        listState.animateScrollToItem(0)
        listGridState.animateScrollToItem(0)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    AnimatedVisibility(
                        visible = isSearchBarVisible,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        TextField(
                            value = searchQuery,
                            onValueChange = {
                                searchQuery = it
                                homeComponent.search(it, isArchive = false)
                            },
                            placeholder = {
                                Text(
                                    text = stringResource(id = R.string.search),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            textStyle = MaterialTheme.typography.bodyLarge,
                            singleLine = true,
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.Search,
                                    contentDescription = stringResource(id = R.string.search)
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = {
                                        searchQuery = ""
                                        homeComponent.search("", isArchive = false)
                                    }) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = stringResource(id = R.string.close)
                                        )
                                    }
                                }
                            },
                            shape = RoundedCornerShape(36.dp),
                            colors = TextFieldDefaults.colors().copy(
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .padding(end = 8.dp)
                                .animateContentSize()
                                .onSizeChanged { size ->
                                    searchBarSize = size.toSize()
                                }
                                .focusRequester(focusRequester)
                                .onGloballyPositioned {
                                    if (isSearchBarVisible) {
                                        focusRequester.requestFocus()
                                    }
                                }
                        )
                    }
                    AnimatedVisibility(
                        visible = !isSearchBarVisible,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Text(
                            text = stringResource(id = R.string.parcels),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        isSearchBarVisible = !isSearchBarVisible
                        if (!isSearchBarVisible) {
                            focusManager.clearFocus()
                            searchQuery = ""
                            homeComponent.search("", isArchive = false)
                        }
                    }) {
                        Icon(
                            Icons.Outlined.Search,
                            contentDescription = stringResource(id = R.string.search)
                        )
                    }
                    IconButton(onClick = {
                        homeComponent.getFeedItems(true)
                    }) {
                        Icon(
                            Icons.Outlined.Refresh,
                            contentDescription = stringResource(id = R.string.reload)
                        )
                    }
                },
                modifier = Modifier.noRippleClickable {
                    coroutineScope.launch {
                        if (windowSizeClass == WindowWidthSizeClass.Compact) {
                            listState.animateScrollToItem(0)
                        } else {
                            listGridState.animateScrollToItem(0)
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomEnd
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    AnimatedVisibility(
                        visible = showUnreadButton,
                        enter = slideInVertically(
                            initialOffsetY = { fullHeight -> fullHeight }
                        ),
                        exit = slideOutVertically(
                            targetOffsetY = { fullHeight -> fullHeight }
                        )
                    ) {
                        FloatingActionButton(
                            onClick = {
                                homeComponent.readAllParcels()
                            },
                            modifier = Modifier.size(45.dp),
                            containerColor = MaterialTheme.colorScheme.onSecondary
                        ) {
                            Icon(
                                painterResource(R.drawable.baseline_done_all_24),
                                contentDescription = "Read all parcels"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    FloatingActionButton(
                        onClick = {
                            showDialog = true
                        },
                        modifier = Modifier
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = stringResource(id = R.string.add_parcel)
                        )
                    }
                }
            }
        },
        contentWindowInsets = WindowInsets(0.dp)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            PullToRefreshBox(
                contentAlignment = Alignment.TopCenter,
                isRefreshing = isRefreshing,
                state = refreshState,
                onRefresh = {
                    isRefreshing = true
                    homeComponent.getFeedItems(true)
                },
            ) {
                val transition = updateTransition(
                    targetState = state == LoadState.Loading,
                    label = "homeshimmer"
                )

                when (state) {
                    is LoadState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            if (windowSizeClass != WindowWidthSizeClass.Compact) {
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(2),
                                    state = listGridState,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(5) {
                                        ShimmerFeedCard(transition, isDarkTheme, windowSizeClass)
                                    }
                                }

                            } else {
                                LazyColumn(
                                    state = listState,
                                    modifier = Modifier
                                        .fillMaxSize()
                                ) {
                                    items(5) {
                                        ShimmerFeedCard(transition, isDarkTheme, windowSizeClass)
                                    }
                                }
                            }
                        }
                    }

                    is LoadState.Success -> {
                        isRefreshing = false
                        if (windowSizeClass != WindowWidthSizeClass.Compact) {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                state = listGridState,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(start = 8.dp, end = 8.dp)
                            ) {
                                if (trackingItems.isEmpty()) {
                                    item {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(paddingValues),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(text = stringResource(id = R.string.no_parcels))
                                        }
                                    }
                                } else {
                                    items(trackingItems.size) { trackingItem ->
                                        FeedCard(
                                            tracking = trackingItems[trackingItem],
                                            onSwipe = {
                                                homeComponent.archiveParcel(trackingItems[trackingItem])
                                            },
                                            onClick = {
                                                homeComponent.navigateTo(
                                                    RootComponent.TopLevelConfiguration.SelectedElementScreenConfiguration(
                                                        trackingItems[trackingItem].id
                                                    )
                                                )

                                                if (trackingItems[trackingItem].isUnread == true) {
                                                    homeComponent.updateReadParcel(trackingItems[trackingItem])
                                                }
                                            },
                                            isDark = isDarkTheme,
                                            windowSizeClass = windowSizeClass
                                        )
                                    }
                                }
                            }
                        } else {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier
                                    .fillMaxSize()
                            ) {
                                if (trackingItems.isEmpty()) {
                                    item {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(paddingValues),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(text = stringResource(id = R.string.no_parcels))
                                        }
                                    }
                                } else {
                                    items(trackingItems) { trackingItem ->
                                        FeedCard(
                                            tracking = trackingItem,
                                            onSwipe = {
                                                homeComponent.archiveParcel(trackingItem)
                                            },
                                            onClick = {
                                                homeComponent.navigateTo(
                                                    RootComponent.TopLevelConfiguration.SelectedElementScreenConfiguration(
                                                        trackingItem.id
                                                    )
                                                )

                                                if (trackingItem.isUnread == true) {
                                                    homeComponent.updateReadParcel(trackingItem)
                                                }
                                            },
                                            isDark = isDarkTheme,
                                            windowSizeClass = windowSizeClass
                                        )
                                    }
                                }
                            }
                        }
                    }

                    is LoadState.Error -> {
                        isRefreshing = false
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(
                                    R.string.error_message,
                                    (state as LoadState.Error).message
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    TrackingBottomSheet(
        showBottomSheet = showDialog,
        onBSStateChange = { showDialog = it },
        addTracking = homeComponent::addTracking
    )

    CheckAndEnablePushNotificationsDialog()
    CheckAndDisableBatteryOptimizationDialog()
}