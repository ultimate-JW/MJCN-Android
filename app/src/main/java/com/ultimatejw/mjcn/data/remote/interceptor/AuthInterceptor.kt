package com.ultimatejw.mjcn.data.remote.interceptor

import com.ultimatejw.mjcn.data.local.TokenStore
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 보호된 API 호출에 Authorization: Bearer <access_token> 헤더를 자동 첨부한다.
 * - 인증이 필요 없는 경로(signup/login/verify-email 등)는 그대로 통과.
 * - 토큰이 없으면 헤더 미첨부 → 서버에서 401 응답.
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenStore: TokenStore
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val token = tokenStore.currentAccessToken()
        val request = if (token.isNullOrBlank()) {
            original
        } else {
            original.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        }
        return chain.proceed(request)
    }
}
