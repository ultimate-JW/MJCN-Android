package com.ultimatejw.mjcn.data.repository

import com.ultimatejw.mjcn.data.remote.MjcnApiService
import com.ultimatejw.mjcn.data.remote.dto.ChatMessageCreateDto
import com.ultimatejw.mjcn.data.remote.dto.ChatMessageDto
import com.ultimatejw.mjcn.data.remote.dto.ChatRoomListDto
import com.ultimatejw.mjcn.domain.model.ChatMessage
import com.ultimatejw.mjcn.domain.model.ChatRoomDetail
import com.ultimatejw.mjcn.domain.model.ChatSession
import com.ultimatejw.mjcn.domain.repository.ChatRepository
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

class ChatRepositoryImpl @Inject constructor(
    private val apiService: MjcnApiService
) : ChatRepository {

    override suspend fun getChatRooms(): Result<List<ChatSession>> = runCatching {
        val response = apiService.getChatRooms()
        val body = response.body() ?: error("채팅방 목록 응답이 비어있습니다 (${response.code()})")
        body.results.map { it.toDomain() }
    }

    override suspend fun createChatRoom(): Result<ChatSession> = runCatching {
        val response = apiService.createChatRoom()
        val body = response.body() ?: error("채팅방 생성 실패 (${response.code()})")
        body.toDomain()
    }

    override suspend fun getChatRoomDetail(id: String): Result<ChatRoomDetail> = runCatching {
        val response = apiService.getChatRoomDetail(id)
        val body = response.body() ?: error("채팅방 상세 응답이 비어있습니다 (${response.code()})")
        ChatRoomDetail(
            id = body.id.toString(),
            title = body.title,
            category = body.category,
            messages = body.messages.map { it.toDomain() }
        )
    }

    override suspend fun deleteChatRoom(id: String): Result<Unit> = runCatching {
        val response = apiService.deleteChatRoom(id)
        if (!response.isSuccessful) error("삭제 실패 (${response.code()})")
    }

    override suspend fun sendMessage(roomId: String, content: String): Result<ChatMessage> = runCatching {
        val response = apiService.sendChatMessage(roomId, ChatMessageCreateDto(content = content))
        val body = response.body() ?: error("메시지 전송 실패 (${response.code()})")
        body.toDomain()
    }

    private fun ChatRoomListDto.toDomain() = ChatSession(
        id = id.toString(),
        title = title,
        lastMessage = lastMessagePreview,
        category = category,
        updatedAt = formatUpdatedAt(updatedAt)
    )

    private fun ChatMessageDto.toDomain() = ChatMessage(
        id = id.toString(),
        role = role,
        content = content,
        createdAt = createdAt.take(19).replace("T", " ")
    )

    private fun formatUpdatedAt(isoString: String): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                .apply { timeZone = TimeZone.getTimeZone("UTC") }
            val date = sdf.parse(isoString.take(19)) ?: return isoString.take(10)
            val diffMs = System.currentTimeMillis() - date.time
            val diffMin = diffMs / 60_000
            val diffHour = diffMs / 3_600_000
            val diffDay = diffMs / 86_400_000
            when {
                diffMin < 1   -> "방금 전"
                diffMin < 60  -> "${diffMin}분 전"
                diffHour < 24 -> "${diffHour}시간 전"
                diffDay == 1L -> "어제"
                diffDay < 7   -> "${diffDay}일 전"
                else          -> isoString.take(10)
            }
        } catch (e: Exception) {
            isoString.take(10)
        }
    }
}
