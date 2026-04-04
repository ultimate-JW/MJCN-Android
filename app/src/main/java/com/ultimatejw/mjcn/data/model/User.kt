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
    val profileImageUrl: String? = null
)
