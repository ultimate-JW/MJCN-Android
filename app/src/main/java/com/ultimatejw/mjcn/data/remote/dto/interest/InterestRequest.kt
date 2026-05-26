package com.ultimatejw.mjcn.data.remote.dto.interest

import com.google.gson.annotations.SerializedName

data class InterestRequest(
    @SerializedName("category") val category: String,
    @SerializedName("custom_text") val customText: String = ""
)
