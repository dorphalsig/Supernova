package com.supernova.network

import android.util.Log
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Url

interface AvatarService {

    @GET
    suspend fun downloadAvatar(@Url url: String): Response<ResponseBody>

    companion object {
        private const val TAG = "AvatarService"
        private const val DICEBEAR_BASE_URL = "http://api.dicebear.com/9.x/bottts/png"

        fun create(): AvatarService {
            Log.d(TAG, "Creating AvatarService with base URL: https://api.dicebear.com/")
            val retrofit = Retrofit.Builder()
                .baseUrl("https://api.dicebear.com/")
                .build()

            return retrofit.create(AvatarService::class.java)
        }

        /**
         * Generates a DiceBear avatar URL with custom background colors
         * @param seed Unique seed for avatar generation (typically timestamp)
         * @return Complete avatar URL
         */
        fun generateAvatarUrl(seed: Long): String {
            val url = "$DICEBEAR_BASE_URL?backgroundColor=191b22,23253a&radius=50&seed=$seed"
            Log.d(TAG, "Generated avatar URL for seed $seed: $url")
            return url
        }

        /**
         * Generates multiple random avatar URLs
         * @param count Number of avatars to generate
         * @return List of avatar URLs
         */
        fun generateRandomAvatarUrls(count: Int): List<String> {
            Log.d(TAG, "Generating $count random avatar URLs")
            val currentTime = System.currentTimeMillis()
            Log.d(TAG, "Using base timestamp: $currentTime")

            val urls = (0 until count).map { index ->
                val seed = currentTime + index
                generateAvatarUrl(seed)
            }

            Log.d(TAG, "Generated ${urls.size} avatar URLs:")
            urls.forEachIndexed { index, url ->
                Log.d(TAG, "  [$index]: $url")
            }

            return urls
        }


    }
}