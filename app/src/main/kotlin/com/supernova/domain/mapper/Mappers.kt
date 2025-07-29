package com.supernova.domain.mapper

import com.supernova.domain.model.Category
import com.supernova.domain.model.Stream
import com.supernova.domain.model.Program
import com.supernova.network.dto.CategoryDto
import com.supernova.network.dto.StreamDto
import com.supernova.network.dto.ProgramDto
import com.supernova.data.CategoryEntity
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

private val PROGRAM_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

fun CategoryDto.toDomain(): Category = Category(
    id = categoryId,
    name = categoryName
)

fun StreamDto.toDomain(): Stream = Stream(
    id = streamId,
    name = name,
    categoryId = categoryId,
    streamType = streamType
)

fun ProgramDto.toDomain(): Program {
    val startTime = parseDate(start)
    val endTime = parseDate(end)
    return Program(
        id = id,
        epgChannelId = epgChannelId,
        start = startTime,
        end = endTime,
        title = title,
        description = description
    )
}

private fun parseDate(value: String): LocalDateTime = try {
    LocalDateTime.parse(value, PROGRAM_FORMATTER)
} catch (_: DateTimeParseException) {
    LocalDateTime.MIN
}

fun CategoryEntity.toDomain(): Category = Category(
    id = id,
    name = name
)
