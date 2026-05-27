package com.ultimatejw.mjcn.data.remote

import com.ultimatejw.mjcn.data.remote.dto.DashboardDto
import com.ultimatejw.mjcn.data.remote.dto.InformationDetailDto
import com.ultimatejw.mjcn.data.remote.dto.NoticeDetailDto
import com.ultimatejw.mjcn.data.remote.dto.PaginatedInformationDto
import com.ultimatejw.mjcn.data.remote.dto.PaginatedNoticeDto
import com.ultimatejw.mjcn.data.remote.dto.LoginRequestDto
import com.ultimatejw.mjcn.data.remote.dto.LoginResponseDto
import com.ultimatejw.mjcn.data.remote.dto.UserProfileDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.POST
import retrofit2.http.Query

interface MjcnApiService {

    @POST("api/v1/accounts/login/")
    suspend fun login(@Body body: LoginRequestDto): Response<LoginResponseDto>

    @GET("api/v1/dashboard/")
    suspend fun getDashboard(): Response<DashboardDto>

    @GET("api/v1/accounts/profile/")
    suspend fun getProfile(): Response<UserProfileDto>

    @GET("api/v1/notices/")
    suspend fun getNotices(
        @Query("page") page: Int,
        @Query("page_size") pageSize: Int,
        @Query("view") view: String? = null,
        @Query(value = "source", encoded = true) source: String? = null,
        @Query("q") q: String? = null
    ): Response<PaginatedNoticeDto>

    @GET("api/v1/information/")
    suspend fun getInformations(
        @Query("page") page: Int,
        @Query("page_size") pageSize: Int,
        @Query("view") view: String? = null,
        @Query(value = "category", encoded = true) category: String? = null,
        @Query("q") q: String? = null
    ): Response<PaginatedInformationDto>

    @GET("api/v1/notices/{id}/")
    suspend fun getNoticeDetail(@Path("id") id: String): Response<NoticeDetailDto>

    @GET("api/v1/information/{id}/")
    suspend fun getInformationDetail(@Path("id") id: String): Response<InformationDetailDto>
}
