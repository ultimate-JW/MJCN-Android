package com.ultimatejw.mjcn.domain.model

data class User(
    val id: String,
    val name: String,
    val email: String,
    val grade: Int,
    val semester: Int,
    val graduationYear: Int?,
    val interests: List<String>,
    val profileImageUrl: String? = null,
    val entranceYear: Int? = null,
    val graduationDate: String? = null,
    val college: String? = null,
    val department: String? = null,
    val major: String? = null
)
