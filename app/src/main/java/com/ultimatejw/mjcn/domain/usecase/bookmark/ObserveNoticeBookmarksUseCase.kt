package com.ultimatejw.mjcn.domain.usecase.bookmark

import com.ultimatejw.mjcn.domain.repository.BookmarkRepository
import javax.inject.Inject

class ObserveNoticeBookmarksUseCase @Inject constructor(
    private val repository: BookmarkRepository
) {
    operator fun invoke() = repository.observeBookmarkedNotices()
}
