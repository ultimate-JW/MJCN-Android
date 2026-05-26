package com.ultimatejw.mjcn.data.remote.dto.interest

import com.google.gson.annotations.SerializedName

data class InterestResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("category") val category: String?,
    @SerializedName("custom_text") val customText: String?
)

/** 페이지네이션 응답 (interests GET). 우리는 count 정도만 사용. */
data class PaginatedInterestResponse(
    @SerializedName("count") val count: Int = 0,
    @SerializedName("results") val results: List<InterestResponse> = emptyList()
)
