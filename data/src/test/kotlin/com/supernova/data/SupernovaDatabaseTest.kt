package com.supernova.data

import com.supernova.testing.BaseRoomTest
import kotlin.test.Test
import kotlin.test.assertNotNull

class SupernovaDatabaseTest : BaseRoomTest<SupernovaDatabase>() {
    override val databaseClass = SupernovaDatabase::class

    override fun initDaos(db: SupernovaDatabase) {
        // No-op: empty database
    }

    @Test
    fun databaseOpens_andProvidesBaseDao() = runBlockingTest {
        assertNotNull(db.baseDao())
    }
}
