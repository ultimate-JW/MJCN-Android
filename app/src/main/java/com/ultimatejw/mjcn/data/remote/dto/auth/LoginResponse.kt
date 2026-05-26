package com.ultimatejw.mjcn.data.remote.dto.auth

import com.google.gson.annotations.SerializedName

/**
 * SimpleJWT 응답. 스웨거에 응답 바디가 명시되어 있지 않으므로 access/refresh만 수신.
 * 추가 필드(user 등)는 무시.
 */
data class LoginResponse(
    @SerializedName("access") val access: String?,
    @SerializedName("refresh") val refresh: String?
)
