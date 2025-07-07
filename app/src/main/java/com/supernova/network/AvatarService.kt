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
        private const val BASE_URL = "https://robohash.org/"

        fun create(): AvatarService {
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .build()

            return retrofit.create(AvatarService::class.java)
        }

        fun generateAvatarUrl(timestamp: Long): String {
            return "$BASE_URL$timestamp.png"
        }

        fun generateRandomAvatarUrls(count: Int): List<String> {
            val currentTime = System.currentTimeMillis()
            return (0 until count).map { index ->
                generateAvatarUrl(currentTime + index)
            }
        }
    }
}