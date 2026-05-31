package com.ultimatejw.mjcn.domain.repository

interface InterestRepository {
    /** 관심분야 1건 등록. */
    suspend fun createInterest(category: String, customText: String): Result<Unit>

    /** 현재 등록된 관심분야 개수 (온보딩 재개 분기용). */
    suspend fun countInterests(): Result<Int>
}
