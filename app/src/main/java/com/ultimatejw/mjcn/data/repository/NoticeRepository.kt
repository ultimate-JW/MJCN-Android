package com.ultimatejw.mjcn.data.repository

import com.ultimatejw.mjcn.data.local.dao.NoticeDao
import com.ultimatejw.mjcn.data.local.entity.toEntity
import com.ultimatejw.mjcn.domain.model.Notice
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class NoticeRepository @Inject constructor(private val noticeDao: NoticeDao) {

    fun getAllNotices(): Flow<List<Notice>> =
        noticeDao.getAllNotices().map { entities -> entities.map { it.toDomain() } }

    fun getNoticesByCategory(category: String): Flow<List<Notice>> =
        noticeDao.getNoticesByCategory(category).map { entities -> entities.map { it.toDomain() } }

    suspend fun refreshNotices(notices: List<Notice>) {
        noticeDao.clearAll()
        noticeDao.insertAll(notices.map { it.toEntity() })
    }

    suspend fun toggleBookmark(id: String, bookmarked: Boolean) {
        noticeDao.updateBookmark(id, bookmarked)
    }
}
