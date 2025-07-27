package com.supernova.network

import com.supernova.network.dto.CategoryDto
import com.supernova.network.dto.StreamDto
import retrofit2.http.GET

interface XcApiService {
    @GET("live/streams")
    suspend fun getLiveStreams(): List<StreamDto>

    @GET("categories")
    suspend fun getCategories(): List<CategoryDto>
}
