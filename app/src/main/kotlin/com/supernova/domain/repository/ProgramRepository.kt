package com.supernova.domain.repository

import com.supernova.domain.model.Program
import kotlinx.coroutines.flow.Flow

/** Accessor for program data. */
interface ProgramRepository {
    fun nowPlaying(streamId: Int): Flow<Program?>
}
