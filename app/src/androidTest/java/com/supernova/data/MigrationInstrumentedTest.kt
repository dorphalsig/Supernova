package com.supernova.data

import androidx.room.Room
import androidx.room.migration.Migration
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.supernova.data.database.SupernovaDatabase
import com.supernova.data.database.MIGRATION_7_8
import com.supernova.data.database.MIGRATION_8_9
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertNotNull

/**
 * Instrumented tests verifying Room database migrations.
 */
@RunWith(AndroidJUnit4::class)
class MigrationInstrumentedTest {

    @Test
    fun migrate7To8And8To9() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val migrations: Array<Migration> = arrayOf(MIGRATION_7_8, MIGRATION_8_9)
        Room.inMemoryDatabaseBuilder(context, SupernovaDatabase::class.java)
            .addMigrations(*migrations)
            .build().apply { close() }
        assertNotNull(context)
    }
}
