package com.supernova.ui.navigation

/** Events emitted by ViewModels to drive navigation. */
sealed class NavigationEvent {
    object NavigateToHome : NavigationEvent()
}
