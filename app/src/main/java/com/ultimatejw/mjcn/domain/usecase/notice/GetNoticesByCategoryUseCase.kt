package com.ultimatejw.mjcn.domain.usecase.notice

import com.ultimatejw.mjcn.domain.model.Notice
import com.ultimatejw.mjcn.domain.repository.NoticeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetNoticesByCategoryUseCase @Inject constructor(
    private val repository: NoticeRepository
) {
    operator fun invoke(category: String): Flow<List<Notice>> =
        repository.getNoticesByCategory(category)
}
