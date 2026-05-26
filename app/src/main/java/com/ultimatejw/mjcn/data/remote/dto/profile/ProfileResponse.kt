package com.ultimatejw.mjcn.data.remote.dto.profile

import com.google.gson.annotations.SerializedName

/**
 * GET/PATCH /api/v1/accounts/profile/ 응답. 모든 필드가 옵셔널.
 * 온보딩 진행 분기에 사용.
 */
data class ProfileResponse(
    @SerializedName("name") val name: String? = null,
    @SerializedName("grade") val grade: Int? = null,
    @SerializedName("semester") val semester: Int? = null,
    @SerializedName("admission_year") val admissionYear: Int? = null,
    @SerializedName("graduation_year") val graduationYear: Int? = null,
    @SerializedName("graduation_month") val graduationMonth: Int? = null,
    @SerializedName("major") val major: String? = null,
    @SerializedName("is_onboarding_completed") val isOnboardingCompleted: Boolean? = null
)
