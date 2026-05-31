package com.ultimatejw.mjcn.data.remote.dto.course

import com.google.gson.annotations.SerializedName

data class CurrentCourseRequest(
    @SerializedName("course_name") val courseName: String,
    @SerializedName("course_code") val courseCode: String,
    @SerializedName("day_of_week") val dayOfWeek: String,
    @SerializedName("start_time") val startTime: String,
    @SerializedName("end_time") val endTime: String,
    @SerializedName("professor") val professor: String = "",
    @SerializedName("room") val room: String = "",
    @SerializedName("building") val building: String = ""
)
