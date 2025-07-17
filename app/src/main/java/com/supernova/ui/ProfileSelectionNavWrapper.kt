package com.supernova.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.supernova.data.database.SupernovaDatabase
import com.supernova.ui.navigation.NavRoutes
import com.supernova.ui.screens.ProfileSelectionScreen
import com.supernova.ui.screens.ProfileSelectionViewModel
import com.supernova.ui.screens.ProfileSelectionViewModelFactory

@Composable
fun ProfileSelectionNavWrapper(navController: NavController) {
    val db = SupernovaDatabase.getDatabase(navController.context)
    val factory = ProfileSelectionViewModelFactory(db)
    ProfileSelectionScreen(
        onProfileSelected = { navController.navigate(NavRoutes.Home) },
        onAddProfile = { navController.navigate(NavRoutes.ProfileCreation) },
        viewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = factory)
    )
}
