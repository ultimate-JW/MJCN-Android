package com.ultimatejw.mjcn.data.repository

import com.ultimatejw.mjcn.data.remote.MjcnApiService
import com.ultimatejw.mjcn.data.remote.dto.InformationListItemDto
import com.ultimatejw.mjcn.domain.model.Info
import com.ultimatejw.mjcn.domain.model.InfoPage
import com.ultimatejw.mjcn.domain.repository.InfoRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

class InfoRepositoryImpl @Inject constructor(
    private val apiService: MjcnApiService
) : InfoRepository {

    override suspend fun fetchInfosPage(
        page: Int,
        pageSize: Int,
        view: String?,
        category: String?,
        q: String?
    ): Result<InfoPage> = runCatching {
        val response = apiService.getInformations(
            page = page,
            pageSize = pageSize,
            view = view,
            category = category,
            q = q
        )
        val body = response.body()
            ?: error("정보 목록 응답이 비어있습니다 (${response.code()})")
        InfoPage(
            infos = body.results.map { it.toDomain() },
            totalCount = body.count,
            hasMore = body.next != null
        )
    }

    private fun InformationListItemDto.toDomain() = Info(
        id = id.toString(),
        title = title,
        category = categories?.firstOrNull() ?: "",
        team = organizer.orEmpty(),
        isGroup = false,
        dday = dDay ?: calculateDday(endDate),
        url = url,
        startDate = startDate,
        endDate = endDate
    )

    private fun calculateDday(endDate: String?): Int {
        endDate ?: return 0
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .apply { timeZone = TimeZone.getTimeZone("UTC") }
            val end = sdf.parse(endDate) ?: return 0
            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time
            ((end.time - today.time) / 86_400_000L).toInt()
        } catch (e: Exception) {
            0
        }
    }
}
