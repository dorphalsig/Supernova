package com.supernova.testing

import com.supernova.data.entities.ProviderConfigEntity
import org.junit.Assert.assertTrue

/**
 * Collection of common assertions used across entity tests.
 */
object AssertionUtils {
    /** Ensure that a [ProviderConfigEntity] follows validation rules. */
    fun assertValid(config: ProviderConfigEntity) {
        assertTrue(config.baseUrl.startsWith("http"))
        assertTrue(config.username.isNotBlank())
        assertTrue(config.password.isNotBlank())
        assertTrue(config.updatedAt > 0)
    }
}
