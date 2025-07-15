package com.supernova.testing

import androidx.room.RoomDatabase
import androidx.sqlite.db.SimpleSQLiteQuery

/**
 * Utility helpers for inspecting SQLite query plans during tests.
 */
object DbPerformanceUtils {
    /** Return the raw EXPLAIN QUERY PLAN output strings for the given query. */
    fun explainQueryPlan(db: RoomDatabase, query: String): List<String> {
        val cursor = db.query(SimpleSQLiteQuery("EXPLAIN QUERY PLAN $query"))
        val results = mutableListOf<String>()
        while (cursor.moveToNext()) {
            results.add(cursor.getString(3))
        }
        cursor.close()
        return results
    }
}
