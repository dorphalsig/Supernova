package com.supernova.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.supernova.ui.theme.SupernovaTheme

@Composable
fun FocusableButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var focused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (focused) 1.05f else 1f, label = "scale")
    val background = if (focused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer
    val contentColor = if (focused) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer

    Box(
        modifier
            .onFocusChanged { focused = it.isFocused }
            .focusable()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                shadowElevation = if (focused) 8f else 0f
            }
            .background(background, RoundedCornerShape(8.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(text = label, color = contentColor)
    }
}

@Preview
@Composable
private fun FocusableButtonPreviewLight() {
    SupernovaTheme(darkTheme = false) {
        FocusableButton(label = "Play", onClick = {})
    }
}

@Preview
@Composable
private fun FocusableButtonPreviewDark() {
    SupernovaTheme(darkTheme = true) {
        FocusableButton(label = "Play", onClick = {})
    }
}
