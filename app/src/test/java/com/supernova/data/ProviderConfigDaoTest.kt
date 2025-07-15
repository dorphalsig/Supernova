package com.supernova.data

import org.robolectric.RobolectricTestRunner
import com.supernova.data.dao.ProviderConfigDao
import com.supernova.testing.EntityTestSuite
import com.supernova.testing.AssertionUtils.assertValid
import com.supernova.testing.DbPerformanceUtils.explainQueryPlan
import com.supernova.testing.factories.ProviderConfigFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests for [ProviderConfigDao] CRUD operations and index usage.
 */
@RunWith(RobolectricTestRunner::class)
class ProviderConfigDaoTest : EntityTestSuite() {

    private lateinit var dao: ProviderConfigDao

    @Before
    override fun setUp() {
        super.setUp()
        dao = db.providerConfigDao()
    }

    @Test
    fun insert_and_retrieve_config() = runBlocking {
        val config = ProviderConfigFactory.create()
        val id = dao.insert(config)
        val fromDb = dao.getById(id)!!
        assertEquals(id, fromDb.id)
        assertValid(fromDb)
    }

    @Test
    fun update_and_delete_config() = runBlocking {
        val config = ProviderConfigFactory.create()
        val id = dao.insert(config)
        val updated = config.copy(id = id, baseUrl = "http://new.example.com")
        dao.update(updated)
        assertEquals("http://new.example.com", dao.getById(id)?.baseUrl)
        dao.deleteById(id)
        assertNull(dao.getById(id))
    }

    @Test
    fun get_all_returns_flow() = runBlocking {
        val config1 = ProviderConfigFactory.create()
        val config2 = ProviderConfigFactory.create(baseUrl = "http://two.com")
        dao.insert(config1)
        dao.insert(config2)
        val items = dao.getAll().first()
        assertEquals(2, items.size)
    }

    @Test
    fun timestamp_preserved() = runBlocking {
        val ts = 123456789L
        val id = dao.insert(ProviderConfigFactory.create(updatedAt = ts))
        assertEquals(ts, dao.getById(id)?.updatedAt)
    }

    @Test
    fun query_uses_index() {
        val plan = explainQueryPlan(db, "SELECT * FROM provider_configs WHERE baseUrl = 'http://example.com'")
        assertTrue(plan.any { it.contains("USING INDEX") })
    }
}
