package com.supernova.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun <T> ContentRail(
    title: String,
    items: List<T>,
    modifier: Modifier = Modifier,
    itemContent: @Composable (T) -> Unit
) {
    val listState = rememberLazyListState()

    Column(modifier) {
        BasicText(
            text = title,
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
        )
        LazyRow(state = listState) {
            itemsIndexed(items) { _, item ->
                itemContent(item)
            }
        }
    }
}
