package com.supernova.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.supernova.data.entities.ProfileEntity
import com.supernova.ui.components.CardPosition
import com.supernova.ui.components.ProfileCard
import com.supernova.ui.model.CarouselState
import com.supernova.ui.model.ProfileDisplayItem
import com.supernova.ui.model.toCarouselItems
import com.supernova.utils.AvatarPreloader

@Composable
fun ProfileSelectionScreen(
    onProfileSelected: (ProfileEntity) -> Unit,
    onAddProfile: () -> Unit,
    viewModel: ProfileSelectionViewModel = viewModel()
) {
    val context = LocalContext.current
    val avatarPreloader = remember { AvatarPreloader(context) }

    val profiles by viewModel.profiles.collectAsState()
    val carouselState by remember(profiles) {
        derivedStateOf {
            CarouselState(profiles.toCarouselItems())
        }
    }

    var currentCarouselState by remember { mutableStateOf(carouselState) }
    val focusRequester = remember { FocusRequester() }

    // Update carousel state when profiles change
    LaunchedEffect(carouselState) {
        currentCarouselState = carouselState
    }

    // Load profiles when screen starts
    LaunchedEffect(Unit) {
        viewModel.loadProfiles()
        // Request focus on the screen for TV remote navigation
        focusRequester.requestFocus()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .focusRequester(focusRequester)
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    when (keyEvent.key) {
                        Key.DirectionLeft, Key.A -> {
                            if (currentCarouselState.canMoveLeft()) {
                                currentCarouselState = currentCarouselState.moveLeft()
                                avatarPreloader.preloadAdjacentAvatars(
                                    currentCarouselState,
                                    AvatarPreloader.Direction.LEFT
                                )
                                true
                            } else false
                        }
                        Key.DirectionRight, Key.D -> {
                            if (currentCarouselState.canMoveRight()) {
                                currentCarouselState = currentCarouselState.moveRight()
                                avatarPreloader.preloadAdjacentAvatars(
                                    currentCarouselState,
                                    AvatarPreloader.Direction.RIGHT
                                )
                                true
                            } else false
                        }
                        Key.Enter, Key.DirectionCenter -> {
                            when (val centerItem = currentCarouselState.centerItem) {
                                is ProfileDisplayItem.RealProfile -> {
                                    onProfileSelected(centerItem.profile)
                                    true
                                }
                                is ProfileDisplayItem.PlaceholderProfile -> {
                                    onAddProfile()
                                    true
                                }
                                else -> false
                            }
                        }
                        else -> false
                    }
                } else false
            }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title
            Text(
                text = "Choose Your Profile",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 32.dp, bottom = 32.dp)
            )

            // Carousel
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                ProfileCarousel(
                    carouselState = currentCarouselState,
                    onProfileClick = { profile ->
                        if (profile != null) {
                            onProfileSelected(profile)
                        } else {
                            onAddProfile()
                        }
                    }
                )
            }

            // Instructions
            Text(
                text = "Use ◀ or ▶ to select",
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )
        }

        // Add Profile FAB
        FloatingActionButton(
            onClick = onAddProfile,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Profile",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
private fun ProfileCarousel(
    carouselState: CarouselState,
    onProfileClick: (ProfileEntity?) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp),
        contentAlignment = Alignment.Center
    ) {
        // Left card (partially visible)
        carouselState.leftItem?.let { item ->
            ProfileCard(
                item = item,
                position = CardPosition.Left,
                onClick = onProfileClick,
                modifier = Modifier
                    .offset(x = (-200).dp)
                    .width(200.dp) // Partially visible
            )
        }

        // Center card (fully visible and prominent)
        carouselState.centerItem?.let { item ->
            ProfileCard(
                item = item,
                position = CardPosition.Center,
                onClick = onProfileClick,
                modifier = Modifier.zIndex(1f)
            )
        }

        // Right card (partially visible)
        carouselState.rightItem?.let { item ->
            ProfileCard(
                item = item,
                position = CardPosition.Right,
                onClick = onProfileClick,
                modifier = Modifier
                    .offset(x = 200.dp)
                    .width(200.dp) // Partially visible
            )
        }
    }
}