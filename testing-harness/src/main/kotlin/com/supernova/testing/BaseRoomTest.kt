package com.supernova.testing

import androidx.room.Room
import androidx.room.RoomDatabase
import android.app.Application
import android.content.ContextWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import java.util.concurrent.Executors
import kotlin.reflect.KClass
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Base class for Room DAO tests using an in-memory database.
 */
@OptIn(ExperimentalCoroutinesApi::class)
abstract class BaseRoomTest<T : RoomDatabase> {

    /** Coroutine dispatcher used for database operations. */
    protected open val dispatcher: TestDispatcher = StandardTestDispatcher()
    protected val scope = TestScope(dispatcher)

    /** Database class reference used to build the Room database. */
    protected abstract val databaseClass: KClass<T>

    /** The database instance available to tests. */
    lateinit var db: T
        private set

    /**
     * Override to initialize DAO properties from the created database.
     */
    protected abstract fun initDaos(db: T)

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        val context = object : ContextWrapper(Application()) {
            override fun getSystemService(name: String): Any? = null
            private fun tmp() = java.io.File(System.getProperty("java.io.tmpdir") ?: ".")
            override fun getCacheDir() = tmp()
            override fun getFilesDir() = tmp()
        }
        db = Room.inMemoryDatabaseBuilder(
            context,
            databaseClass.java
        )
            .fallbackToDestructiveMigration()
            .setQueryExecutor(Executors.newSingleThreadExecutor())
            .setTransactionExecutor(Executors.newSingleThreadExecutor())
            .build()
        initDaos(db)
    }

    @AfterEach
    fun tearDown() {
        db.close()
        Dispatchers.resetMain()
    }

    /**
     * Execute a suspending block within the test scope.
     */
    protected fun runBlockingTest(
        timeout: Duration = 10.seconds,
        block: suspend TestScope.() -> Unit
    ) = scope.runTest(timeout) { block() }

    /** Clears all tables in the database. */
    protected fun clearDatabase() {
        db.clearAllTables()
    }
}
