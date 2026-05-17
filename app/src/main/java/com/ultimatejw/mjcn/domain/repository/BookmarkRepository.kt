package com.ultimatejw.mjcn.domain.repository

import com.ultimatejw.mjcn.domain.model.Info
import com.ultimatejw.mjcn.domain.model.Notice
import kotlinx.coroutines.flow.Flow

interface BookmarkRepository {
    fun observeBookmarkedNotices(): Flow<List<Notice>>
    fun observeBookmarkedInfos(): Flow<List<Info>>
    suspend fun toggleNoticeBookmark(notice: Notice)
    suspend fun toggleInfoBookmark(info: Info)
}
