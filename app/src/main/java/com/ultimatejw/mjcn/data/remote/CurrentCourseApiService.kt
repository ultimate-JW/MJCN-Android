package com.ultimatejw.mjcn.data.remote

import com.ultimatejw.mjcn.data.remote.dto.course.CurrentCourseRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface CurrentCourseApiService {

    @POST("api/v1/accounts/current-courses/")
    suspend fun createCurrentCourse(@Body request: CurrentCourseRequest): Response<Unit>
}
