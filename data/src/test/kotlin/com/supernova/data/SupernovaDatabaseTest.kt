package com.supernova.data

import android.content.Context
import com.supernova.testing.BaseRoomTest
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertNotNull

private val fakeCtx = mockk<Context>(relaxed = true)
class SupernovaDatabaseTest : BaseRoomTest<SupernovaDatabase>(fakeCtx) {
    override val databaseClass = SupernovaDatabase::class

    override fun initDaos(db: SupernovaDatabase) {
        // No-op: empty database
    }

    @Test
    fun databaseOpens() = runBlockingTest {
        assertNotNull(db)
    }
}
