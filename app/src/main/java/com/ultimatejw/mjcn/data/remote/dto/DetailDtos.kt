package com.ultimatejw.mjcn.data.remote.dto

import com.google.gson.annotations.SerializedName

data class AiCardDto(
    val title: String?,
    val items: List<String>?
)

data class AiResultDto(
    @SerializedName("notice_type") val noticeType: String?,
    val summary: String?,
    val cards: List<AiCardDto>?,
    val status: String?
)

data class NoticeDetailDto(
    val id: Int?,
    val source: String?,
    @SerializedName("source_label") val sourceLabel: String?,
    val department: String?,
    @SerializedName("department_display") val departmentDisplay: String?,
    val title: String?,
    @SerializedName("title_without_dept") val titleWithoutDept: String?,
    val summary: String?,
    @SerializedName("published_at") val publishedAt: String?,
    @SerializedName("end_date") val endDate: String?,
    val url: String?,
    val content: String?,
    @SerializedName("ai_result") val aiResult: AiResultDto?
)

data class InformationDetailDto(
    val id: Int?,
    val title: String?,
    val organizer: String?,
    val url: String?,
    @SerializedName("start_date") val startDate: String?,
    @SerializedName("end_date") val endDate: String?,
    val categories: List<String>?,
    @SerializedName("is_active") val isActive: Boolean?,
    val description: String?,
    @SerializedName("d_day") val dDay: Int?
)
