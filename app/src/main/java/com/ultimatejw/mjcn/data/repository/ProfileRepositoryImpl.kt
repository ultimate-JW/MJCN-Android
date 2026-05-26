package com.ultimatejw.mjcn.data.repository

import com.ultimatejw.mjcn.data.remote.ProfileApiService
import com.ultimatejw.mjcn.data.remote.dto.profile.ProfileResponse
import com.ultimatejw.mjcn.data.remote.dto.profile.ProfileUpdateRequest
import com.ultimatejw.mjcn.domain.repository.ProfileRepository
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(
    private val profileApiService: ProfileApiService
) : ProfileRepository {

    override suspend fun getProfile(): Result<ProfileResponse> = runCatching {
        val response = profileApiService.getProfile()
        if (!response.isSuccessful) {
            throw AuthApiException(response.code(), response.errorBody()?.string().orEmpty())
        }
        response.body() ?: ProfileResponse()
    }

    override suspend fun patchProfile(
        name: String?,
        grade: Int?,
        semester: Int?,
        admissionYear: Int?,
        graduationYear: Int?,
        graduationMonth: Int?,
        major: String?,
        isOnboardingCompleted: Boolean?
    ): Result<Unit> = runCatching {
        val response = profileApiService.patchProfile(
            ProfileUpdateRequest(
                name = name,
                grade = grade,
                semester = semester,
                admissionYear = admissionYear,
                graduationYear = graduationYear,
                graduationMonth = graduationMonth,
                major = major,
                isOnboardingCompleted = isOnboardingCompleted
            )
        )
        if (!response.isSuccessful) {
            throw AuthApiException(response.code(), response.errorBody()?.string().orEmpty())
        }
    }
}
