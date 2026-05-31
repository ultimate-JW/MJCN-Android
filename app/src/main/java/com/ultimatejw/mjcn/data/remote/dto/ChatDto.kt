package com.ultimatejw.mjcn.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ChatRoomListDto(
    val id: Int,
    val title: String,
    val category: String,
    @SerializedName("last_message_preview") val lastMessagePreview: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)

data class PaginatedChatRoomDto(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<ChatRoomListDto>
)

data class ReferencedItemDto(
    val type: String,
    val title: String,
    val url: String
)

data class ChatAttachmentDto(
    val id: Int,
    val file: String,
    @SerializedName("file_type") val fileType: String,
    @SerializedName("original_name") val originalName: String,
    @SerializedName("created_at") val createdAt: String
)

data class ChatMessageDto(
    val id: Int,
    val role: String,
    val content: String,
    @SerializedName("referenced_items") val referencedItems: List<ReferencedItemDto>? = null,
    val attachments: List<ChatAttachmentDto>? = null,
    @SerializedName("created_at") val createdAt: String
)

data class ChatRoomDetailDto(
    val id: Int,
    val title: String,
    val category: String,
    @SerializedName("last_message_preview") val lastMessagePreview: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String,
    val messages: List<ChatMessageDto>
)

data class ChatMessageCreateDto(
    val content: String
)
