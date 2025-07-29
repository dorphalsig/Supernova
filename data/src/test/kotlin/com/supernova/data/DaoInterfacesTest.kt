package com.supernova.data

import com.supernova.testing.BaseRoomTest
import kotlin.test.Test
import kotlin.test.assertTrue

class DaoInterfacesTest : BaseRoomTest<SupernovaDatabase>() {

    override val databaseClass = SupernovaDatabase::class

    override fun initDaos(db: SupernovaDatabase) {
        // no-op
    }
    @Test
    fun channelDao_extendsBaseDao() {
        assertTrue(ChannelDao::class.supertypes.any { it.toString().contains("BaseDao") })
    }

    @Test
    fun programDao_extendsBaseDao() {
        assertTrue(ProgramDao::class.supertypes.any { it.toString().contains("BaseDao") })
    }

    @Test
    fun categoryDao_extendsBaseDao() {
        assertTrue(CategoryDao::class.supertypes.any { it.toString().contains("BaseDao") })
    }
}
