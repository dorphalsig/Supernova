package com.supernova.search

import kotlinx.coroutines.flow.Flow

interface VoiceInputProvider {
    val results: Flow<String>
    fun start()
    fun stop()
}
