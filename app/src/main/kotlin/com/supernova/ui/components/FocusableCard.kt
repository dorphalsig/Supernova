package com.supernova.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.supernova.ui.theme.SupernovaTheme

@Composable
fun FocusableCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    var focused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (focused) 1.1f else 1f, label = "scale")

    Box(
        modifier
            .onFocusChanged { focused = it.isFocused }
            .focusable()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                shadowElevation = if (focused) 10f else 0f
            }
            .border(
                if (focused) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else BorderStroke(0.dp, MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() },
        content = content
    )
}

@Preview
@Composable
private fun FocusableCardPreviewLight() {
    SupernovaTheme(darkTheme = false) {
        FocusableCard(onClick = {}) {}
    }
}

@Preview
@Composable
private fun FocusableCardPreviewDark() {
    SupernovaTheme(darkTheme = true) {
        FocusableCard(onClick = {}) {}
    }
}
