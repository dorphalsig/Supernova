package com.supernova.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.supernova.R
import com.supernova.ui.theme.SupernovaColors

/** Category card used in grid layouts */
@Composable
fun CategoryCard(
    name: String,
    imageUrl: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var focused by remember { mutableStateOf(false) }
    Card(
        modifier = modifier
            .size(180.dp)
            .focusable()
            .onFocusChanged { focused = it.isFocused }
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        border = if (focused) BorderStroke(2.dp, SupernovaColors.Focus) else null,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Text(
                text = name,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .background(SupernovaColors.SurfaceVariant.copy(alpha = 0.7f))
                    .padding(4.dp)
            )
        }
    }
}

/** Media card showing poster art with metadata overlay */
@Composable
fun MediaCard(
    title: String,
    imageUrl: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var focused by remember { mutableStateOf(false) }
    Card(
        modifier = modifier
            .size(200.dp, 300.dp)
            .focusable()
            .onFocusChanged { focused = it.isFocused }
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        border = if (focused) BorderStroke(2.dp, SupernovaColors.Focus) else null,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.inactive_avatar),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .background(SupernovaColors.SurfaceVariant.copy(alpha = 0.7f))
                    .padding(4.dp)
            )
        }
    }
}

/** Simple navigation rail optimized for D-pad */
@Composable
fun NavigationRail(
    items: List<String>,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(72.dp)
            .background(SupernovaColors.SurfaceVariant),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items.forEachIndexed { index, label ->
            FocusableButton(
                onClick = { onItemSelected(index) },
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .fillMaxWidth()
            ) {
                Text(text = label, fontSize = 14.sp)
            }
        }
    }
}

/** Branded loading spinner */
@Composable
fun LoadingSpinner(modifier: Modifier = Modifier) {
    androidx.compose.material3.CircularProgressIndicator(
        color = SupernovaColors.Primary,
        modifier = modifier.size(48.dp)
    )
}

/** Button with larger hit box and focus indicator */
@Composable
fun FocusableButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    var focused by remember { mutableStateOf(false) }
    androidx.compose.material3.Button(
        onClick = onClick,
        modifier = modifier
            .focusable()
            .onFocusChanged { focused = it.isFocused },
        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
            containerColor = if (focused) SupernovaColors.Focus else MaterialTheme.colorScheme.primary
        ),
        content = content
    )
}
