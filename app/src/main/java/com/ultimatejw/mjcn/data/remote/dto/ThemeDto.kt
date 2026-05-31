package com.ultimatejw.mjcn.data.remote.dto

import com.google.gson.annotations.SerializedName

data class PaginatedThemeDto(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<ThemeListItemDto>
)

data class ThemeListItemDto(
    val id: Int,
    val title: String,
    val category: String,
    val description: String?,
    val order: Int
)

data class ThemeDetailDto(
    val id: Int,
    val title: String,
    val category: String,
    val description: String?,
    val order: Int,
    @SerializedName("created_at") val createdAt: String?,
    val items: List<ThemeItemDto>
)

data class ThemeItemDto(
    val id: Int,
    val title: String,
    val content: String?,
    @SerializedName("external_url") val externalUrl: String?,
    @SerializedName("item_type") val itemType: String,
    val order: Int
)
