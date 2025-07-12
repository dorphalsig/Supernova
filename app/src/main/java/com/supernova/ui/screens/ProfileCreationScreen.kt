package com.supernova.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.Coil
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.supernova.ui.theme.SupernovaColors

@Composable
fun ProfileCreationScreen(
    avatars: List<String>,
    onNameChange: (String) -> Unit,
    onPinChange: (String) -> Unit,
    onAvatarSelected: (String) -> Unit,
    onSave: () -> Unit
) {
    val context = LocalContext.current
    // Use Coil's default loader for preloading
    val imageLoader: ImageLoader = Coil.imageLoader(context)

    // Preload all avatar URLs into memory cache on first composition
    LaunchedEffect(avatars) {
        avatars.forEach { avatarUrl ->
            val request = ImageRequest.Builder(context)
                .data(avatarUrl)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.ENABLED)
                .build()
            imageLoader.enqueue(request)
        }
    }

    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val columns = if (screenWidthDp >= 720) 6 else 4

    var name by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var selectedAvatar by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it.take(20)
                onNameChange(name)
            },
            label = { Text(text = "Name", fontSize = 20.sp) },
            modifier = Modifier
                .fillMaxWidth()
                .focusable()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            repeat(4) { index ->
                OutlinedTextField(
                    value = pin.getOrNull(index)?.toString() ?: "",
                    onValueChange = { input ->
                        if (input.length <= 1 && input.all { it.isDigit() }) {
                            pin = pin.padEnd(4, '0').toCharArray().apply {
                                this[index] = input.firstOrNull() ?: '0'
                            }.concatToString().trimEnd('0')
                            onPinChange(pin)
                        }
                    },
                    modifier = Modifier
                        .width(64.dp)
                        .focusable(),
                    singleLine = true
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "{sv_string.create_profile_choose_avatar}",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.focusable()
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(avatars) { avatarUrl ->
                AvatarItem(
                    avatarUrl = avatarUrl,
                    isSelected = avatarUrl == selectedAvatar,
                    onSelected = {
                        selectedAvatar = avatarUrl
                        onAvatarSelected(avatarUrl)
                    }
                )
            }
        }
        Button(
            onClick = onSave,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .focusable()
        ) {
            Text(text = "{sv_string.save}", fontSize = 20.sp)
        }
    }
}

@Composable
fun AvatarItem(
    avatarUrl: String,
    isSelected: Boolean,
    onSelected: () -> Unit
) {
    val bgColor = MaterialTheme.colorScheme.surfaceVariant
    val shadowShape = RoundedCornerShape(12.dp)
    val highlightColor = if (isSelected) SupernovaColors.Focus.copy(alpha = 0.6f) else Color.Transparent
    Box(
        modifier = Modifier
            .size(160.dp)
            .shadow(8.dp, shape = shadowShape)
            .background(bgColor, shape = shadowShape)
            .drawBehind {
                val radius = size.minDimension * 0.5f
                val colors = listOf(highlightColor, Color.Transparent)
                listOf(
                    androidx.compose.ui.geometry.Offset(0f, 0f),
                    androidx.compose.ui.geometry.Offset(size.width, 0f),
                    androidx.compose.ui.geometry.Offset(0f, size.height),
                    androidx.compose.ui.geometry.Offset(size.width, size.height)
                ).forEach { corner ->
                    drawCircle(
                        brush = Brush.radialGradient(colors, center = corner, radius = radius),
                        radius = radius,
                        center = corner
                    )
                }
            }
            .clickable(onClick = onSelected)
            .focusable(),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = avatarUrl,
            contentDescription = null,
            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}
