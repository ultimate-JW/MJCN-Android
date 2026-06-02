package com.ultimatejw.mjcn.data.repository

import com.ultimatejw.mjcn.data.remote.CourseHistoryApiService
import com.ultimatejw.mjcn.data.remote.dto.course.CourseHistoryRequest
import com.ultimatejw.mjcn.domain.repository.CourseHistoryRepository
import javax.inject.Inject

class CourseHistoryRepositoryImpl @Inject constructor(
    private val api: CourseHistoryApiService
) : CourseHistoryRepository {

    override suspend fun createCourseHistory(
        courseCode: String,
        year: Int,
        semester: Int,
        gradeReceived: String
    ): Result<Unit> = runCatching {
        val response = api.createCourseHistory(
            CourseHistoryRequest(
                courseCode = courseCode,
                year = year,
                semester = semester,
                gradeReceived = gradeReceived
            )
        )
        if (!response.isSuccessful) {
            throw AuthApiException(response.code(), response.errorBody()?.string().orEmpty())
        }
    }
}
