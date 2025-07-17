package com.supernova.ui.screens

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.room.Room
import com.supernova.data.database.SupernovaDatabase
import com.supernova.utils.SecureDataStore
import com.supernova.utils.SecureStorageKeys
import com.supernova.work.SyncManager
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class SettingsViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private lateinit var context: Context
    private lateinit var db: SupernovaDatabase
    private lateinit var sync: SyncManager

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        SecureDataStore.init(context)
        db = Room.inMemoryDatabaseBuilder(context, SupernovaDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        sync = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun loadsSyncInfoFromSecureStore() = runTest {
        SecureDataStore.putBoolean(SecureStorageKeys.LAST_SYNC_SUCCESS, true)
        SecureDataStore.putLong(SecureStorageKeys.LAST_SYNC_TIME, 100L)
        val vm = SettingsViewModel(db, sync)
        assertTrue(vm.lastSyncSuccess.first()!!)
        assertEquals(100L, vm.lastSyncTime.first())
    }

    @Test
    fun triggerManualSyncCallsManager() = runTest {
        val vm = SettingsViewModel(db, sync)
        vm.triggerManualSync()
        verify { sync.triggerImmediateSync() }
    }
}
