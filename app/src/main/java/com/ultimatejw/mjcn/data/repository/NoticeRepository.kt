package com.ultimatejw.mjcn.data.repository

import com.ultimatejw.mjcn.data.local.dao.NoticeDao
import com.ultimatejw.mjcn.data.model.Notice
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class NoticeRepository @Inject constructor(private val noticeDao: NoticeDao) {

    fun getAllNotices(): Flow<List<Notice>> = noticeDao.getAllNotices()

    fun getNoticesByCategory(category: String): Flow<List<Notice>> =
        noticeDao.getNoticesByCategory(category)

    suspend fun refreshNotices(notices: List<Notice>) {
        noticeDao.clearAll()
        noticeDao.insertAll(notices)
    }

    suspend fun toggleBookmark(id: String, bookmarked: Boolean) {
        noticeDao.updateBookmark(id, bookmarked)
    }
}
