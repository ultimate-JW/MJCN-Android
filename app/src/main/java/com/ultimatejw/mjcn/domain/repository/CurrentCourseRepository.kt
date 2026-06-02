package com.ultimatejw.mjcn.domain.repository

interface CurrentCourseRepository {
    suspend fun createCurrentCourse(offeringId: Int): Result<Unit>
}
