package com.ultimatejw.mjcn.data.repository

import com.ultimatejw.mjcn.data.local.TokenStore
import com.ultimatejw.mjcn.data.remote.AuthApiService
import com.ultimatejw.mjcn.data.remote.dto.auth.LoginRequest
import com.ultimatejw.mjcn.data.remote.dto.auth.ResendVerificationRequest
import com.ultimatejw.mjcn.data.remote.dto.auth.SignupRequest
import com.ultimatejw.mjcn.data.remote.dto.auth.VerifyEmailRequest
import com.ultimatejw.mjcn.domain.repository.AuthRepository
import com.ultimatejw.mjcn.domain.repository.UserRepository
import retrofit2.Response
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authApiService: AuthApiService,
    private val tokenStore: TokenStore,
    private val userRepository: UserRepository
) : AuthRepository {

    override suspend fun signup(
        email: String,
        password: String,
        passwordConfirm: String
    ): Result<Unit> = runCatching {
        val response = authApiService.signup(
            SignupRequest(email = email, password = password, passwordConfirm = passwordConfirm)
        )
        response.requireSuccess()
    }

    override suspend fun verifyEmail(email: String, code: String): Result<Unit> = runCatching {
        val response = authApiService.verifyEmail(
            VerifyEmailRequest(email = email, code = code)
        )
        response.requireSuccess()
    }

    override suspend fun resendVerification(email: String): Result<Unit> = runCatching {
        val response = authApiService.resendVerification(
            ResendVerificationRequest(email = email)
        )
        response.requireSuccess()
    }

    override suspend fun login(email: String, password: String): Result<Unit> = runCatching {
        val response = authApiService.login(LoginRequest(email = email, password = password))
        if (!response.isSuccessful) {
            val body = response.errorBody()?.string().orEmpty()
            throw AuthApiException(code = response.code(), errorBody = body)
        }
        val body = response.body() ?: throw AuthApiException(
            code = response.code(),
            errorBody = "로그인 응답이 비어있습니다."
        )
        val access = body.access ?: throw AuthApiException(
            code = response.code(),
            errorBody = "access 토큰이 없습니다."
        )
        tokenStore.saveTokens(access = access, refresh = body.refresh)
        // 로컬 isLoggedIn 플래그도 함께 마킹 → 스플래시/온보딩 재개 분기에 사용.
        userRepository.saveLoginState(access)
    }

    private fun Response<Unit>.requireSuccess() {
        if (!isSuccessful) {
            val errorBody = errorBody()?.string().orEmpty()
            throw AuthApiException(code = code(), errorBody = errorBody)
        }
    }
}

class AuthApiException(
    val code: Int,
    val errorBody: String
) : RuntimeException("Auth API failed: HTTP $code, body=$errorBody")
