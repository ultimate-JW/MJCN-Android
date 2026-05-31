package com.ultimatejw.mjcn.domain.model

data class ChatSession(
    val id: String,
    val title: String,
    val lastMessage: String,
    val category: String,
    val updatedAt: String
)

data class ReferencedItem(
    val type: String,   // "notice" | "information"
    val title: String,
    val url: String
)

data class ChatMessage(
    val id: String,
    val role: String,
    val content: String,
    val referencedItems: List<ReferencedItem> = emptyList(),
    val createdAt: String
) {
    val isFromUser: Boolean get() = role == "user"
}

data class ChatRoomDetail(
    val id: String,
    val title: String,
    val category: String,
    val messages: List<ChatMessage>
)
