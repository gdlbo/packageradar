package ru.gdlbo.parcelradar.app.nav.home

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import ru.gdlbo.parcelradar.app.nav.calculateWindowSizeClass
import ru.gdlbo.parcelradar.app.nav.home.components.HomeBottomSheet
import ru.gdlbo.parcelradar.app.nav.home.components.HomeContent
import ru.gdlbo.parcelradar.app.nav.home.components.HomeFloatingActionButtons
import ru.gdlbo.parcelradar.app.nav.home.components.HomeTopBar
import ru.gdlbo.parcelradar.app.ui.components.CheckAndDisableBatteryOptimizationDialog
import ru.gdlbo.parcelradar.app.ui.components.CheckAndEnablePushNotificationsDialog

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
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val isDarkTheme = themeManager.isDarkTheme.collectAsState().value ?: isSystemInDarkTheme()
    val showUnreadButton = trackingItems.any { parcel ->
        parcel.isUnread == true
    }

    LaunchedEffect(tappedState) {
        listState.animateScrollToItem(0)
        listGridState.animateScrollToItem(0)
    }

    Scaffold(
        topBar = {
            HomeTopBar(
                isSearchBarVisible = isSearchBarVisible,
                onSearchBarVisibleChange = { isSearchBarVisible = it },
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                homeComponent = homeComponent,
                focusRequester = focusRequester,
                focusManager = focusManager,
                windowSizeClass = windowSizeClass,
                listState = listState,
                listGridState = listGridState,
                coroutineScope = coroutineScope
            )
        },
        floatingActionButton = {
            HomeFloatingActionButtons(
                showUnreadButton = showUnreadButton,
                homeComponent = homeComponent,
                onShowDialogChange = { showDialog = it }
            )
        },
        contentWindowInsets = WindowInsets(0.dp)
    ) { paddingValues ->
        HomeContent(
            paddingValues = paddingValues,
            isRefreshing = isRefreshing,
            onRefreshingChange = { isRefreshing = it },
            refreshState = refreshState,
            homeComponent = homeComponent,
            state = state,
            windowSizeClass = windowSizeClass,
            listGridState = listGridState,
            listState = listState,
            trackingItems = trackingItems,
            isDarkTheme = isDarkTheme
        )
    }

    HomeBottomSheet(
        showDialog = showDialog,
        onShowDialogChange = { showDialog = it },
        homeComponent = homeComponent
    )

    CheckAndEnablePushNotificationsDialog()
    CheckAndDisableBatteryOptimizationDialog()
}