package com.supernova.data.dao

import androidx.room.*
import com.supernova.data.entities.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Query("SELECT * FROM category WHERE type = :type ORDER BY name ASC")
    fun getCategoriesByType(type: String): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM category WHERE type = :type AND parent_id = :parentId ORDER BY name ASC")
    fun getCategoriesByTypeAndParent(type: String, parentId: Int): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM category WHERE type = :type AND id = :id")
    suspend fun getCategoryById(type: String, id: Int): CategoryEntity?

    @Query("SELECT * FROM category WHERE type = :type AND parent_id = 0 ORDER BY name ASC")
    fun getRootCategories(type: String): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Delete
    suspend fun deleteCategory(category: CategoryEntity)

    @Query("DELETE FROM category WHERE type = :type")
    suspend fun deleteCategoriesByType(type: String)

    @Query("DELETE FROM category WHERE type = :type AND id = :id")
    suspend fun deleteCategoryById(type: String, id: Int)

    @Query("SELECT COUNT(*) FROM category WHERE type = :type")
    suspend fun getCategoryCount(type: String): Int
}