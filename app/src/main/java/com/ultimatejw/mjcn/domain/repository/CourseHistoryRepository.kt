package com.ultimatejw.mjcn.domain.repository

interface CourseHistoryRepository {
    suspend fun createCourseHistory(
        courseName: String,
        courseCode: String,
        year: Int,
        semester: Int,
        gradeReceived: String,
        category: String,
        credits: Int
    ): Result<Unit>
}
