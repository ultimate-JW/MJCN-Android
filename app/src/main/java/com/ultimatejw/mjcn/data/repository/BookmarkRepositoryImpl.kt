package com.ultimatejw.mjcn.data.repository

import com.ultimatejw.mjcn.domain.model.Info
import com.ultimatejw.mjcn.domain.model.Notice
import com.ultimatejw.mjcn.domain.repository.BookmarkRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookmarkRepositoryImpl @Inject constructor() : BookmarkRepository {

    private val _notices = MutableStateFlow<Map<String, Notice>>(emptyMap())
    private val _infos   = MutableStateFlow<Map<String, Info>>(emptyMap())

    override fun observeBookmarkedNotices(): Flow<List<Notice>> =
        _notices.map { it.values.toList() }

    override fun observeBookmarkedInfos(): Flow<List<Info>> =
        _infos.map { it.values.toList() }

    override suspend fun toggleNoticeBookmark(notice: Notice) {
        _notices.update { map ->
            if (map.containsKey(notice.id)) map - notice.id
            else map + (notice.id to notice.copy(isBookmarked = true))
        }
    }

    override suspend fun toggleInfoBookmark(info: Info) {
        _infos.update { map ->
            if (map.containsKey(info.id)) map - info.id
            else map + (info.id to info.copy(isBookmarked = true))
        }
    }
}
