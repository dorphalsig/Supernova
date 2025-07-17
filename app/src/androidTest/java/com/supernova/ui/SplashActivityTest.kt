package com.supernova.ui

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.supernova.data.database.SupernovaDatabase
import com.supernova.utils.SecureDataStore
import com.supernova.utils.SecureStorageKeys
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class SplashActivityTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    fun setup() {
        SecureDataStore.init(context)
        context.deleteDatabase("supernova")
    }

    @After
    fun tearDown() {
        context.deleteDatabase("supernova")
    }

    @Test
    fun navigationDelayIsAtLeastThreeSeconds() = runTest {
        SecureDataStore.putBoolean(SecureStorageKeys.IS_CONFIGURED, false)
        SecureDataStore.putBoolean(SecureStorageKeys.LAST_SYNC_SUCCESS, false)
        ActivityScenario.launch(SplashActivity::class.java).use { scenario ->
            Thread.sleep(2000)
            scenario.onActivity { assertFalse(it.isFinishing) }
            Thread.sleep(1000)
            scenario.onActivity { assertTrue(it.isFinishing) }
        }
    }

    @Test
    fun firstLaunchNavigatesToConfiguration() = runTest {
        SecureDataStore.putBoolean(SecureStorageKeys.IS_CONFIGURED, false)
        SecureDataStore.putBoolean(SecureStorageKeys.LAST_SYNC_SUCCESS, false)
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val monitor = instrumentation.addMonitor(ConfigurationActivity::class.java.name, null, false)
        ActivityScenario.launch(SplashActivity::class.java)
        val next = instrumentation.waitForMonitorWithTimeout(monitor, 4000)
        assertEquals(ConfigurationActivity::class.java.name, next!!.javaClass.name)
        next.finish()
    }

    @Test
    fun configuredWithoutProfilesNavigatesToProfileCreation() = runTest {
        SecureDataStore.putBoolean(SecureStorageKeys.IS_CONFIGURED, true)
        SecureDataStore.putBoolean(SecureStorageKeys.LAST_SYNC_SUCCESS, true)
        val db = SupernovaDatabase.getDatabase(context)
        db.profileDao().deleteProfileById(1)
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val monitor = instrumentation.addMonitor(ProfileCreationActivity::class.java.name, null, false)
        ActivityScenario.launch(SplashActivity::class.java)
        val next = instrumentation.waitForMonitorWithTimeout(monitor, 4000)
        assertEquals(ProfileCreationActivity::class.java.name, next!!.javaClass.name)
        next.finish()
    }

    @Test
    fun configuredWithProfilesNavigatesToProfileSelection() = runTest {
        SecureDataStore.putBoolean(SecureStorageKeys.IS_CONFIGURED, true)
        SecureDataStore.putBoolean(SecureStorageKeys.LAST_SYNC_SUCCESS, true)
        val db = SupernovaDatabase.getDatabase(context)
        db.profileDao().insertProfile(com.supernova.data.TestEntityFactory.profile(name = "Test"))
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val monitor = instrumentation.addMonitor(ProfileSelectionActivity::class.java.name, null, false)
        ActivityScenario.launch(SplashActivity::class.java)
        val next = instrumentation.waitForMonitorWithTimeout(monitor, 4000)
        assertEquals(ProfileSelectionActivity::class.java.name, next!!.javaClass.name)
        next.finish()
    }
}
