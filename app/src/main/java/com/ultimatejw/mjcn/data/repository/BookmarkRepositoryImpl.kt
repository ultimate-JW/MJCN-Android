package com.ultimatejw.mjcn.data.repository

import com.ultimatejw.mjcn.data.local.dao.InfoDao
import com.ultimatejw.mjcn.data.local.dao.NoticeDao
import com.ultimatejw.mjcn.data.local.entity.toEntity
import com.ultimatejw.mjcn.domain.model.Info
import com.ultimatejw.mjcn.domain.model.Notice
import com.ultimatejw.mjcn.domain.repository.BookmarkRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookmarkRepositoryImpl @Inject constructor(
    private val noticeDao: NoticeDao,
    private val infoDao: InfoDao,
) : BookmarkRepository {

    override fun observeBookmarkedNotices(): Flow<List<Notice>> =
        noticeDao.getBookmarkedNotices().map { list -> list.map { it.toDomain() } }

    override fun observeBookmarkedInfos(): Flow<List<Info>> =
        infoDao.getBookmarkedInfos().map { list -> list.map { it.toDomain() } }

    override suspend fun toggleNoticeBookmark(notice: Notice) {
        if (notice.isBookmarked) {
            noticeDao.updateBookmark(notice.id, false)
        } else {
            noticeDao.insert(notice.toEntity().copy(isBookmarked = true))
        }
    }

    override suspend fun toggleInfoBookmark(info: Info) {
        if (info.isBookmarked) {
            infoDao.updateBookmark(info.id, false)
        } else {
            infoDao.insert(info.toEntity().copy(isBookmarked = true))
        }
    }
}
