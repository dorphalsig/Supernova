package com.supernova.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.supernova.ui.components.FocusableButton
import com.supernova.ui.components.LoadingSpinner
import com.supernova.ui.components.MediaCard

@Composable
fun HomeScreen(
    onContentSelected: (String, Int) -> Unit,
    onRailExpanded: (String) -> Unit,
    onSearchClicked: () -> Unit,
    onProfileClicked: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        viewModel.loadHome()
        focusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .focusRequester(focusRequester)
    ) {
        // Top Bar with profile and search
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FocusableButton(onClick = onProfileClicked) {
                Text("Profile")
            }
            Spacer(modifier = Modifier.weight(1f))
            FocusableButton(onClick = onSearchClicked) {
                Text("Search")
            }
        }

        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                LoadingSpinner()
            }
            return
        }

        ContentRail(
            title = "Continue Watching",
            items = state.continueWatching,
            onSeeAll = { onRailExpanded("continue") },
            card = { item ->
                ContinueWatchingCard(item) { onContentSelected("stream", item.id) }
            }
        )
        ContentRail(
            title = "Trending Movies",
            items = state.trendingMovies,
            onSeeAll = { onRailExpanded("movies") },
            card = { item -> TrendingMediaCard(item) { onContentSelected("movie", item.id) } }
        )
        ContentRail(
            title = "Trending Series",
            items = state.trendingSeries,
            onSeeAll = { onRailExpanded("series") },
            card = { item -> TrendingMediaCard(item) { onContentSelected("series", item.id) } }
        )
        ContentRail(
            title = "For You",
            items = state.forYou,
            onSeeAll = { onRailExpanded("for_you") },
            card = { item -> TrendingMediaCard(item) { onContentSelected("movie", item.id) } }
        )
    }
}

@Composable
fun ContentRail(
    title: String,
    items: List<MediaItem>,
    onSeeAll: () -> Unit,
    card: @Composable (MediaItem) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        RailHeader(title = title, onSeeAll = onSeeAll)
        if (items.isEmpty()) {
            Text(
                text = "No items",
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(8.dp)
            )
        } else {
            LazyRow(modifier = Modifier.padding(horizontal = 16.dp)) {
                items(items) { item ->
                    Box(modifier = Modifier.padding(end = 12.dp)) {
                        card(item)
                    }
                }
            }
        }
    }
}

@Composable
fun RailHeader(title: String, onSeeAll: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("header_" + title),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f)
        )
        FocusableButton(onClick = onSeeAll) { Text("See All") }
    }
}

@Composable
fun ContinueWatchingCard(item: MediaItem, onClick: () -> Unit) {
    Box {
        MediaCard(title = item.title, imageUrl = item.posterUrl, onClick = onClick)
        item.progress?.let { progress ->
            androidx.compose.material3.LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
fun TrendingMediaCard(item: MediaItem, onClick: () -> Unit) {
    MediaCard(title = item.title, imageUrl = item.posterUrl, onClick = onClick)
}
