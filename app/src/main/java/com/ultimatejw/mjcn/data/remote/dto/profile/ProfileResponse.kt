package com.ultimatejw.mjcn.data.remote.dto.profile

import com.google.gson.annotations.SerializedName

data class ProfileResponse(
    @SerializedName("name") val name: String? = null,
    @SerializedName("grade") val grade: Int? = null,
    @SerializedName("semester") val semester: Int? = null,
    @SerializedName("admission_year") val admissionYear: Int? = null,
    @SerializedName("graduation_year") val graduationYear: Int? = null,
    @SerializedName("graduation_month") val graduationMonth: Int? = null,
    @SerializedName("major") val major: String? = null,
    @SerializedName("is_onboarding_completed") val isOnboardingCompleted: Boolean? = null,
    @SerializedName("interests") val interests: List<InterestAreaDto>? = null,
    @SerializedName("course_histories") val courseHistories: List<CourseHistoryDto>? = null,
    @SerializedName("current_courses") val currentCourses: List<CurrentCourseDto>? = null
)
