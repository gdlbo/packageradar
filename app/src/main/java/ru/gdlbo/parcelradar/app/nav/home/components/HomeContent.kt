package ru.gdlbo.parcelradar.app.nav.home.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.gdlbo.parcelradar.app.R
import ru.gdlbo.parcelradar.app.core.network.model.Tracking
import ru.gdlbo.parcelradar.app.nav.RootComponent
import ru.gdlbo.parcelradar.app.nav.WindowWidthSizeClass
import ru.gdlbo.parcelradar.app.nav.home.HomeComponent
import ru.gdlbo.parcelradar.app.nav.home.LoadState
import ru.gdlbo.parcelradar.app.ui.components.FeedCard
import ru.gdlbo.parcelradar.app.ui.components.ShimmerFeedCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(
    paddingValues: PaddingValues,
    isRefreshing: Boolean,
    onRefreshingChange: (Boolean) -> Unit,
    refreshState: PullToRefreshState,
    homeComponent: HomeComponent,
    state: LoadState,
    windowSizeClass: WindowWidthSizeClass,
    listGridState: LazyGridState,
    listState: LazyListState,
    trackingItems: List<Tracking>,
    isDarkTheme: Boolean
) {
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
                onRefreshingChange(true)
                homeComponent.getFeedItems(true)
            },
        ) {
            val isLoading = state is LoadState.Loading

            Crossfade(targetState = isLoading, animationSpec = tween(durationMillis = 300)) { loading ->
                if (loading) {
                    LoadingView(windowSizeClass, listGridState, listState)
                } else {
                    when (state) {
                        is LoadState.Success -> {
                            onRefreshingChange(false)
                            if (trackingItems.isEmpty()) {
                                EmptyView()
                            } else {
                                TrackingListView(
                                    windowSizeClass = windowSizeClass,
                                    listGridState = listGridState,
                                    listState = listState,
                                    trackingItems = trackingItems,
                                    homeComponent = homeComponent,
                                    isDarkTheme = isDarkTheme
                                )
                            }
                        }

                        is LoadState.Error -> {
                            onRefreshingChange(false)
                            ErrorView(message = state.message.toString())
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
                items(6) {
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
                items(6) {
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

@Composable
private fun TrackingListView(
    windowSizeClass: WindowWidthSizeClass,
    listGridState: LazyGridState,
    listState: LazyListState,
    trackingItems: List<Tracking>,
    homeComponent: HomeComponent,
    isDarkTheme: Boolean
) {
    val itemContent: @Composable (Tracking) -> Unit = { trackingItem ->
        key(trackingItem.id) {
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
                itemContent(trackingItem)
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
                itemContent(trackingItem)
            }
        }
    }
}
