package com.supernova.network.tmdb

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TmdbService {
    @GET("trending/movie/day")
    suspend fun trendingMovies(@Query("api_key") apiKey: String): TmdbTrendingResponse

    @GET("trending/tv/week")
    suspend fun trendingSeries(@Query("api_key") apiKey: String): TmdbTrendingResponse

    @GET("{type}/{id}/recommendations")
    suspend fun recommendations(
        @Path("type") type: String,
        @Path("id") id: Int,
        @Query("api_key") apiKey: String
    ): TmdbTrendingResponse
}
