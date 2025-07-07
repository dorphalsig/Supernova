package com.supernova.network.models

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("user_info")
    val userInfo: UserInfo
)

data class UserInfo(
    @SerializedName("auth")
    val auth: Int,
    @SerializedName("status")
    val status: String
)

// Result wrapper for API calls
sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String) : ApiResult<Nothing>()
    data class Loading(val isLoading: Boolean = true) : ApiResult<Nothing>()
}