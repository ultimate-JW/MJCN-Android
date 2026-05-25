package com.ultimatejw.mjcn.data.remote

import com.ultimatejw.mjcn.data.remote.dto.DashboardDto
import com.ultimatejw.mjcn.data.remote.dto.LoginRequestDto
import com.ultimatejw.mjcn.data.remote.dto.LoginResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface MjcnApiService {

    @POST("api/v1/accounts/login/")
    suspend fun login(@Body body: LoginRequestDto): Response<LoginResponseDto>

    @GET("api/v1/dashboard/")
    suspend fun getDashboard(): Response<DashboardDto>
}
