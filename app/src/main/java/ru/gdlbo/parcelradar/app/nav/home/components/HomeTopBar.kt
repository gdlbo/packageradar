package ru.gdlbo.parcelradar.app.nav.home.components

import androidx.compose.animation.*
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ru.gdlbo.parcelradar.app.R
import ru.gdlbo.parcelradar.app.nav.WindowWidthSizeClass
import ru.gdlbo.parcelradar.app.nav.home.HomeComponent
import ru.gdlbo.parcelradar.app.ui.components.noRippleClickable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(
    isSearchBarVisible: Boolean,
    onSearchBarVisibleChange: (Boolean) -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    homeComponent: HomeComponent,
    focusRequester: FocusRequester,
    focusManager: FocusManager,
    windowSizeClass: WindowWidthSizeClass,
    listState: LazyListState,
    listGridState: LazyGridState,
    coroutineScope: CoroutineScope
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
                    onValueChange = {
                        onSearchQueryChange(it)
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
                                onSearchQueryChange("")
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
                val newVisible = !isSearchBarVisible
                onSearchBarVisibleChange(newVisible)
                if (!newVisible) {
                    focusManager.clearFocus()
                    onSearchQueryChange("")
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
}
