package com.ultimatejw.mjcn.domain.repository

import com.ultimatejw.mjcn.data.model.ApiResult
import com.ultimatejw.mjcn.domain.model.DashboardData

interface HomeRepository {
    suspend fun getDashboard(): ApiResult<DashboardData>
}
