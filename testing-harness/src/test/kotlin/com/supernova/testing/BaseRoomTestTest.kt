package com.supernova.testing

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlin.test.assertEquals
import org.junit.jupiter.api.Test

@Entity
data class TestItem(
    @PrimaryKey val id: Int,
    val name: String
)

@Dao
interface TestDao {
    @Insert
    suspend fun insert(item: TestItem)

    @Query("SELECT * FROM TestItem")
    suspend fun all(): List<TestItem>
}

@Database(entities = [TestItem::class], version = 1, exportSchema = false)
abstract class TestDb : RoomDatabase() {
    abstract fun dao(): TestDao
}

class BaseRoomTestTest : BaseRoomTest<TestDb>() {
    lateinit var dao: TestDao

    override val databaseClass = TestDb::class

    override fun initDaos(db: TestDb) {
        dao = db.dao()
    }

    @Test
    fun insertAndQuery() = runBlockingTest {
        dao.insert(TestItem(1, "hello"))
        val list = dao.all()
        assertEquals(1, list.size)
        assertEquals("hello", list.first().name)
    }

    @Test
    fun clearDatabaseRemovesData() = runBlockingTest {
        dao.insert(TestItem(2, "bye"))
        clearDatabase()
        val list = dao.all()
        assertEquals(0, list.size)
    }
}
