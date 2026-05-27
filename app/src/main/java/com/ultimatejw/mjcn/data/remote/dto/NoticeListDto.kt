package com.ultimatejw.mjcn.data.remote.dto

data class PaginatedNoticeDto(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<NoticeListItemDto>
)
