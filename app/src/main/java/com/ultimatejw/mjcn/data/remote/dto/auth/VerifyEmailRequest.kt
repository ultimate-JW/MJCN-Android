package com.ultimatejw.mjcn.data.remote.dto.auth

import com.google.gson.annotations.SerializedName

data class VerifyEmailRequest(
    @SerializedName("email") val email: String,
    @SerializedName("code") val code: String
)
