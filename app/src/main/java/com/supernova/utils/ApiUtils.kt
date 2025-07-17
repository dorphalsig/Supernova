package com.supernova.utils

import android.util.Log
import android.util.Xml
import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.MalformedJsonException
import com.supernova.data.entities.ChannelEntity
import com.supernova.data.entities.EpgEntity
import com.supernova.network.DataSyncService
import okhttp3.ResponseBody
import org.xmlpull.v1.XmlPullParser
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Utility functions for handling inconsistent API responses
 */
object ApiUtils {


    const val DEFAULT_BATCH_SIZE = 100

    @PublishedApi
    internal const val TAG = "BatchStream"

    @PublishedApi
    internal val gson: Gson = Gson()

    /**
     * Safely converts string to Int, returns null if conversion fails
     */
    fun String?.toIntSafely(): Int? {
        return try {
            this?.trim()?.takeIf { it.isNotBlank() }?.toIntOrNull()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Safely converts string to Long, returns null if conversion fails
     */
    fun String?.toLongSafely(): Long? {
        return try {
            this?.trim()?.takeIf { it.isNotBlank() }?.toLongOrNull()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Safely converts string to Float, returns null if conversion fails
     */
    fun String?.toFloatSafely(): Float? {
        return try {
            this?.trim()?.takeIf { it.isNotBlank() }?.toFloatOrNull()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Returns the string if it's not null, empty, or just whitespace
     * Otherwise returns null
     */
    fun String?.takeIfNotBlank(): String? {
        return this?.trim()?.takeIf { it.isNotBlank() }
    }

    /**
     * Returns the string if it's not null, empty, or just whitespace
     * Otherwise returns the provided default value
     */
    fun String?.orDefault(default: String): String {
        return this?.trim()?.takeIf { it.isNotBlank() } ?: default
    }

    /**
     * Safely converts any number type to Int, returns null if conversion fails
     */
    fun Any?.toIntSafely(): Int? {
        return when (this) {
            is Int -> this
            is Long -> this.toInt()
            is Float -> this.toInt()
            is Double -> this.toInt()
            is String -> this.toIntOrNull()
            else -> null
        }
    }

    /**
     * Safely converts any number type to Long, returns null if conversion fails
     */
    fun Any?.toLongSafely(): Long? {
        return when (this) {
            is Long -> this
            is Int -> this.toLong()
            is Float -> this.toLong()
            is Double -> this.toLong()
            is String -> this.toLongSafely()
            else -> null
        }
    }

    /**
     * Handles common API timestamp formats and converts to Long
     */
    fun String?.parseTimestamp(): Long? {
        if (this.isNullOrBlank()) return null

        return when (this.length) {
            10, 13 -> this.toLongOrNull()
            else -> this.toLongOrNull()
        }
    }

    /**
     * Validates and normalizes URLs
     */
    fun String?.normalizeUrl(): String? {
        return this?.trim()?.takeIf { it.isNotBlank() }?.let { url ->
            when {
                url.startsWith("http://") || url.startsWith("https://") -> url
                url.startsWith("//") -> "http:$url"
                else -> url
            }
        }
    }


    fun parseXmlTvTime(value: String?): Long? {
        val epgDateFormat = SimpleDateFormat("yyyyMMddHHmmss Z", Locale.US)
        return try {
            value?.let { epgDateFormat.parse(it)?.time }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Normalize search queries using simple lowercase + punctuation stripping.
     */
    fun normalizeSearchQuery(query: String): String {
        return query.lowercase(Locale.getDefault())
            .replace(Regex("[^a-z0-9 ]"), " ")
            .replace("\\s+".toRegex(), " ")
            .trim()
    }

    /**
     * Stream parse JSON array from response body in batches.
     * Balances memory efficiency with database performance.
     *
     * @param responseBody The response body containing JSON array
     * @param batchSize Number of items to accumulate before processing (default 100)
     * @param onBatch Callback to process each batch of items
     * @return Total number of items processed
     */
    suspend inline fun <reified T> batchJsonStream(
        dataSyncService: DataSyncService, responseBody: ResponseBody,
        batchSize: Int = DEFAULT_BATCH_SIZE,
        onBatch: suspend (List<T>) -> Unit
    ): Int {
        var totalProcessed = 0
        var currentBatch = mutableListOf<T>()

        responseBody.use { body ->
            JsonReader(body.charStream()).use { reader ->
                while (reader.hasNext()) {
                    try {
                        when (reader.peek()) {
                            JsonToken.BEGIN_OBJECT -> {
                                val item = gson.fromJson<T>(reader, T::class.java)
                                currentBatch.add(item)

                                // Process batch when it reaches the specified size
                                if (currentBatch.size >= batchSize) {
                                    onBatch(currentBatch)
                                    totalProcessed += currentBatch.size
                                    currentBatch = mutableListOf()
                                }
                            }

                            JsonToken.NULL -> reader.skipValue()
                            else -> reader.skipValue()
                        }
                    } catch (e: Exception) {
                        Log.e(
                            TAG,
                            "Error parsing item at position ${totalProcessed + currentBatch.size}, skipping",
                            e
                        )
                        try {
                            reader.skipValue()
                        } catch (skipError: Exception) {
                            Log.e(
                                TAG,
                                "Error skipping malformed item",
                                skipError
                            )
                        }
                    }
                }

                reader.endArray()

                // Process any remaining items in the last batch
                if (currentBatch.isNotEmpty()) {
                    onBatch(currentBatch)
                    totalProcessed += currentBatch.size
                }
            }
        }

        Log.d(
            TAG,
            "Finished parsing. Total items processed: $totalProcessed"
        )
        return totalProcessed
    }

    suspend fun batchXmlStream(
        dataSyncService: DataSyncService, responseBody: ResponseBody,
        batchSize: Int = DEFAULT_BATCH_SIZE,
        insertChannels: suspend (List<ChannelEntity>) -> Unit,
        insertPrograms: suspend (List<EpgEntity>) -> Unit
    ) {
        val parser = Xml.newPullParser().apply {
            setInput(responseBody.byteStream(), null)
        }

        var event = parser.eventType
        var currentProgram: EpgEntity? = null
        var currentChannelId: String? = null
        var currentChannelName: String? = null
        val programBatch = mutableListOf<EpgEntity>()
        val channelBatch = mutableListOf<ChannelEntity>()

        while (event != XmlPullParser.END_DOCUMENT) {
            when (event) {
                XmlPullParser.START_TAG -> when (parser.name) {
                    "channel" -> {
                        currentChannelId = parser.getAttributeValue(null, "id")
                        currentChannelName = null
                    }

                    "display-name" -> if (currentChannelId != null) {
                        currentChannelName = parser.nextText()
                    }

                    "programme" -> {
                        val ch = parser.getAttributeValue(null, "channel") ?: ""
                        val start =
                            parseXmlTvTime(parser.getAttributeValue(null, "start"))
                        val end =
                            parseXmlTvTime(parser.getAttributeValue(null, "stop"))
                        currentProgram = if (start != null && end != null) {
                            EpgEntity(
                                channel_id = ch,
                                start = start,
                                end = end,
                                title = null,
                                description = null
                            )
                        } else null
                    }

                    "title" -> currentProgram = currentProgram?.copy(title = parser.nextText())
                    "desc" -> currentProgram = currentProgram?.copy(description = parser.nextText())
                }

                XmlPullParser.END_TAG -> when (parser.name) {
                    "channel" -> {
                        currentChannelId?.let { id ->
                            channelBatch += ChannelEntity(id, currentChannelName)
                            currentChannelId = null
                            currentChannelName = null

                            if (channelBatch.size >= batchSize) {
                                insertChannels(channelBatch.toList())
                                channelBatch.clear()
                            }
                        }
                    }

                    "programme" -> {
                        currentProgram?.let { prog ->
                            programBatch += prog
                            currentProgram = null

                            if (programBatch.size >= batchSize) {
                                insertPrograms(programBatch.toList())
                                programBatch.clear()
                            }
                        }
                    }
                }
            }
            event = parser.next()
        }

        if (channelBatch.isNotEmpty()) insertChannels(channelBatch)
        if (programBatch.isNotEmpty()) insertPrograms(programBatch)
    }
}