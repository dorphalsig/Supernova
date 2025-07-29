package com.supernova.data

import com.supernova.domain.repository.CategoryRepository
import com.supernova.domain.mapper.toDomain
import com.supernova.testing.BaseRoomTest
import io.mockk.mockk
import io.mockk.every
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

private val fakeCtx = mockk<android.content.Context>(relaxed = true) {
    every { filesDir } returns File("build/tmp")
    every { cacheDir } returns File("build/tmp")
}
class RoomCategoryRepositoryTest : BaseRoomTest<SupernovaDatabase>(fakeCtx) {

    private lateinit var dao: CategoryDao
    private lateinit var repository: CategoryRepository

    override val databaseClass = SupernovaDatabase::class

    override fun initDaos(db: SupernovaDatabase) {
        dao = db.categoryDao()
        repository = RoomCategoryRepository(dao)
    }

    @Test
    fun flowEmitsMappedCategories() = runBlockingTest {
        val rows = listOf(
            CategoryEntity(id = 1, name = "News"),
            CategoryEntity(id = 2, name = "Sports")
        )
        dao.upsertAll(rows)

        val emitted = repository.getCategories().first()
        assertEquals(rows.map { it.toDomain() }, emitted)
    }
}
