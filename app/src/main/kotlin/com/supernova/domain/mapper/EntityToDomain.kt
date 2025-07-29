package com.supernova.domain.mapper

import com.supernova.data.ProgramEntity
import com.supernova.domain.model.Program
import java.time.Instant
import java.time.ZoneOffset

fun ProgramEntity.toDomain(): Program = Program(
    id = id,
    epgChannelId = epgChannelId,
    start = Instant.ofEpochMilli(start).atZone(ZoneOffset.UTC).toLocalDateTime(),
    end = Instant.ofEpochMilli(end).atZone(ZoneOffset.UTC).toLocalDateTime(),
    title = title,
    description = description
)
