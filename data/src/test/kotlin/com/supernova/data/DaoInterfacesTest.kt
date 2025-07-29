package com.supernova.data

import android.content.Context
import com.supernova.testing.BaseRoomTest
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertTrue

private val fakeCtx = mockk<Context>(relaxed = true)
class DaoInterfacesTest : BaseRoomTest<SupernovaDatabase>(fakeCtx) {

    override val databaseClass = SupernovaDatabase::class

    override fun initDaos(db: SupernovaDatabase) {
        // no-op
    }

    @Test
    fun daos_areInterfaces() {
        assertTrue(CategoryDao::class.java.isInterface)
        assertTrue(StreamDao::class.java.isInterface)
        assertTrue(ProgramDao::class.java.isInterface)
    }
}
