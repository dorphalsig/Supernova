package com.supernova.network

import com.supernova.network.models.LoginResponse
import com.supernova.network.models.CategoryResponse
import com.supernova.network.models.VodResponse
import com.supernova.network.models.SeriesResponse
import com.supernova.network.models.LiveTvResponse
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

interface ApiService {

    // --- Authentication ---
    @GET
    suspend fun testLogin(
        @Url url: String,
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "login"
    ): Response<LoginResponse>

    // --- Category Endpoints ---
    @GET
    suspend fun getLiveCategories(
        @Url url: String,
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_live_categories"
    ): Response<List<CategoryResponse>>

    @GET
    suspend fun getVodCategories(
        @Url url: String,
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_vod_categories"
    ): Response<List<CategoryResponse>>

    @GET
    suspend fun getSeriesCategories(
        @Url url: String,
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_series_categories"
    ): Response<List<CategoryResponse>>

    // --- Stream Endpoints ---
    @GET
    suspend fun getLiveStreams(
        @Url url: String,
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_live_streams"
    ): Response<List<LiveTvResponse>>

    @GET
    suspend fun getVodStreams(
        @Url url: String,
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_vod_streams"
    ): Response<List<VodResponse>>

    @GET
    suspend fun getSeriesStreams(
        @Url url: String,
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_series"
    ): Response<List<SeriesResponse>>

    companion object {
        fun create(baseUrl: String): ApiService {
            val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            return retrofit.create(ApiService::class.java)
        }

        // Utility to build the player API URL
        fun buildLoginUrl(portal: String): String {
            return "$portal/player_api.php"
        }
    }
}
