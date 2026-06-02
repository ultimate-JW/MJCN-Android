package com.ultimatejw.mjcn.data.remote

import com.ultimatejw.mjcn.data.remote.dto.course.CourseHistoryRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.Path

interface CourseHistoryApiService {

    @POST("api/v1/accounts/course-history/")
    suspend fun createCourseHistory(@Body request: CourseHistoryRequest): Response<Unit>

    @DELETE("api/v1/accounts/course-history/{id}/")
    suspend fun deleteCourseHistory(@Path("id") id: Int): Response<Unit>
}
