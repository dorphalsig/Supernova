package com.supernova.data


/**
 * Base contract for simple CRUD operations.
 *
 * Subclasses should provide a @Query implementation for [clear] if needed.
 */
abstract class BaseDao<T> {
    /** Insert one or more entities. */
    open suspend fun insert(vararg entities: T) {}

    /** Insert or replace one or more entities. */
    open suspend fun upsert(vararg entities: T) {}

    /** Delete one or more entities. */
    open suspend fun delete(vararg entities: T) {}

    /** Delete all rows from the table. Implementation provided by concrete DAO. */
    open suspend fun clear() {}
}
