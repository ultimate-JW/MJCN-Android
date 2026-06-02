package com.ultimatejw.mjcn.data.remote.dto.course

import com.google.gson.annotations.SerializedName

data class CourseListDto(
    @SerializedName("id") val id: Int,
    @SerializedName("course_code") val courseCode: String,
    @SerializedName("name") val name: String,
    @SerializedName("college") val college: String? = null,
    @SerializedName("department") val department: String? = null,
    @SerializedName("major") val major: String? = null,
    @SerializedName("category") val category: String,
    @SerializedName("credits") val credits: Int,
    @SerializedName("professor") val professor: String? = null
)
