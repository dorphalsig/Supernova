package com.supernova.ui.components

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Minimal focusable card for TV surfaces.
 */
@Composable
fun FocusableCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    content: @Composable BoxScope.() -> Unit
) {
    Card(
        modifier = modifier.focusable(),
        onClick = onClick,
        colors = CardDefaults.cardColors()
    ) {
        Box(Modifier.fillMaxSize(), content = content)
    }
}

@Composable
@androidx.compose.ui.tooling.preview.Preview
private fun FocusableCardPreview() {
    FocusableCard {}
}
