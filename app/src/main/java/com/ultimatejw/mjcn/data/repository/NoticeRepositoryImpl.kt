package com.ultimatejw.mjcn.data.repository

import com.ultimatejw.mjcn.data.local.dao.NoticeDao
import com.ultimatejw.mjcn.data.local.entity.toEntity
import com.ultimatejw.mjcn.domain.model.Notice
import com.ultimatejw.mjcn.domain.repository.NoticeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class NoticeRepositoryImpl @Inject constructor(
    private val noticeDao: NoticeDao
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
}
