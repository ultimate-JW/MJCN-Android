package com.ultimatejw.mjcn.domain.usecase.bookmark

import com.ultimatejw.mjcn.domain.model.Info
import com.ultimatejw.mjcn.domain.repository.BookmarkRepository
import javax.inject.Inject

class ToggleInfoBookmarkUseCase @Inject constructor(
    private val repository: BookmarkRepository
) {
    suspend operator fun invoke(info: Info) = repository.toggleInfoBookmark(info)
}
