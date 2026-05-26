package com.ultimatejw.mjcn.data.remote.dto.auth

import com.google.gson.annotations.SerializedName

data class ResendVerificationRequest(
    @SerializedName("email") val email: String
)
