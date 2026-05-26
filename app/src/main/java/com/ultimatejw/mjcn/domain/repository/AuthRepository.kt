package com.ultimatejw.mjcn.domain.repository

interface AuthRepository {
    suspend fun signup(email: String, password: String, passwordConfirm: String): Result<Unit>
    suspend fun verifyEmail(email: String, code: String): Result<Unit>
    suspend fun resendVerification(email: String): Result<Unit>
    /** 성공 시 access/refresh 토큰을 TokenStore에 저장한다. */
    suspend fun login(email: String, password: String): Result<Unit>
}
