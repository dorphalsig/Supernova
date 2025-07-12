package com.supernova.data.dao

import androidx.room.*
import com.supernova.data.entities.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Query("SELECT * FROM category WHERE type = :type AND is_live = 1 ORDER BY name ASC")
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

    // --- DML versioning helpers ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategoriesStaging(categories: List<CategoryEntity>)

    @Query(
        "INSERT INTO category(type, id, name, parent_id, is_live) " +
            "SELECT type, id, name, parent_id, 0 FROM category " +
            "WHERE type = :type AND (:categoryId IS NULL OR id = :categoryId) AND is_live = 1"
    )
    suspend fun copyFromLive(type: String, categoryId: Int?)

    @Query("DELETE FROM category WHERE is_live = 0")
    suspend fun deleteStaging()

    @Query("DELETE FROM category WHERE is_live = 1")
    suspend fun deleteLive()

    @Query("UPDATE category SET is_live = 1 WHERE is_live = 0")
    suspend fun promoteStaging()

    @Transaction
    suspend fun swapStagingToLive() {
        deleteLive()
        promoteStaging()
    }
}