package com.supernova.ui

import android.content.Context
import android.os.Looper
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.supernova.data.database.SupernovaDatabase
import com.supernova.data.entities.ProfileEntity
import com.supernova.utils.SecureDataStore
import com.supernova.utils.SecureStorageKeys
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import java.time.Duration
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class SplashActivityTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
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
        val controller = Robolectric.buildActivity(SplashActivity::class.java).setup()
        val shadowLooper = Shadows.shadowOf(Looper.getMainLooper())
        shadowLooper.idleFor(Duration.ofSeconds(2))
        assertNull(Shadows.shadowOf(controller.get()).nextStartedActivity)
        shadowLooper.idleFor(Duration.ofSeconds(1))
        assertEquals(
            ConfigurationActivity::class.java.name,
            Shadows.shadowOf(controller.get()).nextStartedActivity!!.component!!.className
        )
    }

    @Test
    fun firstLaunchNavigatesToConfiguration() = runTest {
        SecureDataStore.putBoolean(SecureStorageKeys.IS_CONFIGURED, false)
        SecureDataStore.putBoolean(SecureStorageKeys.LAST_SYNC_SUCCESS, false)
        val controller = Robolectric.buildActivity(SplashActivity::class.java).setup()
        Shadows.shadowOf(Looper.getMainLooper()).idleFor(Duration.ofSeconds(3))
        assertEquals(
            ConfigurationActivity::class.java.name,
            Shadows.shadowOf(controller.get()).nextStartedActivity!!.component!!.className
        )
    }

    @Test
    fun configuredWithoutProfilesNavigatesToProfileCreation() = runTest {
        SecureDataStore.putBoolean(SecureStorageKeys.IS_CONFIGURED, true)
        SecureDataStore.putBoolean(SecureStorageKeys.LAST_SYNC_SUCCESS, true)
        val db = SupernovaDatabase.getDatabase(context)
        db.profileDao().deleteProfileById(1)
        val controller = Robolectric.buildActivity(SplashActivity::class.java).setup()
        Shadows.shadowOf(Looper.getMainLooper()).idleFor(Duration.ofSeconds(3))
        assertEquals(
            ProfileCreationActivity::class.java.name,
            Shadows.shadowOf(controller.get()).nextStartedActivity!!.component!!.className
        )
    }

    @Test
    fun configuredWithProfilesNavigatesToProfileSelection() = runTest {
        SecureDataStore.putBoolean(SecureStorageKeys.IS_CONFIGURED, true)
        SecureDataStore.putBoolean(SecureStorageKeys.LAST_SYNC_SUCCESS, true)
        val db = SupernovaDatabase.getDatabase(context)
        db.profileDao().insertProfile(ProfileEntity(name = "Test"))
        val controller = Robolectric.buildActivity(SplashActivity::class.java).setup()
        Shadows.shadowOf(Looper.getMainLooper()).idleFor(Duration.ofSeconds(3))
        assertEquals(
            ProfileSelectionActivity::class.java.name,
            Shadows.shadowOf(controller.get()).nextStartedActivity!!.component!!.className
        )
    }
}
