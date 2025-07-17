package com.supernova.testing

import android.content.Context
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.supernova.data.database.SupernovaDatabase
import org.junit.After
import org.junit.Before

/**
 * Base class for instrumented Room tests using a real in-memory database.
 */
abstract class InstrumentedTestSuite {

    protected lateinit var db: SupernovaDatabase

    @Before
    open fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, SupernovaDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    open fun tearDown() {
        db.close()
    }
}

