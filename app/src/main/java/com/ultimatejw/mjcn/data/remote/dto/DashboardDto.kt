package com.ultimatejw.mjcn.data.remote.dto

import com.google.gson.annotations.SerializedName

data class DashboardDto(
    val greeting: GreetingDto,
    @SerializedName("graduation_progress_percent") val graduationProgressPercent: Int?,
    @SerializedName("today_schedule") val todaySchedule: List<TodayScheduleDto>,
    val notices: List<NoticeListItemDto>,
    val information: List<InformationListItemDto>,
    @SerializedName("unread_notification_count") val unreadNotificationCount: Int?
)

data class GreetingDto(
    @SerializedName("user_name") val userName: String,
    val weekday: String,
    @SerializedName("today_class_count") val todayClassCount: Int
)

data class TodayScheduleDto(
    val id: Int,
    @SerializedName("course_name") val courseName: String,
    @SerializedName("course_code") val courseCode: String,
    @SerializedName("day_of_week") val dayOfWeek: String,
    @SerializedName("start_time") val startTime: String,
    @SerializedName("end_time") val endTime: String,
    val professor: String?,
    val room: String?,
    val building: String?
)

data class NoticeListItemDto(
    val id: Int,
    val source: String,
    @SerializedName("source_label") val sourceLabel: String,
    val department: String?,
    @SerializedName("department_display") val departmentDisplay: String,
    val title: String,
    @SerializedName("title_without_dept") val titleWithoutDept: String,
    val summary: String?,
    @SerializedName("published_at") val publishedAt: String,
    @SerializedName("end_date") val endDate: String?,
    val url: String,
    @SerializedName("match_score") val matchScore: Int?
)

data class InformationListItemDto(
    val id: Int,
    val title: String,
    val organizer: String?,
    val url: String,
    @SerializedName("start_date") val startDate: String?,
    @SerializedName("end_date") val endDate: String?,
    val categories: List<String>?,
    @SerializedName("is_active") val isActive: Boolean?,
    val source: String?,
    @SerializedName("source_id") val sourceId: String?,
    @SerializedName("match_score") val matchScore: Int?,
    @SerializedName("d_day") val dDay: Int?
)
