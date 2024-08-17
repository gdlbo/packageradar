package ru.parcel.app.nav.archive

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import kotlinx.coroutines.launch
import ru.parcel.app.R
import ru.parcel.app.nav.RootComponent
import ru.parcel.app.nav.WindowWidthSizeClass
import ru.parcel.app.nav.calculateWindowSizeClass
import ru.parcel.app.nav.home.LoadState
import ru.parcel.app.ui.components.FeedCard
import ru.parcel.app.ui.components.ShimmerFeedCard
import ru.parcel.app.ui.components.noRippleClickable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchiveComponentImpl(archiveComponent: ArchiveComponent) {
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
                val transition = updateTransition(
                    targetState = (state == LoadState.Loading),
                    label = "archiveshimmer"
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
                }
            }
        }
    }
}