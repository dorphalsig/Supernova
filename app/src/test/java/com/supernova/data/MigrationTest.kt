package com.supernova.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.room.migration.Migration
import com.supernova.data.database.SupernovaDatabase
import com.supernova.data.database.MIGRATION_7_8
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertNotNull

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class MigrationTest {
    @Test
    fun migrate7To8() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        Room.inMemoryDatabaseBuilder(context, SupernovaDatabase::class.java)
            .addMigrations(MIGRATION_7_8)
            .build().apply { close() }
        assertNotNull(context)
    }
}
