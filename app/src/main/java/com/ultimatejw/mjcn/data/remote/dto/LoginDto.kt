package com.ultimatejw.mjcn.data.remote.dto

data class LoginRequestDto(
    val email: String,
    val password: String
)

data class LoginResponseDto(
    val access: String,
    val refresh: String
)
