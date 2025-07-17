package com.supernova.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.supernova.data.dao.ProfileDao
import com.supernova.testing.InstrumentedTestSuite
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import io.mockk.mockk
import io.mockk.verify

/**
 * Instrumented tests for [ProfileDao] using real Room database.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class ProfileDaoInstrumentedTest : InstrumentedTestSuite() {

    private lateinit var dao: ProfileDao

    @Before
    override fun setUp() {
        super.setUp()
        dao = db.profileDao()
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
        val job = launch { dao.getAllProfiles().collect { collector.invoke(it) } }
        dao.insertProfile(TestEntityFactory.profile(name = "A"))
        verify { collector.invoke(any()) }
        job.cancel()
    }
}
