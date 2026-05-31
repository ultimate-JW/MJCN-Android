package com.ultimatejw.mjcn.data.remote

import com.ultimatejw.mjcn.data.remote.dto.interest.InterestRequest
import com.ultimatejw.mjcn.data.remote.dto.interest.InterestResponse
import com.ultimatejw.mjcn.data.remote.dto.interest.PaginatedInterestResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface InterestApiService {

    @GET("api/v1/accounts/interests/")
    suspend fun listInterests(): Response<PaginatedInterestResponse>

    @POST("api/v1/accounts/interests/")
    suspend fun createInterest(@Body request: InterestRequest): Response<InterestResponse>
}
