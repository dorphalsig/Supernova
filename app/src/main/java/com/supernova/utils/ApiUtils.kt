package com.supernova.utils

/**
 * Utility functions for handling inconsistent API responses
 */
object ApiUtils {

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
            is String -> this.toIntSafely()
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
        return try {
            when {
                this.isNullOrBlank() -> null
                this.length == 10 -> this.toLongOrNull() // Unix timestamp in seconds
                this.length == 13 -> this.toLongOrNull() // Unix timestamp in milliseconds
                else -> this.toLongOrNull() // Try direct conversion
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Validates and normalizes URLs
     */
    fun String?.normalizeUrl(): String? {
        return try {
            this?.trim()?.takeIf { it.isNotBlank() }?.let { url ->
                when {
                    url.startsWith("http://") || url.startsWith("https://") -> url
                    url.startsWith("//") -> "http:$url"
                    else -> url // Return as-is for relative URLs or other formats
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Safely parses JSON lists from strings, returns empty list if parsing fails
     */
    fun <T> String?.parseJsonList(clazz: Class<T>): List<T> {
        return try {
            if (this.isNullOrBlank()) return emptyList()
            // This would require Gson instance, simplified for now
            emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}