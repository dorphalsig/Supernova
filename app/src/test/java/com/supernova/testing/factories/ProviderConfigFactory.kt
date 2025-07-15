package com.supernova.testing.factories

import com.supernova.data.entities.ProviderConfigEntity

/**
 * Factory for creating [ProviderConfigEntity] instances in tests.
 *
 * Default values ensure valid URLs and non-empty credentials.
 */
object ProviderConfigFactory {
    fun create(
        id: Long = 0L,
        baseUrl: String = "http://example.com",
        username: String = "user",
        password: String = "pass",
        updatedAt: Long = System.currentTimeMillis()
    ): ProviderConfigEntity = ProviderConfigEntity(
        id = id,
        baseUrl = baseUrl,
        username = username,
        password = password,
        updatedAt = updatedAt
    )
}
