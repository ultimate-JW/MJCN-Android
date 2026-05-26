package com.ultimatejw.mjcn.domain.repository

interface AuthRepository {
    suspend fun signup(email: String, password: String, passwordConfirm: String): Result<Unit>
    suspend fun verifyEmail(email: String, code: String): Result<Unit>
    suspend fun resendVerification(email: String): Result<Unit>
}
