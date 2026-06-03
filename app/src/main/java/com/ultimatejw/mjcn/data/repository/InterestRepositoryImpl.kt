package com.ultimatejw.mjcn.data.repository

import com.ultimatejw.mjcn.data.remote.InterestApiService
import com.ultimatejw.mjcn.data.remote.dto.interest.InterestRequest
import com.ultimatejw.mjcn.domain.repository.InterestRepository
import javax.inject.Inject

class InterestRepositoryImpl @Inject constructor(
    private val interestApiService: InterestApiService
) : InterestRepository {

    override suspend fun createInterest(category: String, customText: String): Result<Unit> = runCatching {
        val response = interestApiService.createInterest(
            InterestRequest(category = category, customText = customText)
        )
        if (!response.isSuccessful) {
            throw AuthApiException(response.code(), response.errorBody()?.string().orEmpty())
        }
    }

    override suspend fun deleteInterest(id: Int): Result<Unit> = runCatching {
        val response = interestApiService.deleteInterest(id)
        if (!response.isSuccessful) {
            throw AuthApiException(response.code(), response.errorBody()?.string().orEmpty())
        }
    }

    override suspend fun listInterests(): Result<List<Pair<Int, String>>> = runCatching {
        val response = interestApiService.listInterests()
        if (!response.isSuccessful) {
            throw AuthApiException(response.code(), response.errorBody()?.string().orEmpty())
        }
        response.body()?.results?.map { dto ->
            val label: String = if (dto.category == "기타" && !dto.customText.isNullOrBlank()) dto.customText!! else dto.category.orEmpty()
            Pair(dto.id, label)
        } ?: emptyList<Pair<Int, String>>()
    }

    override suspend fun countInterests(): Result<Int> = runCatching {
        val response = interestApiService.listInterests()
        if (!response.isSuccessful) {
            throw AuthApiException(response.code(), response.errorBody()?.string().orEmpty())
        }
        response.body()?.count ?: 0
    }
}
