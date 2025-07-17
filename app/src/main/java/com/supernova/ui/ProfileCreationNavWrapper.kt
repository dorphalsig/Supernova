package com.supernova.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.supernova.ui.navigation.NavRoutes

@Composable
fun ProfileCreationNavWrapper(navController: NavController) {
    // Placeholder, navigate to selection on save
    ProfileCreationScreen(
        avatars = emptyList(),
        onNameChange = {},
        onPinChange = {},
        onAvatarSelected = {},
        onSave = { navController.navigate(NavRoutes.ProfileSelection) }
    )
}
