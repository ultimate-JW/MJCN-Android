package com.ultimatejw.mjcn.data.repository

import com.ultimatejw.mjcn.data.local.dao.NoticeDao
import com.ultimatejw.mjcn.data.local.entity.toEntity
import com.ultimatejw.mjcn.data.remote.MjcnApiService
import com.ultimatejw.mjcn.data.remote.dto.NoticeListItemDto
import com.ultimatejw.mjcn.domain.model.Notice
import com.ultimatejw.mjcn.domain.model.NoticePage
import com.ultimatejw.mjcn.domain.repository.NoticeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

class NoticeRepositoryImpl @Inject constructor(
    private val noticeDao: NoticeDao,
    private val apiService: MjcnApiService
) : NoticeRepository {

    override fun getAllNotices(): Flow<List<Notice>> =
        noticeDao.getAllNotices().map { entities -> entities.map { it.toDomain() } }

    override fun getNoticesByCategory(category: String): Flow<List<Notice>> =
        noticeDao.getNoticesByCategory(category).map { entities -> entities.map { it.toDomain() } }

    override suspend fun refreshNotices(notices: List<Notice>) {
        noticeDao.clearAll()
        noticeDao.insertAll(notices.map { it.toEntity() })
    }

    override suspend fun toggleBookmark(id: String, bookmarked: Boolean) {
        noticeDao.updateBookmark(id, bookmarked)
    }

    override suspend fun fetchNoticesPage(
        page: Int,
        pageSize: Int,
        view: String?,
        source: String?,
        q: String?
    ): Result<NoticePage> = runCatching {
        val response = apiService.getNotices(
            page = page,
            pageSize = pageSize,
            view = view,
            source = source,
            q = q
        )
        val body = response.body()
            ?: error("공지 목록 응답이 비어있습니다 (${response.code()})")
        NoticePage(
            notices = body.results.map { it.toDomain() },
            totalCount = body.count,
            hasMore = body.next != null
        )
    }

    private fun NoticeListItemDto.toDomain() = Notice(
        id = id.toString(),
        title = titleWithoutDept.takeIf { it.isNotBlank() } ?: title,
        category = sourceToCategory(source),
        team = departmentDisplay,
        date = formatPublishedAt(publishedAt),
        url = url,
        summary = summary.orEmpty()
    )

    private fun sourceToCategory(source: String?): String = when (source) {
        "academic"        -> "학사"
        "general"         -> "일반"
        "event"           -> "행사"
        "scholarship"     -> "장학/학자금"
        "overseas"        -> "해외"
        "student_activity"-> "학생활동"
        "career"          -> "진로/취업/창업"
        "contest"         -> "공모전"
        "opentalk"        -> "일반"
        else              -> "일반"
    }

    private fun formatPublishedAt(publishedAt: String?): String {
        publishedAt ?: return ""
        return try {
            // API returns ISO 8601 like "2026-05-27T10:30:00.123456Z"
            // Take first 19 chars to get "2026-05-27T10:30:00" and parse as UTC
            val normalized = publishedAt.take(19).replace("T", " ")
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .apply { timeZone = TimeZone.getTimeZone("UTC") }
            val date = sdf.parse(normalized) ?: return publishedAt.take(10)

            val diffMs = System.currentTimeMillis() - date.time
            val diffMin = diffMs / 60_000
            val diffHour = diffMs / 3_600_000
            val diffDay = diffMs / 86_400_000
            when {
                diffMin < 60  -> "${diffMin}분 전"
                diffHour < 24 -> "${diffHour}시간 전"
                diffDay < 7   -> "${diffDay}일 전"
                else          -> publishedAt.take(10)
            }
        } catch (e: Exception) {
            publishedAt.take(10)
        }
    }
}
