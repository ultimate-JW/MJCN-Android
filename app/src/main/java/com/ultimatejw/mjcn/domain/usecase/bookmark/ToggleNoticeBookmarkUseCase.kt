package com.ultimatejw.mjcn.domain.usecase.bookmark

import com.ultimatejw.mjcn.domain.model.Notice
import com.ultimatejw.mjcn.domain.repository.BookmarkRepository
import javax.inject.Inject

class ToggleNoticeBookmarkUseCase @Inject constructor(
    private val repository: BookmarkRepository
) {
    suspend operator fun invoke(notice: Notice) = repository.toggleNoticeBookmark(notice)
}
