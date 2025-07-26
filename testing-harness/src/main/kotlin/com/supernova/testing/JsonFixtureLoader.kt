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
    
    fun loadRaw(name: String): String = rawCache.getOrPut(name) {
        val stream = javaClass.classLoader?.getResource("fixtures/$name")
            ?: throw IllegalArgumentException("Fixture not found: $name")
        stream.readText()
    }
    
    fun loadAsMap(name: String): Map<String, Any> = mapCache.getOrPut(name) {
        parseMap(loadRaw(name))
    }
    
    // ... rest of the comprehensive implementation
}