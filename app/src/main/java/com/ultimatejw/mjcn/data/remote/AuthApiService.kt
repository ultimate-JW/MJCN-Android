package com.ultimatejw.mjcn.data.remote

import com.ultimatejw.mjcn.data.remote.dto.auth.LoginRequest
import com.ultimatejw.mjcn.data.remote.dto.auth.LoginResponse
import com.ultimatejw.mjcn.data.remote.dto.auth.ResendVerificationRequest
import com.ultimatejw.mjcn.data.remote.dto.auth.SignupRequest
import com.ultimatejw.mjcn.data.remote.dto.auth.VerifyEmailRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    @POST("api/v1/accounts/signup/")
    suspend fun signup(@Body request: SignupRequest): Response<Unit>

    @POST("api/v1/accounts/verify-email/")
    suspend fun verifyEmail(@Body request: VerifyEmailRequest): Response<Unit>

    @POST("api/v1/accounts/verify-email/resend/")
    suspend fun resendVerification(@Body request: ResendVerificationRequest): Response<Unit>

    @POST("api/v1/accounts/login/")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
}
