package com.supernova.testing

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import java.sql.Connection
import java.sql.DriverManager
import kotlin.test.assertTrue

class DbAssertionHelpersTest : TestEntityFactory() {

    private fun createDb(): Connection {
        val conn = DriverManager.getConnection("jdbc:sqlite::memory:")
        conn.createStatement().use { stmt ->
            stmt.execute("PRAGMA foreign_keys=ON")
            stmt.execute("CREATE TABLE categories(id INTEGER PRIMARY KEY, name TEXT)")
            stmt.execute(
                """CREATE TABLE streams(
                    id INTEGER PRIMARY KEY,
                    title TEXT,
                    categoryId INTEGER,
                    streamType TEXT,
                    isLive INTEGER,
                    number INTEGER,
                    FOREIGN KEY(categoryId) REFERENCES categories(id) ON DELETE CASCADE
                )"""
            )
            stmt.execute("CREATE VIRTUAL TABLE stream_search USING fts4(content='streams', title)")
            stmt.execute("CREATE TRIGGER streams_ai AFTER INSERT ON streams BEGIN INSERT INTO stream_search(rowid,title) VALUES (new.rowid,new.title); END")
            stmt.execute("CREATE TRIGGER streams_ad AFTER DELETE ON streams BEGIN DELETE FROM stream_search WHERE rowid = old.rowid; END")
        }
        return conn
    }

    private fun Connection.count(table: String): Int {
        createStatement().use { stmt ->
            val rs = stmt.executeQuery("SELECT COUNT(*) FROM $table")
            return if (rs.next()) rs.getInt(1) else 0
        }
    }

    private fun Connection.insertCategory(category: Category) {
        prepareStatement("INSERT INTO categories(id,name) VALUES (?,?)").use {
            it.setLong(1, category.id)
            it.setString(2, category.name)
            it.executeUpdate()
        }
    }

    private fun Connection.insertStream(stream: Stream) {
        prepareStatement(
            "INSERT INTO streams(id,title,categoryId,streamType,isLive,number) VALUES (?,?,?,?,?,?)"
        ).use {
            it.setLong(1, stream.id)
            it.setString(2, stream.title)
            it.setLong(3, stream.categoryId)
            it.setString(4, stream.streamType)
            it.setInt(5, if (stream.isLive) 1 else 0)
            it.setInt(6, stream.number)
            it.executeUpdate()
        }
    }

    private fun Connection.queryStream(id: Long): Stream? {
        prepareStatement(
            "SELECT id,title,categoryId,streamType,isLive,number FROM streams WHERE id=?"
        ).use {
            it.setLong(1, id)
            val rs = it.executeQuery()
            return if (rs.next()) {
                Stream(
                    rs.getLong(1),
                    rs.getString(2),
                    rs.getLong(3),
                    rs.getString(4),
                    rs.getInt(5) == 1,
                    rs.getInt(6)
                )
            } else null
        }
    }

    private fun Connection.queryAllStreams(): List<Stream> {
        createStatement().use { stmt ->
            val rs = stmt.executeQuery(
                "SELECT id,title,categoryId,streamType,isLive,number FROM streams ORDER BY id"
            )
            val list = mutableListOf<Stream>()
            while (rs.next()) {
                list += Stream(
                    rs.getLong(1),
                    rs.getString(2),
                    rs.getLong(3),
                    rs.getString(4),
                    rs.getInt(5) == 1,
                    rs.getInt(6)
                )
            }
            return list
        }
    }

    private fun Connection.searchStreams(q: String): List<Stream> {
        prepareStatement("SELECT s.id,s.title,s.categoryId,s.streamType,s.isLive,s.number FROM streams s JOIN stream_search ON s.id=stream_search.rowid WHERE stream_search MATCH ? ORDER BY s.id").use {
            it.setString(1, q)
            val rs = it.executeQuery()
            val list = mutableListOf<Stream>()
            while (rs.next()) {
                list += Stream(rs.getLong(1), rs.getString(2), rs.getLong(3), rs.getString(4), rs.getInt(5) == 1, rs.getInt(6))
            }
            return list
        }
    }

    private fun loadSearchFixture(): String =
        javaClass.classLoader!!.getResource("fixtures/search_results.json")!!.readText()

    @Test
    fun rowCount_entity_list_assertions() = runTest() {
        val db = createDb()
        val category = TestEntityFactory.category()
        val stream = TestEntityFactory.stream(categoryId = category.id)
        db.insertCategory(category)
        db.insertStream(stream)

        DbAssertionHelpers.table("streams") { db.count("streams") }
            .hasRowCount(1)
            .isPopulated()

        val fetched = db.queryStream(stream.id)
        DbAssertionHelpers.entity(fetched).isEqualTo(stream)

        val list = db.queryAllStreams()
        DbAssertionHelpers.list(list).isEqualTo(listOf(stream))

        db.close()
    }

    @Test
    fun search_state_and_cascade() = runTest() {
        val db = createDb()
        val category = TestEntityFactory.category()
        val stream1 = TestEntityFactory.stream(id = 1L, title = "Alpha", categoryId = category.id)
        val stream2 = TestEntityFactory.stream(id = 2L, title = "Beta", categoryId = category.id)
        db.insertCategory(category)
        db.insertStream(stream1)
        db.insertStream(stream2)

        val searchResults = db.searchStreams("Alpha")
        val fixture = loadSearchFixture()
        assertTrue(fixture.isNotEmpty())
        DbAssertionHelpers.search("Alpha", searchResults).matches(listOf(stream1))

        DbAssertionHelpers.table("streams") { db.count("streams") }.isPopulated()

        DbAssertionHelpers.cascade(
            removeParent = {
                db.prepareStatement("DELETE FROM categories WHERE id=?").use {
                    it.setLong(1, category.id)
                    it.executeUpdate()
                }
            },
            childQuery = { db.queryStream(stream1.id) }
        ).cascades()

        DbAssertionHelpers.table("streams") { db.count("streams") }.isEmpty()

        db.close()
    }
}
