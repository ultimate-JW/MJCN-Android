package com.ultimatejw.mjcn.domain.repository

import com.ultimatejw.mjcn.domain.model.Notice
import com.ultimatejw.mjcn.domain.model.NoticePage
import kotlinx.coroutines.flow.Flow

interface NoticeRepository {
    fun getAllNotices(): Flow<List<Notice>>
    fun getNoticesByCategory(category: String): Flow<List<Notice>>
    suspend fun refreshNotices(notices: List<Notice>)
    suspend fun toggleBookmark(id: String, bookmarked: Boolean)
    suspend fun fetchNoticesPage(
        page: Int,
        pageSize: Int = 10,
        view: String? = null,
        source: String? = null,
        q: String? = null
    ): Result<NoticePage>
}
