package com.supernova.domain.mapper

import com.supernova.domain.model.Category
import com.supernova.domain.model.Stream
import com.supernova.domain.model.Program
import com.supernova.network.dto.CategoryDto
import com.supernova.network.dto.StreamDto
import com.supernova.network.dto.ProgramDto
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

private val PROGRAM_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

fun CategoryDto.toDomain(): Category = Category(
    id = category_id,
    name = category_name
)

fun StreamDto.toDomain(): Stream = Stream(
    id = stream_id,
    name = name,
    categoryId = category_id,
    streamType = stream_type
)

fun ProgramDto.toDomain(): Program {
    val startTime = parseDate(start)
    val endTime = parseDate(end)
    return Program(
        id = id,
        epgChannelId = epg_channel_id,
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
