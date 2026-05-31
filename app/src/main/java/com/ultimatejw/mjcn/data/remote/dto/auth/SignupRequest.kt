package com.ultimatejw.mjcn.data.remote.dto.auth

import com.google.gson.annotations.SerializedName

data class SignupRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("password_confirm") val passwordConfirm: String
)
