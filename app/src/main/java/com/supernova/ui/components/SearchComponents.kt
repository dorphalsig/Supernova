package com.supernova.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource

/** Search bar with optional voice input */
@Composable
fun SearchBar(onQueryChanged: (String) -> Unit, modifier: Modifier = Modifier) {
    val (query, setQuery) = remember { mutableStateOf("") }
    androidx.compose.material3.TextField(
        value = query,
        onValueChange = {
            setQuery(it)
            onQueryChanged(it)
        },
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text("Search") },
        trailingIcon = {
            IconButton(onClick = { /* voice search */ }) {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_btn_speak_now),
                    contentDescription = "Voice Search"
                )
            }
        },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedPlaceholderColor = Color.Gray,
            focusedPlaceholderColor = Color.Gray
        )
    )
}
