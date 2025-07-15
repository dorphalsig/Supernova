package com.supernova.data

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.room.Room
import com.supernova.data.database.SupernovaDatabase
import com.supernova.data.dao.ProfileDao
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ProfileDaoTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private lateinit var db: SupernovaDatabase
    private lateinit var dao: ProfileDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(context, SupernovaDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.profileDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insertAndRetrieveProfile() = runTest {
        val profile = TestEntityFactory.profile(pin = 1234)
        val id = dao.insertProfile(profile).toInt()

        val loaded = dao.getProfileById(id)
        assertNotNull(loaded)
        assertEquals(4, loaded!!.pin?.toString()?.length)
        assertEquals(profile.name, loaded.name)
    }

    @Test
    fun observeProfilesEmitsValues() = runTest {
        val collector = mockk<(List<com.supernova.data.entities.ProfileEntity>) -> Unit>(relaxed = true)
        val job = launch {
            dao.getAllProfiles().collect { collector.invoke(it) }
        }
        dao.insertProfile(TestEntityFactory.profile(name = "A"))
        verify { collector.invoke(any()) }
        job.cancel()
    }
}
