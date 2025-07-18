package com.supernova.testing

import android.content.Context
import androidx.room.Room
import io.mockk.every
import io.mockk.mockk
import com.supernova.data.database.SupernovaDatabase
import com.supernova.data.entities.MovieEntity
import com.supernova.data.entities.StreamEntity
import org.junit.After
import org.junit.Before

/**
 * Base class for Room entity tests.
 *
 * Provides an in-memory [SupernovaDatabase] instance configured for the latest
 * [StreamEntity] and [MovieEntity] schemas. The database is created before each
 * test and closed afterwards.
 */
abstract class EntityTestSuite {

    protected lateinit var db: SupernovaDatabase

    /** Create the in-memory database for tests. */
    @Before
    open fun setUp() {
        // Mock a minimal Context for Room
        val context = mockk<Context>(relaxed = true)
        every { context.applicationContext } returns context
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
