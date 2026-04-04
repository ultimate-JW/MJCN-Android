package com.ultimatejw.mjcn.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_sessions")
data class ChatSession(
    @PrimaryKey val id: String,
    val title: String,
    val lastMessage: String,
    val category: String,
    val updatedAt: String
)

data class ChatMessage(
    val id: String,
    val sessionId: String,
    val content: String,
    val isFromUser: Boolean,
    val createdAt: String
)

enum class ChatCategory(val label: String) {
    ALL("전체"),
    ENROLLMENT("수강신청"),
    SCHOOL_LIFE("학교생활"),
    CAREER("취업·진로"),
    CONTEST("공모전")
}
