package com.ultimatejw.mjcn.domain.repository

interface CurrentCourseRepository {
    suspend fun createCurrentCourse(
        courseName: String,
        courseCode: String,
        dayOfWeek: String,
        startTime: String,
        endTime: String,
        professor: String,
        room: String,
        building: String
    ): Result<Unit>
}
