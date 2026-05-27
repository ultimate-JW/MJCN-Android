package com.ultimatejw.mjcn.domain.repository

import com.ultimatejw.mjcn.domain.model.InfoPage

interface InfoRepository {
    suspend fun fetchInfosPage(
        page: Int,
        pageSize: Int = 10,
        view: String? = null,
        category: String? = null,
        q: String? = null
    ): Result<InfoPage>
}
