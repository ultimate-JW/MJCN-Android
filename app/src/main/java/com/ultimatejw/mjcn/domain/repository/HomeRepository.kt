package com.ultimatejw.mjcn.domain.repository

import com.ultimatejw.mjcn.data.model.ApiResult
import com.ultimatejw.mjcn.domain.model.DashboardData
import com.ultimatejw.mjcn.domain.model.Info
import com.ultimatejw.mjcn.domain.model.Notice

interface HomeRepository {
    suspend fun getDashboard(): ApiResult<DashboardData>
    suspend fun getNoticeDetail(id: String): ApiResult<Notice>
    suspend fun getInfoDetail(id: String): ApiResult<Info>
}
