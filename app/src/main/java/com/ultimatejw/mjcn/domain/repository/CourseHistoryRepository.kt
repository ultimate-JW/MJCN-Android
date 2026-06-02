package com.ultimatejw.mjcn.domain.repository

interface CourseHistoryRepository {
    suspend fun createCourseHistory(
        courseCode: String,
        year: Int,
        semester: Int,
        gradeReceived: String = ""
    ): Result<Unit>

    suspend fun deleteCourseHistory(id: Int): Result<Unit>
}
