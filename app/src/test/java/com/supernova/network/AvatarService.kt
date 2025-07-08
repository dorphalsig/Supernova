package com.supernova.network

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Url

interface AvatarService {

    @GET
    suspend fun downloadAvatar(@Url url: String): Response<ResponseBody>

    companion object {
        private const val DICEBEAR_BASE_URL = "https://api.dicebear.com/7.x/bottts/png"

        fun create(): AvatarService {
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
            return "$DICEBEAR_BASE_URL?backgroundColor=191b22,23253a&radius=50&seed=$seed"
        }

        /**
         * Generates multiple random avatar URLs
         * @param count Number of avatars to generate
         * @return List of avatar URLs
         */
        fun generateRandomAvatarUrls(count: Int): List<String> {
            val currentTime = System.currentTimeMillis()
            return (0 until count).map { index ->
                generateAvatarUrl(currentTime + index)
            }
        }

        /**
         * Generates a unique avatar URL for profile creation
         * @return Avatar URL string
         */
        fun generateProfileAvatarUrl(): String {
            return generateAvatarUrl(System.currentTimeMillis())
        }
    }
}