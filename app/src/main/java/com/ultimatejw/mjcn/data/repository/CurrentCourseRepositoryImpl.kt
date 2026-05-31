package com.ultimatejw.mjcn.data.repository

import com.ultimatejw.mjcn.data.remote.CurrentCourseApiService
import com.ultimatejw.mjcn.data.remote.dto.course.CurrentCourseRequest
import com.ultimatejw.mjcn.domain.repository.CurrentCourseRepository
import javax.inject.Inject

class CurrentCourseRepositoryImpl @Inject constructor(
    private val api: CurrentCourseApiService
) : CurrentCourseRepository {

    override suspend fun createCurrentCourse(
        courseName: String,
        courseCode: String,
        dayOfWeek: String,
        startTime: String,
        endTime: String,
        professor: String,
        room: String,
        building: String
    ): Result<Unit> = runCatching {
        val response = api.createCurrentCourse(
            CurrentCourseRequest(
                courseName = courseName,
                courseCode = courseCode,
                dayOfWeek = dayOfWeek,
                startTime = startTime,
                endTime = endTime,
                professor = professor,
                room = room,
                building = building
            )
        )
        if (!response.isSuccessful) {
            throw AuthApiException(response.code(), response.errorBody()?.string().orEmpty())
        }
    }
}
