package com.ultimatejw.mjcn.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.ultimatejw.mjcn.domain.model.User

data class UserProfileDto(
    val id: Int? = null,
    val email: String = "",
    val name: String? = null,
    val grade: Int? = null,
    val semester: Int? = null,
    @SerializedName("admission_year") val admissionYear: Int? = null,
    @SerializedName("graduation_year") val graduationYear: Int? = null,
    @SerializedName("graduation_month") val graduationMonth: Int? = null,
    val major: String? = null,
    @SerializedName("is_onboarding_completed") val isOnboardingCompleted: Boolean? = null
)

fun UserProfileDto.toDomain() = User(
    id = id?.toString() ?: "",
    name = name ?: "",
    email = email,
    grade = grade ?: 0,
    semester = semester ?: 0,
    graduationYear = graduationYear,
    interests = emptyList(),
    entranceYear = admissionYear,
    major = major?.split(" · ")?.lastOrNull()?.trim()
)
