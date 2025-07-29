package com.supernova.data

import android.content.Context
import com.supernova.testing.BaseRoomTest
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertNotNull

private val fakeCtx = mockk<Context>(relaxed = true)
class RoomSkeletonSmokeTest : BaseRoomTest<SupernovaDatabase>(fakeCtx) {

    override val databaseClass = SupernovaDatabase::class

    override fun initDaos(db: SupernovaDatabase) {
        // no-op
    }

    @Test
    fun daoGetters_areAccessible() = runBlockingTest {
        assertNotNull(db.categoryDao())
        assertNotNull(db.streamDao())
        assertNotNull(db.programDao())
    }
}
