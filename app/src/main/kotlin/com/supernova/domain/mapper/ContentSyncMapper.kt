package com.supernova.domain.mapper

import com.supernova.data.CategoryEntity
import com.supernova.data.StreamEntity
import com.supernova.data.ProgramEntity
import com.supernova.network.dto.CategoryDto
import com.supernova.network.dto.StreamDto
import com.supernova.network.dto.ProgramDto

/**
 * Maps network DTOs to their Room entity counterparts for content sync.
 */
fun CategoryDto.toEntity(): CategoryEntity = CategoryEntity(
    id = categoryId,
    name = categoryName
)

fun StreamDto.toEntity(catId: Int): StreamEntity = StreamEntity(
    id = streamId,
    name = name,
    categoryId = catId,
    streamType = streamType
)

fun ProgramDto.toEntity(streamId: Int): ProgramEntity = ProgramEntity(
    id = id,
    epgChannelId = epgChannelId,
    start = start,
    end = end,
    title = title,
    description = description
)
