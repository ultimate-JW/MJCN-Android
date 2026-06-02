package com.ultimatejw.mjcn.data.repository

import com.ultimatejw.mjcn.data.remote.CurrentCourseApiService
import com.ultimatejw.mjcn.data.remote.dto.course.CurrentCourseRequest
import com.ultimatejw.mjcn.domain.repository.CurrentCourseRepository
import javax.inject.Inject

class CurrentCourseRepositoryImpl @Inject constructor(
    private val api: CurrentCourseApiService
) : CurrentCourseRepository {

    override suspend fun createCurrentCourse(offeringId: Int): Result<Unit> = runCatching {
        val response = api.createCurrentCourse(CurrentCourseRequest(offeringId))
        if (!response.isSuccessful) {
            throw AuthApiException(response.code(), response.errorBody()?.string().orEmpty())
        }
    }

    override suspend fun deleteCurrentCourse(id: Int): Result<Unit> = runCatching {
        val response = api.deleteCurrentCourse(id)
        if (!response.isSuccessful) {
            throw AuthApiException(response.code(), response.errorBody()?.string().orEmpty())
        }
    }
}
