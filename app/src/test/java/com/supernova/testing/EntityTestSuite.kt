package com.supernova.testing

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.supernova.data.database.SupernovaDatabase
import org.junit.After
import org.junit.Before

/**
 * Base class for Room entity tests.
 *
 * Provides an in-memory [SupernovaDatabase] instance for DAO access. The
 * database is created before each test and closed afterwards.
 */
abstract class EntityTestSuite {

    protected lateinit var db: SupernovaDatabase

    /** Create the in-memory database for tests. */
    @Before
    open fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, SupernovaDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    /** Close the database after every test. */
    @After
    open fun tearDown() {
        db.close()
    }
}
