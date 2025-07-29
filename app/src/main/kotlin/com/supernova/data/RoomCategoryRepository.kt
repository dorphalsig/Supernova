package com.supernova.data

import com.supernova.domain.model.Category
import com.supernova.domain.repository.CategoryRepository
import com.supernova.domain.mapper.toDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomCategoryRepository(
    private val categoryDao: CategoryDao
) : CategoryRepository {

    override fun getCategories(): Flow<List<Category>> =
        categoryDao.getAll().map { entities ->
            entities.map { it.toDomain() }
        }
}
