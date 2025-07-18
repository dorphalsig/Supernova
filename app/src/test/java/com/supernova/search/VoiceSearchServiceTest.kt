package com.supernova.search

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

import kotlin.test.assertEquals

/**
 * Unit tests for [VoiceSearchService].
 */
@OptIn(ExperimentalCoroutinesApi::class)
class VoiceSearchServiceTest {
    private lateinit var provider: VoiceInputProvider
    private lateinit var repo: EnhancedSearchRepository
    private lateinit var service: VoiceSearchService

    @Before
    fun setup() {
        provider = mockk(relaxed = true)
        every { provider.results } returns MutableSharedFlow<String>(replay = 1).apply { tryEmit("hello") }
        val dao = mockk<com.supernova.data.dao.SearchDao>(relaxed = true)
        every { dao.searchAll(any(), any()) } returns emptyList()
        repo = EnhancedSearchRepository(dao, mockk(relaxed = true))
        service = VoiceSearchService(provider, repo)
    }

    @Test
    fun emits_voice_listening() = runTest {
        val state = service.startListening(1).first()
        assertEquals(EnhancedSearchRepository.SearchState.VoiceListening, state)
    }
}
