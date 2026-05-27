package com.ultimatejw.mjcn.domain.repository

import com.ultimatejw.mjcn.domain.model.ChatMessage
import com.ultimatejw.mjcn.domain.model.ChatRoomDetail
import com.ultimatejw.mjcn.domain.model.ChatSession

interface ChatRepository {
    suspend fun getChatRooms(): Result<List<ChatSession>>
    suspend fun createChatRoom(): Result<ChatSession>
    suspend fun getChatRoomDetail(id: String): Result<ChatRoomDetail>
    suspend fun deleteChatRoom(id: String): Result<Unit>
    suspend fun sendMessage(roomId: String, content: String): Result<ChatMessage>
}
