package com.ultimatejw.mjcn.data.remote

import com.ultimatejw.mjcn.data.remote.dto.course.CourseListDto
import com.ultimatejw.mjcn.data.remote.dto.course.CourseOfferingDto
import com.ultimatejw.mjcn.data.remote.dto.course.PaginatedDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface CoursesApiService {

    @GET("api/v1/courses/")
    suspend fun getCourses(
        @Query("q") query: String? = null,
        @Query("category") category: String? = null,
        @Query("college") college: String? = null,
        @Query("department") department: String? = null,
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 50
    ): Response<PaginatedDto<CourseListDto>>

    @GET("api/v1/courses/offerings/")
    suspend fun getCourseOfferings(
        @Query("query") query: String? = null,
        @Query("year") year: Int? = null,
        @Query("semester") semester: Int? = null,
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 50
    ): Response<PaginatedDto<CourseOfferingDto>>
}
