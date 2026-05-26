package com.ultimatejw.mjcn.data.remote.dto.course

import com.google.gson.annotations.SerializedName

data class CourseHistoryRequest(
    @SerializedName("course_name") val courseName: String,
    @SerializedName("course_code") val courseCode: String,
    @SerializedName("year") val year: Int,
    @SerializedName("semester") val semester: Int,
    @SerializedName("grade_received") val gradeReceived: String = "",
    @SerializedName("category") val category: String,
    @SerializedName("credits") val credits: Int
)
