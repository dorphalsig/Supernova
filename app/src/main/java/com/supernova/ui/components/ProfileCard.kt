package com.supernova.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.supernova.R
import com.supernova.data.entities.ProfileEntity
import com.supernova.ui.model.ProfileDisplayItem
import com.supernova.ui.theme.SupernovaColors

@Composable
fun ProfileCard(
    item: ProfileDisplayItem,
    position: CardPosition,
    onClick: (ProfileEntity?) -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }

    val cardTransforms = getCardTransforms(position, isFocused)

    Card(
        modifier = modifier
            .size(300.dp, 400.dp)
            .graphicsLayer {
                scaleX = cardTransforms.scale
                scaleY = cardTransforms.scale
                rotationY = cardTransforms.rotationY
                alpha = cardTransforms.alpha
            }
            .focusable()
            .onFocusChanged { isFocused = it.isFocused }
            .clickable(enabled = item is ProfileDisplayItem.RealProfile) {
                if (item is ProfileDisplayItem.RealProfile) {
                    onClick(item.profile)
                } else {
                    onClick(null)
                }
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (position == CardPosition.Center && isFocused) {
                SupernovaColors.SurfaceVariant
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = if (isFocused) {
            androidx.compose.foundation.BorderStroke(2.dp, SupernovaColors.Focus)
        } else null,
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (position == CardPosition.Center) 12.dp else 6.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Avatar Section
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                when (item) {
                    is ProfileDisplayItem.RealProfile -> {
                        ProfileAvatar(
                            profile = item.profile,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Lock indicator if profile has PIN
                        if (item.profile.pin != null) {
                            Icon(
                                painter = painterResource(id = android.R.drawable.ic_lock_idle_lock),
                                contentDescription = "Profile has PIN",
                                tint = SupernovaColors.OnSurface,
                                modifier = Modifier
                                    .size(32.dp)
                                    .align(Alignment.TopEnd)
                                    .offset((-8).dp, 8.dp)
                                    .background(
                                        SupernovaColors.LockBackground,
                                        CircleShape
                                    )
                                    .border(
                                        2.dp,
                                        SupernovaColors.Border,
                                        CircleShape
                                    )
                                    .padding(6.dp)
                            )
                        }
                    }
                    is ProfileDisplayItem.PlaceholderProfile -> {
                        Image(
                            painter = painterResource(id = R.drawable.inactive_avatar),
                            contentDescription = "Add Profile",
                            modifier = Modifier
                                .fillMaxSize()
                                .alpha(0.5f),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Profile Name
            Text(
                text = when (item) {
                    is ProfileDisplayItem.RealProfile -> item.profile.name
                    is ProfileDisplayItem.PlaceholderProfile -> "Add Profile"
                },
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = if (item is ProfileDisplayItem.PlaceholderProfile) {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ProfileAvatar(
    profile: ProfileEntity,
    modifier: Modifier = Modifier
) {
    var imageLoadError by remember { mutableStateOf(false) }

    if (imageLoadError) {
        // Fallback if URL fails to load
        Box(
            modifier = modifier.background(SupernovaColors.Primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = profile.name.take(1).uppercase(),
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = SupernovaColors.OnSurface
            )
        }
    } else {
        AsyncImage(
            model = profile.avatar,
            contentDescription = "Profile Avatar",
            modifier = modifier,
            contentScale = ContentScale.Crop,
            placeholder = painterResource(id = android.R.drawable.ic_menu_gallery),
            error = painterResource(id = android.R.drawable.ic_menu_close_clear_cancel),
            onError = { imageLoadError = true }
        )
    }
}

enum class CardPosition {
    Left, Center, Right
}

private data class CardTransforms(
    val scale: Float,
    val rotationY: Float,
    val alpha: Float
)

private fun getCardTransforms(position: CardPosition, isFocused: Boolean): CardTransforms {
    return when (position) {
        CardPosition.Center -> CardTransforms(
            scale = if (isFocused) 1.15f else 1.1f,
            rotationY = 0f,
            alpha = 1f
        )
        CardPosition.Left -> CardTransforms(
            scale = if (isFocused) 0.95f else 0.9f,
            rotationY = 15f,
            alpha = 0.7f
        )
        CardPosition.Right -> CardTransforms(
            scale = if (isFocused) 0.95f else 0.9f,
            rotationY = -15f,
            alpha = 0.7f
        )
    }
}