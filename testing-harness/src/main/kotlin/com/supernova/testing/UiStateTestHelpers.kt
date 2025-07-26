package com.supernova.testing

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.yield
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take

/**
 * Simple sealed class representing typical UI states for Compose screens.
 */
sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val throwable: Throwable) : UiState<Nothing>()
}

/** Utility helpers for testing UiState and focus/navigation behaviour. */
object UiStateTestHelpers {
    /** Assert that the state is [UiState.Loading]. */
    fun <T> assertLoading(state: UiState<T>) {
        check(state is UiState.Loading) { "Expected Loading, got $state" }
    }

    /** Assert that the state is [UiState.Success] and run [block] on the data. */
    fun <T> assertSuccess(state: UiState<T>, block: (T) -> Unit = {}) {
        check(state is UiState.Success<T>) { "Expected Success, got $state" }
        block(state.data)
    }

    /** Assert that the state is [UiState.Error] and run [block] on the error. */
    fun <T> assertError(state: UiState<T>, block: (Throwable) -> Unit = {}) {
        check(state is UiState.Error) { "Expected Error, got $state" }
        block(state.throwable)
    }

    /** Collect exactly [count] states from the flow for assertions. */
    suspend fun <T> Flow<UiState<T>>.collectStates(count: Int): List<UiState<T>> {
        val results = mutableListOf<UiState<T>>()
        take(count).collect { results.add(it) }
        return results
    }

    /**
     * Minimal ViewModel-like container for testing state producers without
     * depending on AndroidX ViewModel classes.
     */
    abstract class TestViewModel<S>(initial: UiState<S>) {
        private val _state = MutableStateFlow(initial)
        val state: StateFlow<UiState<S>> = _state

        protected fun setState(newState: UiState<S>) { _state.value = newState }

        suspend fun test(block: suspend TestViewModel<S>.() -> Unit): List<UiState<S>> = coroutineScope {
            val collected = mutableListOf<UiState<S>>()
            val collector = launch { state.collect { collected += it } }
            yield()
            block()
            yield()
            collector.cancelAndJoin()
            collected
        }
    }

    /** Simple focus state tracker for D-pad navigation tests. */
    class FocusState(initialIndex: Int = 0) {
        private val _index = MutableStateFlow(initialIndex)
        val index: StateFlow<Int> = _index

        fun moveNext(max: Int) {
            _index.value = (_index.value + 1).coerceAtMost(max - 1)
        }

        fun movePrevious() {
            _index.value = (_index.value - 1).coerceAtLeast(0)
        }
    }

    /** Records navigation routes for verification in tests. */
    class NavigationTracker {
        private val _history = mutableListOf<String>()
        val history: List<String> get() = _history

        fun navigate(route: String) {
            _history += route
        }
    }
}
