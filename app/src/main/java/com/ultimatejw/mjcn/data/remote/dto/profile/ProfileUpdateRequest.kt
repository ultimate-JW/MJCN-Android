package com.ultimatejw.mjcn.data.remote.dto.profile

import com.google.gson.annotations.SerializedName

/**
 * PATCH /api/v1/accounts/profile/ 요청. 보내고 싶은 필드만 채워서 전송.
 * Gson은 null 필드를 기본적으로 직렬화하지 않으므로 PATCH의 부분 갱신 의미를 그대로 살린다.
 */
data class ProfileUpdateRequest(
    @SerializedName("name") val name: String? = null,
    @SerializedName("grade") val grade: Int? = null,
    @SerializedName("semester") val semester: Int? = null,
    @SerializedName("admission_year") val admissionYear: Int? = null,
    @SerializedName("graduation_year") val graduationYear: Int? = null,
    @SerializedName("graduation_month") val graduationMonth: Int? = null,
    @SerializedName("major") val major: String? = null,
    @SerializedName("is_onboarding_completed") val isOnboardingCompleted: Boolean? = null
)
