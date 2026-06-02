package com.ultimatejw.mjcn.data.remote.dto.profile

import com.google.gson.annotations.SerializedName

data class InterestAreaDto(
    @SerializedName("id") val id: Int,
    @SerializedName("category") val category: String,
    @SerializedName("custom_text") val customText: String? = null
)

data class CourseHistoryDto(
    @SerializedName("id") val id: Int,
    @SerializedName("course_code") val courseCode: String,
    @SerializedName("year") val year: Int,
    @SerializedName("semester") val semester: Int,
    @SerializedName("grade_received") val gradeReceived: String? = null,
    @SerializedName("course_name") val courseName: String,
    @SerializedName("category") val category: String,
    @SerializedName("credits") val credits: Int
)

data class CurrentCourseDto(
    @SerializedName("id") val id: Int,
    @SerializedName("course_name") val courseName: String,
    @SerializedName("course_code") val courseCode: String,
    @SerializedName("day_of_week") val dayOfWeek: String,
    @SerializedName("professor") val professor: String
)
