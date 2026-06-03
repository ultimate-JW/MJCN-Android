package com.ultimatejw.mjcn.domain.repository

interface InterestRepository {
    suspend fun createInterest(category: String, customText: String): Result<Unit>
    suspend fun deleteInterest(id: Int): Result<Unit>
    suspend fun listInterests(): Result<List<Pair<Int, String>>>
    suspend fun countInterests(): Result<Int>
}
