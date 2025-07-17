package com.supernova.data

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import android.content.Context
import androidx.room.Room
import com.supernova.data.database.SupernovaDatabase
import com.supernova.data.dao.CategoryDao
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@OptIn(ExperimentalCoroutinesApi::class)
class CategoryDaoTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private lateinit var db: SupernovaDatabase
    private lateinit var dao: CategoryDao

    @Before
    fun setup() {
        val context = mockk<Context>(relaxed = true)
        every { context.applicationContext } returns context
        db = Room.inMemoryDatabaseBuilder(context, SupernovaDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.categoryDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insertAndReplaceCategoryWithCompositeKey() = runTest {
        val category = TestEntityFactory.category(type = "movie", id = 1)
        dao.insertCategory(category)
        val updated = category.copy(name = "Updated")
        dao.insertCategory(updated)
        val loaded = dao.getCategoryById("movie", 1)
        assertNotNull(loaded)
        assertEquals("Updated", loaded!!.name)
    }
}
