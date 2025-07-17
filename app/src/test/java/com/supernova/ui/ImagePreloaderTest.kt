package com.supernova.ui

import android.content.Context
import coil.ImageLoader
import coil.request.ImageRequest
import com.supernova.ui.preload.ImagePreloader
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ImagePreloaderTest {
    private lateinit var context: Context
    private lateinit var loader: ImageLoader
    private lateinit var preloader: ImagePreloader

    @Before
    fun setUp() {
        context = mockk(relaxed = true)
        loader = mockk(relaxed = true)
        preloader = ImagePreloader(context, loader) { true }
    }

    @Test
    fun visible_priority_loaded_first() = runTest {
        val urlsVisible = listOf("visible1", "visible2")
        val urlsNext = listOf("next1")

        preloader.preloadNext(urlsNext)
        preloader.preloadVisible(urlsVisible)

        verify { loader.enqueue(match<ImageRequest> { it.data == "visible1" }) }
        verify { loader.enqueue(match<ImageRequest> { it.data == "visible2" }) }
        verify { loader.enqueue(match<ImageRequest> { it.data == "next1" }) }
    }
}
