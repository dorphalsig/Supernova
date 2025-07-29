package com.supernova.ui.splash

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.supernova.sync.OneShotSyncWorker
import com.supernova.ui.UiState
import com.supernova.ui.navigation.NavigationEvent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Splash screen. Triggers a one-shot sync and navigates
 * based on the result.
 */
class SplashViewModel(
    private val context: Context,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<Unit>>(UiState.Loading)
    val state: StateFlow<UiState<Unit>> = _state

    private val _events = MutableSharedFlow<NavigationEvent>()
    val events: SharedFlow<NavigationEvent> = _events

    init {
        viewModelScope.launch(dispatcher) {
            val result = OneShotSyncWorker.trigger(context)
            if (result.isSuccess) {
                _state.value = UiState.Success(Unit)
                _events.emit(NavigationEvent.NavigateToHome)
            } else {
                _state.value = UiState.Error(result.exceptionOrNull()!!)
            }
        }
    }
}
