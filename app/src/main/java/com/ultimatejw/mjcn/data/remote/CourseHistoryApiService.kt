package com.ultimatejw.mjcn.data.remote

import com.ultimatejw.mjcn.data.remote.dto.course.CourseHistoryRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface CourseHistoryApiService {

    @POST("api/v1/accounts/course-history/")
    suspend fun createCourseHistory(@Body request: CourseHistoryRequest): Response<Unit>
}
