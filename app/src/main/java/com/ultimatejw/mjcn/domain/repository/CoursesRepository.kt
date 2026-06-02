package com.ultimatejw.mjcn.domain.repository

import com.ultimatejw.mjcn.data.remote.dto.course.CourseListDto
import com.ultimatejw.mjcn.data.remote.dto.course.CourseOfferingDto
import com.ultimatejw.mjcn.data.remote.dto.course.PaginatedDto

interface CoursesRepository {
    suspend fun searchCourses(
        query: String? = null,
        page: Int = 1,
        pageSize: Int = 50
    ): Result<PaginatedDto<CourseListDto>>

    suspend fun searchOfferings(
        query: String? = null,
        year: Int? = null,
        semester: Int? = null,
        page: Int = 1,
        pageSize: Int = 50
    ): Result<PaginatedDto<CourseOfferingDto>>
}
