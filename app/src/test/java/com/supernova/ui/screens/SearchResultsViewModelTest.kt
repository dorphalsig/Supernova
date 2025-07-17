package com.supernova.ui.screens

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SearchResultsViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Test
    fun queryFiltersResults() = runTest {
        val vm = SearchResultsViewModel()
        vm.onQueryChanged("in")
        assertTrue(vm.movies.value.isNotEmpty())
        assertTrue(vm.topResults.value.isNotEmpty())
        assertEquals("in", vm.query.value)
    }
}
