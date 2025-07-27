package com.supernova.ui.navigation

sealed class SupernovaDestinations(val route: String) {
    object Home : SupernovaDestinations("home")
    object Details : SupernovaDestinations("details")
}
