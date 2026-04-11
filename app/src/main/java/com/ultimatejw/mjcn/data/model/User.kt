package com.ultimatejw.mjcn.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val grade: Int,
    val semester: Int,
    val graduationYear: Int?,
    val interests: List<String>,
    val profileImageUrl: String? = null,
    // [추가] 전공 선택 관련 필드
    val entranceYear: Int? = null,        // 입학 연도
    val graduationDate: String? = null,   // 졸업 희망 시기
    val college: String? = null,          // 대학
    val department: String? = null,       // 학부/학과
    val major: String? = null             // 전공
)
