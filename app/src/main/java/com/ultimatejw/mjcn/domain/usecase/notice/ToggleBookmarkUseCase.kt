package com.ultimatejw.mjcn.domain.usecase.notice

import com.ultimatejw.mjcn.domain.repository.NoticeRepository
import javax.inject.Inject

class ToggleBookmarkUseCase @Inject constructor(
    private val repository: NoticeRepository
) {
    suspend operator fun invoke(id: String, bookmarked: Boolean) =
        repository.toggleBookmark(id, bookmarked)
}
