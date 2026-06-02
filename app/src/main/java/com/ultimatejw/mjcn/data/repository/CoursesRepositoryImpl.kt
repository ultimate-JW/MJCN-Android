package com.ultimatejw.mjcn.data.repository

import com.ultimatejw.mjcn.data.remote.CoursesApiService
import com.ultimatejw.mjcn.data.remote.dto.course.CourseListDto
import com.ultimatejw.mjcn.data.remote.dto.course.CourseOfferingDto
import com.ultimatejw.mjcn.data.remote.dto.course.PaginatedDto
import com.ultimatejw.mjcn.domain.repository.CoursesRepository
import javax.inject.Inject

class CoursesRepositoryImpl @Inject constructor(
    private val api: CoursesApiService
) : CoursesRepository {

    override suspend fun searchCourses(
        query: String?,
        category: String?,
        page: Int,
        pageSize: Int
    ): Result<PaginatedDto<CourseListDto>> = runCatching {
        val response = api.getCourses(query = query, category = category, page = page, pageSize = pageSize)
        if (!response.isSuccessful) {
            throw AuthApiException(response.code(), response.errorBody()?.string().orEmpty())
        }
        response.body()!!
    }

    override suspend fun searchOfferings(
        query: String?,
        year: Int?,
        semester: Int?,
        page: Int,
        pageSize: Int
    ): Result<PaginatedDto<CourseOfferingDto>> = runCatching {
        val response = api.getCourseOfferings(
            query = query,
            year = year,
            semester = semester,
            page = page,
            pageSize = pageSize
        )
        if (!response.isSuccessful) {
            throw AuthApiException(response.code(), response.errorBody()?.string().orEmpty())
        }
        response.body()!!
    }
}
