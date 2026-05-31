package com.ultimatejw.mjcn.domain.usecase.chat

import com.ultimatejw.mjcn.domain.model.ChatSession
import com.ultimatejw.mjcn.domain.repository.ChatRepository
import javax.inject.Inject

class GetChatSessionsByCategoryUseCase @Inject constructor(
    private val repository: ChatRepository
) {
    suspend operator fun invoke(): Result<List<ChatSession>> = repository.getChatRooms()
}
