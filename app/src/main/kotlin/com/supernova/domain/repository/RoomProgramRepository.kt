package com.supernova.domain.repository

import com.supernova.data.ProgramDao
import com.supernova.domain.model.Program
import com.supernova.domain.mapper.toDomain
import com.supernova.util.Clock
import com.supernova.util.SystemClock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/** Room-backed implementation of [ProgramRepository]. */
class RoomProgramRepository(
    private val dao: ProgramDao,
    private val clock: Clock = SystemClock
) : ProgramRepository {

    override fun nowPlaying(streamId: Int): Flow<Program?> = flow {
        val now = clock.currentTimeMillis()
        emit(dao.nowPlaying(streamId, now)?.toDomain())
    }
}
