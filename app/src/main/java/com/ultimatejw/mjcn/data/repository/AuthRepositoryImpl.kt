package com.ultimatejw.mjcn.data.repository

import com.ultimatejw.mjcn.data.remote.AuthApiService
import com.ultimatejw.mjcn.data.remote.dto.auth.ResendVerificationRequest
import com.ultimatejw.mjcn.data.remote.dto.auth.SignupRequest
import com.ultimatejw.mjcn.data.remote.dto.auth.VerifyEmailRequest
import com.ultimatejw.mjcn.domain.repository.AuthRepository
import retrofit2.Response
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authApiService: AuthApiService
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
