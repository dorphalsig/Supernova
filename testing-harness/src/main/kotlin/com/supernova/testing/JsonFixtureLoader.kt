package com.supernova.testing

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

/**
 * Utility for loading JSON fixtures from test resources.
 * Fixtures are cached to avoid redundant I/O during test runs.
 */
class JsonFixtureLoader(
    private val moshi: Moshi = Moshi.Builder().build()
) {

    private val rawCache = mutableMapOf<String, String>()
    private val mapCache = mutableMapOf<String, Map<String, Any>>()

    /** Number of cached fixtures, used in tests. */
    internal val cacheSize: Int
        get() = rawCache.size

    /** Load a fixture as raw JSON text from `src/test/resources/fixtures`. */
    fun loadRaw(name: String): String = rawCache.getOrPut(name) {
        val stream = javaClass.classLoader?.getResource("fixtures/$name")
            ?: throw IllegalArgumentException("Fixture not found: $name")
        stream.readText()
    }

    /** Parse a fixture into a Map for simple inspection and validation. */
    fun loadAsMap(name: String): Map<String, Any> = mapCache.getOrPut(name) {
        parseMap(loadRaw(name))
    }

    /** Helper for XC API-style responses under `fixtures/xc/`. */
    fun loadXcApiResponse(name: String): Map<String, Any> = loadAsMap("xc/$name")

    /** Validate that the fixture contains the provided JSON fields. */
    fun validateFixture(name: String, requiredFields: Set<String>): Boolean =
        runCatching { requiredFields.all { loadAsMap(name).containsKey(it) } }
            .getOrDefault(false)

    /** Validate raw JSON text against the provided schema keys. */
    fun validateJson(json: String, requiredFields: Set<String>): Boolean =
        runCatching { requiredFields.all { parseMap(json).containsKey(it) } }
            .getOrDefault(false)

    private fun parseMap(json: String): Map<String, Any> {
        val type = Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
        return moshi.adapter<Map<String, Any>>(type).fromJson(json)
            ?: throw IllegalStateException("Invalid JSON")
    }
}
