package com.ultimatejw.mjcn.domain.usecase.home

import com.ultimatejw.mjcn.data.model.ApiResult
import com.ultimatejw.mjcn.domain.model.DashboardData
import com.ultimatejw.mjcn.domain.repository.HomeRepository
import javax.inject.Inject

class GetDashboardUseCase @Inject constructor(
    private val homeRepository: HomeRepository
) {
    suspend operator fun invoke(): ApiResult<DashboardData> = homeRepository.getDashboard()
}
