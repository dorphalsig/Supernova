package com.supernova.network

import com.supernova.network.dto.CategoryDto
import com.supernova.network.dto.StreamDto
import com.supernova.network.dto.ProgramDto
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit service for XC API provider endpoints.
 */
interface ProviderService {
    @GET("player_api.php")
    suspend fun getCategories(
        @Query("action") action: String = "get_live_categories",
        @Query("username") username: String,
        @Query("password") password: String
    ): List<CategoryDto>

    @GET("player_api.php")
    suspend fun getStreams(
        @Query("action") action: String = "get_live_streams",
        @Query("category_id") categoryId: Int,
        @Query("username") username: String,
        @Query("password") password: String
    ): List<StreamDto>

    @GET("player_api.php")
    suspend fun getPrograms(
        @Query("action") action: String = "get_short_epg",
        @Query("stream_id") streamId: Int,
        @Query("limit") limit: Int = 1,
        @Query("username") username: String,
        @Query("password") password: String
    ): List<ProgramDto>
}
