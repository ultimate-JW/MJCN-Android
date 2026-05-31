package com.ultimatejw.mjcn.data.remote

import com.ultimatejw.mjcn.data.remote.dto.profile.ProfileResponse
import com.ultimatejw.mjcn.data.remote.dto.profile.ProfileUpdateRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH

interface ProfileApiService {

    @GET("api/v1/accounts/profile/")
    suspend fun getProfile(): Response<ProfileResponse>

    @PATCH("api/v1/accounts/profile/")
    suspend fun patchProfile(@Body request: ProfileUpdateRequest): Response<ProfileResponse>
}
