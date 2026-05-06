package com.ultimatejw.mjcn.domain.repository

import com.ultimatejw.mjcn.domain.model.Notice
import kotlinx.coroutines.flow.Flow

interface NoticeRepository {
    fun getAllNotices(): Flow<List<Notice>>
    fun getNoticesByCategory(category: String): Flow<List<Notice>>
    suspend fun refreshNotices(notices: List<Notice>)
    suspend fun toggleBookmark(id: String, bookmarked: Boolean)
}
