package ru.gdlbo.parcelradar.app.nav.archive

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.*
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ru.gdlbo.parcelradar.app.R
import ru.gdlbo.parcelradar.app.core.network.model.Tracking
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
    val windowSizeClass = calculateWindowSizeClass(LocalConfiguration.current.screenWidthDp.dp)

    val listGridState = rememberLazyGridState(
        initialFirstVisibleItemIndex = archiveComponent.gridScrollIndex,
        initialFirstVisibleItemScrollOffset = archiveComponent.gridScrollOffset
    )
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = archiveComponent.listScrollIndex,
        initialFirstVisibleItemScrollOffset = archiveComponent.listScrollOffset
    )

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }
            .collect { (index, offset) ->
                archiveComponent.listScrollIndex = index
                archiveComponent.listScrollOffset = offset
            }
    }

    LaunchedEffect(listGridState) {
        snapshotFlow { listGridState.firstVisibleItemIndex to listGridState.firstVisibleItemScrollOffset }
            .collect { (index, offset) ->
                archiveComponent.gridScrollIndex = index
                archiveComponent.gridScrollOffset = offset
            }
    }

    val coroutineScope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }
    val refreshState = rememberPullToRefreshState()
    var isSearchBarVisible by rememberSaveable { mutableStateOf(false) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val isDarkTheme = themeManager.isDarkTheme.value ?: isSystemInDarkTheme()

    LaunchedEffect(tappedState) {
        if (tappedState) {
            listState.animateScrollToItem(0)
            listGridState.animateScrollToItem(0)
            archiveComponent.scrollUp()
        }
    }

    Scaffold(
        topBar = {
            ArchiveTopBar(
                isSearchBarVisible = isSearchBarVisible,
                searchQuery = searchQuery,
                onSearchQueryChange = {
                    searchQuery = it
                    archiveComponent.search(it)
                },
                onSearchBarVisibilityChange = { visible ->
                    isSearchBarVisible = visible
                    if (!visible) {
                        focusManager.clearFocus()
                        searchQuery = ""
                        archiveComponent.search("")
                    }
                },
                onRefresh = { archiveComponent.getFeedItems(true) },
                onTitleClick = {
                    coroutineScope.launch {
                        if (windowSizeClass == WindowWidthSizeClass.Compact) {
                            listState.animateScrollToItem(0)
                        } else {
                            listGridState.animateScrollToItem(0)
                        }
                    }
                },
                focusRequester = focusRequester
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
                        LoadingView(windowSizeClass, listGridState, listState)
                    } else {
                        when (state) {
                            is LoadState.Success -> {
                                isRefreshing = false
                                if (trackingItems.isEmpty()) {
                                    EmptyView()
                                } else {
                                    TrackingListView(
                                        windowSizeClass = windowSizeClass,
                                        listGridState = listGridState,
                                        listState = listState,
                                        trackingItems = trackingItems,
                                        archiveComponent = archiveComponent,
                                        isDarkTheme = isDarkTheme
                                    )
                                }
                            }

                            is LoadState.Error -> {
                                isRefreshing = false
                                ErrorView(message = (state as LoadState.Error).message.toString())
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ArchiveTopBar(
    isSearchBarVisible: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearchBarVisibilityChange: (Boolean) -> Unit,
    onRefresh: () -> Unit,
    onTitleClick: () -> Unit,
    focusRequester: FocusRequester
) {
    TopAppBar(
        title = {
            AnimatedVisibility(
                visible = isSearchBarVisible,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                TextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
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
                            IconButton(onClick = { onSearchQueryChange("") }) {
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
            IconButton(onClick = { onSearchBarVisibilityChange(!isSearchBarVisible) }) {
                Icon(
                    Icons.Outlined.Search,
                    contentDescription = stringResource(id = R.string.search)
                )
            }
            IconButton(onClick = onRefresh) {
                Icon(
                    Icons.Outlined.Refresh,
                    contentDescription = stringResource(id = R.string.reload)
                )
            }
        },
        modifier = Modifier.noRippleClickable(onClick = onTitleClick)
    )
}

@Composable
private fun LoadingView(
    windowSizeClass: WindowWidthSizeClass,
    listGridState: LazyGridState,
    listState: LazyListState
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (windowSizeClass != WindowWidthSizeClass.Compact) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                state = listGridState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
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
}

@Composable
private fun EmptyView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(id = R.string.no_parcels),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ErrorView(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.error_message, message),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TrackingListView(
    windowSizeClass: WindowWidthSizeClass,
    listGridState: LazyGridState,
    listState: LazyListState,
    trackingItems: List<Tracking>,
    archiveComponent: ArchiveComponent,
    isDarkTheme: Boolean
) {
    val itemContent: @Composable (Tracking, Modifier) -> Unit = { trackingItem, modifier ->
        key(trackingItem.id) {
            FeedCard(
                tracking = trackingItem,
                onSwipe = {
                    archiveComponent.restoreParcel(trackingItem)
                },
                onClick = {
                    archiveComponent.navigateTo(
                        RootComponent.TopLevelConfiguration.SelectedElementScreenConfiguration(
                            trackingItem.id
                        )
                    )
                },
                isDark = isDarkTheme,
                windowSizeClass = windowSizeClass,
                modifier = modifier
            )
        }
    }

    if (windowSizeClass != WindowWidthSizeClass.Compact) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            state = listGridState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = trackingItems,
                key = { it.id }
            ) { trackingItem ->
                itemContent(trackingItem, Modifier.animateItem(fadeInSpec = null, fadeOutSpec = null))
            }
        }
    } else {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = trackingItems,
                key = { it.id }
            ) { trackingItem ->
                itemContent(trackingItem, Modifier.animateItem(fadeInSpec = null, fadeOutSpec = null))
            }
        }
    }
}
