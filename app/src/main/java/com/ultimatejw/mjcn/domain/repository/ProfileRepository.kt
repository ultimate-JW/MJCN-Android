package com.ultimatejw.mjcn.domain.repository

import com.ultimatejw.mjcn.data.remote.dto.profile.ProfileResponse

interface ProfileRepository {
    /** 현재 프로필 조회 (온보딩 재개 분기에 사용). */
    suspend fun getProfile(): Result<ProfileResponse>

    /**
     * Step1+2 묶음 저장. nullable 파라미터는 보내지 않음.
     * @param major 대학·학부·전공을 합친 문자열(서버 필드 1개).
     */
    suspend fun patchProfile(
        name: String? = null,
        grade: Int? = null,
        semester: Int? = null,
        admissionYear: Int? = null,
        graduationYear: Int? = null,
        graduationMonth: Int? = null,
        major: String? = null,
        isOnboardingCompleted: Boolean? = null
    ): Result<Unit>
}
