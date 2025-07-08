package com.supernova.utils

import android.content.Context
import coil.ImageLoader
import coil.request.ImageRequest
import com.supernova.data.entities.ProfileEntity
import com.supernova.network.AvatarService
import com.supernova.ui.model.CarouselState
import com.supernova.ui.model.ProfileDisplayItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AvatarPreloader(private val context: Context) {

    private val imageLoader = ImageLoader(context)
    private val preloaderScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    /**
     * Preloads 5 profile avatars (center + 2 in each direction) from database
     * Called from splash screen when user is already configured
     */
    fun preloadProfileAvatars(profiles: List<ProfileEntity>) {
        preloaderScope.launch {
            try {
                // Take first 5 profiles and preload their avatar URLs
                profiles.take(5).forEach { profile ->
                    preloadAvatarUrl(profile.avatar)
                }
            } catch (e: Exception) {
                // Silent fail - preloading is not critical
            }
        }
    }

    /**
     * Preloads adjacent avatars when carousel moves
     */
    fun preloadAdjacentAvatars(carouselState: CarouselState, direction: Direction) {
        preloaderScope.launch {
            try {
                when (direction) {
                    Direction.LEFT -> {
                        // Preload far left item
                        carouselState.farLeftItem?.let { item ->
                            if (item is ProfileDisplayItem.RealProfile) {
                                preloadAvatarUrl(item.profile.avatar)
                            }
                        }
                    }
                    Direction.RIGHT -> {
                        // Preload far right item
                        carouselState.farRightItem?.let { item ->
                            if (item is ProfileDisplayItem.RealProfile) {
                                preloadAvatarUrl(item.profile.avatar)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // Silent fail - preloading is not critical
            }
        }
    }

    /**
     * Preloads random avatars for profile creation
     */
    fun preloadRandomAvatars(count: Int = 6) {
        preloaderScope.launch {
            try {
                val avatarUrls = AvatarService.generateRandomAvatarUrls(count)
                avatarUrls.forEach { url ->
                    preloadAvatarUrl(url)
                }
            } catch (e: Exception) {
                // Silent fail - preloading is not critical
            }
        }
    }

    private suspend fun preloadAvatarUrl(url: String) {
        withContext(Dispatchers.IO) {
            try {
                val request = ImageRequest.Builder(context)
                    .data(url)
                    .build()
                imageLoader.execute(request)
            } catch (e: Exception) {
                // Silent fail for individual avatars
            }
        }
    }

    enum class Direction {
        LEFT, RIGHT
    }
}