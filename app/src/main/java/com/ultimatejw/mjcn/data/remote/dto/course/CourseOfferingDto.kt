package com.ultimatejw.mjcn.data.remote.dto.course

import com.google.gson.annotations.SerializedName

data class CourseScheduleDto(
    @SerializedName("day_of_week") val dayOfWeek: String,
    @SerializedName("start_time") val startTime: String,
    @SerializedName("end_time") val endTime: String,
    @SerializedName("room") val room: String? = null
)

data class CourseOfferingDto(
    @SerializedName("id") val id: Int,
    @SerializedName("year") val year: Int,
    @SerializedName("semester") val semester: Int,
    @SerializedName("section_no") val sectionNo: String,
    @SerializedName("course_code") val courseCode: String,
    @SerializedName("name") val name: String,
    @SerializedName("college") val college: String,
    @SerializedName("department") val department: String? = null,
    @SerializedName("major") val major: String? = null,
    @SerializedName("category") val category: String,
    @SerializedName("credits") val credits: Int,
    @SerializedName("professor") val professor: String? = null,
    @SerializedName("schedules") val schedules: List<CourseScheduleDto>
)
