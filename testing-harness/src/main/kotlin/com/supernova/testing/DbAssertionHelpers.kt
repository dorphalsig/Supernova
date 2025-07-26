package com.supernova.testing

import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Fluent helpers for verifying database state in unit tests.
 */
object DbAssertionHelpers {

    fun table(name: String, countProvider: () -> Int) = TableScope(name, countProvider)

    fun <T> entity(actual: T) = EntityScope(actual)

    fun <T> list(actual: List<T>) = ListScope(actual)

    fun <T> search(query: String, actual: List<T>) = SearchScope(query, actual)

    fun <T> cascade(removeParent: () -> Unit, childQuery: () -> T?) =
        CascadeScope(removeParent, childQuery)

    class TableScope internal constructor(
        private val name: String,
        private val countProvider: () -> Int
    ) {
        fun hasRowCount(expected: Int): TableScope {
            val actual = countProvider()
            assertEquals(
                expected,
                actual,
                "Table '$name' expected $expected rows but had $actual"
            )
            return this
        }

        fun isEmpty(): TableScope = hasRowCount(0)

        fun isPopulated(): TableScope {
            val actual = countProvider()
            assertTrue(actual > 0, "Expected table '$name' to be populated")
            return this
        }
    }

    class EntityScope<T> internal constructor(private val actual: T) {
        fun isEqualTo(expected: T): EntityScope<T> {
            assertEquals(expected, actual)
            return this
        }
    }

    class ListScope<T> internal constructor(private val actual: List<T>) {
        fun isEqualTo(expected: List<T>): ListScope<T> {
            assertEquals(expected, actual)
            return this
        }
    }

    class SearchScope<T> internal constructor(
        private val query: String,
        private val actual: List<T>
    ) {
        fun matches(expected: List<T>): SearchScope<T> {
            assertEquals(expected, actual, "Search '$query' results mismatch")
            return this
        }
    }

    class CascadeScope<T> internal constructor(
        private val removeParent: () -> Unit,
        private val childQuery: () -> T?
    ) {
        fun cascades(): CascadeScope<T> {
            removeParent()
            val child = childQuery()
            assertNull(child, "Cascade delete failed")
            return this
        }
    }
}
