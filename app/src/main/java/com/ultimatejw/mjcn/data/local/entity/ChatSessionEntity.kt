package com.ultimatejw.mjcn.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ultimatejw.mjcn.domain.model.ChatSession

@Entity(tableName = "chat_sessions")
data class ChatSessionEntity(
    @PrimaryKey val id: String,
    val title: String,
    val lastMessage: String,
    val category: String,
    val updatedAt: String
) {
    fun toDomain() = ChatSession(
        id = id,
        title = title,
        lastMessage = lastMessage,
        category = category,
        updatedAt = updatedAt
    )
}

fun ChatSession.toEntity() = ChatSessionEntity(
    id = id,
    title = title,
    lastMessage = lastMessage,
    category = category,
    updatedAt = updatedAt
)
