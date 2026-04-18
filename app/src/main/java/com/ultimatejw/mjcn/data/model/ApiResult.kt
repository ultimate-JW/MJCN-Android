package com.ultimatejw.mjcn.data.model

import retrofit2.Response

sealed class ApiResult<out T> {
    data class Success<out T>(val body: T) : ApiResult<T>()
    data class Error(val message: String?, val code: Int? = null) : ApiResult<Nothing>()
}

suspend fun <T> runRemote(block: suspend () -> Response<T>): ApiResult<T> {
    return try {
        val response = block()
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                ApiResult.Success(body)
            } else {
                ApiResult.Error("응답이 비어있습니다")
            }
        } else {
            ApiResult.Error(response.message(), response.code())
        }
    } catch (e: Exception) {
        ApiResult.Error(e.message)
    }
}
