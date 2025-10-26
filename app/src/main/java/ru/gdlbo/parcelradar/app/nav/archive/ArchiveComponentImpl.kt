package ru.gdlbo.parcelradar.app.nav.archive

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import kotlinx.coroutines.launch
import ru.gdlbo.parcelradar.app.R
import ru.gdlbo.parcelradar.app.nav.RootComponent
import ru.gdlbo.parcelradar.app.nav.WindowWidthSizeClass
import ru.gdlbo.parcelradar.app.nav.calculateWindowSizeClass
import ru.gdlbo.parcelradar.app.nav.home.LoadState
import ru.gdlbo.parcelradar.app.ui.components.FeedCard
import ru.gdlbo.parcelradar.app.ui.components.ShimmerFeedCard
import ru.gdlbo.parcelradar.app.ui.components.noRippleClickable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchiveComponentImpl(archiveComponent: ArchiveComponent) {
    val tappedState by archiveComponent.isScrollable.collectAsState()
    val themeManager = remember { archiveComponent.themeManager }
    val trackingItems by archiveComponent.trackingItemList.collectAsState()
    val state by archiveComponent.loadState.collectAsState()
    val listState = rememberLazyListState()
    val windowSizeClass = calculateWindowSizeClass(LocalConfiguration.current.screenWidthDp.dp)
    val listGridState = rememberLazyGridState()
    val coroutineScope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }
    val refreshState = rememberPullToRefreshState()
    var isSearchBarVisible by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var searchBarSize by remember { mutableStateOf(Size.Zero) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val isDarkTheme = themeManager.isDarkTheme.value ?: isSystemInDarkTheme()

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
                                archiveComponent.search(it, isArchive = true)
                            },
                            placeholder = {
                                Text(
                                    text = stringResource(id = R.string.search),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
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
                                        archiveComponent.search("", isArchive = true)
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
                            textStyle = MaterialTheme.typography.bodyLarge,
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
                            text = stringResource(id = R.string.archive),
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
                            archiveComponent.search("", isArchive = true)
                        }
                    }) {
                        Icon(
                            Icons.Outlined.Search,
                            contentDescription = stringResource(id = R.string.search)
                        )
                    }
                    IconButton(onClick = {
                        archiveComponent.getFeedItems(true)
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
                    archiveComponent.getFeedItems(true)
                },
            ) {
                val isLoading = state is LoadState.Loading

                Crossfade(targetState = isLoading, animationSpec = tween(durationMillis = 500)) { loading ->
                    if (loading) {
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
                                        ShimmerFeedCard(
                                            isLoading = true,
                                            windowSizeClass = windowSizeClass
                                        )
                                    }
                                }

                            } else {
                                LazyColumn(
                                    state = listState,
                                    modifier = Modifier
                                        .fillMaxSize()
                                ) {
                                    items(5) {
                                        ShimmerFeedCard(
                                            isLoading = true,
                                            windowSizeClass = windowSizeClass
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        when (state) {
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
                                                        archiveComponent.archiveParcel(trackingItems[trackingItem])
                                                    },
                                                    onClick = {
                                                        archiveComponent.navigateTo(
                                                            RootComponent.TopLevelConfiguration.SelectedElementScreenConfiguration(
                                                                trackingItems[trackingItem].id
                                                            )
                                                        )
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
                                                        archiveComponent.archiveParcel(trackingItem)
                                                    },
                                                    onClick = {
                                                        archiveComponent.navigateTo(
                                                            RootComponent.TopLevelConfiguration.SelectedElementScreenConfiguration(
                                                                trackingItem.id
                                                            )
                                                        )
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

                            else -> {
                                // Nothing to do here
                            }
                        }
                    }
                }
            }
        }
    }
}