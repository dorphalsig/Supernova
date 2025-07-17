package com.supernova.search

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

class VoiceSearchService @Inject constructor(
    private val provider: VoiceInputProvider,
    private val repository: EnhancedSearchRepository
) {
    fun startListening(userId: Int): Flow<EnhancedSearchRepository.SearchState> {
        return provider.results
            .flatMapLatest { query -> repository.search(kotlinx.coroutines.flow.flowOf(query), userId) }
            .onStart { emit(EnhancedSearchRepository.SearchState.VoiceListening) }
    }

    fun begin() = provider.start()
    fun stop() = provider.stop()
}
