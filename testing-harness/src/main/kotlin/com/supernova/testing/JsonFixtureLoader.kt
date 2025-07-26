package com.supernova.testing

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject

/**
 * Utility for loading JSON fixtures from test resources.
 * Fixtures are cached to avoid redundant I/O during test runs.
 */
open class JsonFixtureLoader {
    private val _cache = mutableMapOf<String, String>()
    val cache: Map<String, String> get() = _cache

    suspend fun loadRaw(filename: String): String {
        return _cache.getOrPut(filename) {
            val resource = this::class.java.classLoader.getResource("fixtures/$filename")
                ?: throw IllegalArgumentException("Fixture not found: $filename")
            resource.readText()
        }
    }

    suspend fun loadAsMap(filename: String): Map<String, JsonElement> {
        val rawJson = loadRaw(filename)
        return Json.parseToJsonElement(rawJson).jsonObject
    }

    suspend fun loadJsonFixture(filename: String): String {
        return loadRaw(filename)
    }

    suspend fun validateFixture(filename: String, requiredKeys: Set<String>): Boolean {
        return try {
            val map = loadAsMap(filename)
            requiredKeys.all { key -> map.containsKey(key) }
        } catch (e: Exception) {
            false
        }
    }

    fun validateJson(jsonString: String, requiredKeys: Set<String>): Boolean {
        return try {
            val jsonElement = Json.parseToJsonElement(jsonString)
            val jsonObject = jsonElement.jsonObject
            requiredKeys.all { key -> jsonObject.containsKey(key) }
        } catch (e: Exception) {
            false
        }
    }
}
