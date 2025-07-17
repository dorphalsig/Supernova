package com.supernova.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.focus.FocusRequester
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.background
import androidx.compose.ui.platform.testTag
import com.supernova.ui.components.MediaCard
import com.supernova.ui.components.FocusableButton
import com.supernova.ui.components.SearchBar

@Composable
fun SearchResultsScreen(
    onItemSelected: (ContentType, String) -> Unit,
    onSeeAll: (ContentType) -> Unit,
    onBackPressed: () -> Unit,
    viewModel: SearchResultsViewModel = viewModel(),
) {
    val query by viewModel.query.collectAsState()
    val topResults by viewModel.topResults.collectAsState()
    val movies by viewModel.movies.collectAsState()
    val series by viewModel.series.collectAsState()
    val channels by viewModel.channels.collectAsState()

    val focusRequester = FocusRequester()

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        SearchBar(
            onQueryChanged = viewModel::onQueryChanged,
            modifier = Modifier
                .padding(16.dp)
                .focusRequester(focusRequester)
                .testTag("search_bar")
        )
        Spacer(modifier = Modifier.height(8.dp))
        if (topResults.isNotEmpty()) {
            Text(
                text = "Top Results",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            TopResultsGrid(items = topResults, onItemSelected = onItemSelected)
        }
        CategoryRail(
            title = "Movies",
            items = movies,
            onItemSelected = { id -> onItemSelected(ContentType.MOVIE, id) },
            onSeeAll = { onSeeAll(ContentType.MOVIE) }
        )
        CategoryRail(
            title = "Series",
            items = series,
            onItemSelected = { id -> onItemSelected(ContentType.SERIES, id) },
            onSeeAll = { onSeeAll(ContentType.SERIES) }
        )
        CategoryRail(
            title = "Channels",
            items = channels,
            onItemSelected = { id -> onItemSelected(ContentType.CHANNEL, id) },
            onSeeAll = { onSeeAll(ContentType.CHANNEL) }
        )
    }
}

@Composable
fun TopResultsGrid(
    items: List<ContentItem>,
    onItemSelected: (ContentType, String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.padding(16.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
    ) {
        items(items) { item ->
            MediaCard(
                title = item.title,
                imageUrl = item.imageUrl,
                onClick = { onItemSelected(item.type, item.id) }
            )
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
fun CategoryRail(
    title: String,
    items: List<ContentItem>,
    onItemSelected: (String) -> Unit,
    onSeeAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) return
    Column(modifier = modifier.padding(vertical = 8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
        ) {
            items(items.take(4)) { item ->
                MediaCard(
                    title = item.title,
                    imageUrl = item.imageUrl,
                    onClick = { onItemSelected(item.id) }
                )
            }
            item {
                SeeAllButton(onClick = onSeeAll)
            }
        }
    }
}

@Composable
fun SeeAllButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    FocusableButton(
        onClick = onClick,
        modifier = modifier
            .size(200.dp, 300.dp)
            .testTag("see_all")
    ) {
        Text("See All")
    }
}

