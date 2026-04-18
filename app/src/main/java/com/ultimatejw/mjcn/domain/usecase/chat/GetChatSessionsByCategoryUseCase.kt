package com.ultimatejw.mjcn.domain.usecase.chat

import com.ultimatejw.mjcn.domain.model.ChatSession
import com.ultimatejw.mjcn.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetChatSessionsByCategoryUseCase @Inject constructor(
    private val repository: ChatRepository
) {
    operator fun invoke(category: String): Flow<List<ChatSession>> =
        repository.getSessionsByCategory(category)
}
