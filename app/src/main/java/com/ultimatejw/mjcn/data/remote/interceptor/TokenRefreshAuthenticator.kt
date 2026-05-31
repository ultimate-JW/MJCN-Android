package com.ultimatejw.mjcn.data.remote.interceptor

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.ultimatejw.mjcn.data.local.TokenStore
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Singleton

private const val BASE_URL = "http://3.34.185.127:8000/"

@Singleton
class TokenRefreshAuthenticator @Inject constructor(
    private val tokenStore: TokenStore
) : Authenticator {

    private val refreshClient = OkHttpClient()
    private val gson = Gson()

    override fun authenticate(route: Route?, response: Response): Request? {
        // refresh 엔드포인트 자체가 401이면 무한 루프 방지
        if (response.request.url.encodedPath.contains("token/refresh")) return null

        synchronized(this) {
            val currentToken = tokenStore.currentAccessToken()
            val failedToken = response.request.header("Authorization")
                ?.removePrefix("Bearer ")?.trim()

            // 다른 스레드가 이미 토큰을 갱신한 경우 새 토큰으로 재시도
            if (currentToken != null && currentToken != failedToken) {
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $currentToken")
                    .build()
            }

            val refreshToken = tokenStore.currentRefreshToken() ?: run {
                tokenStore.notifySessionExpired()
                return null
            }

            val newAccess = doRefresh(refreshToken) ?: run {
                runBlocking { tokenStore.clearTokens() }
                tokenStore.notifySessionExpired()
                return null
            }

            runBlocking { tokenStore.saveTokens(newAccess, null) }

            return response.request.newBuilder()
                .header("Authorization", "Bearer $newAccess")
                .build()
        }
    }

    private fun doRefresh(refreshToken: String): String? {
        val body = gson.toJson(mapOf("refresh" to refreshToken))
            .toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("${BASE_URL}api/v1/accounts/token/refresh/")
            .post(body)
            .build()
        return try {
            refreshClient.newCall(request).execute().use { resp ->
                if (!resp.isSuccessful) return null
                gson.fromJson(resp.body?.string(), JsonObject::class.java)
                    ?.get("access")?.asString
            }
        } catch (e: Exception) {
            null
        }
    }
}
