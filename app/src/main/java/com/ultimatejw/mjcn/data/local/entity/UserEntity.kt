package com.ultimatejw.mjcn.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ultimatejw.mjcn.domain.model.User

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
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
) {
    fun toDomain() = User(
        id = id,
        name = name,
        email = email,
        grade = grade,
        semester = semester,
        graduationYear = graduationYear,
        interests = interests,
        profileImageUrl = profileImageUrl,
        entranceYear = entranceYear,
        graduationDate = graduationDate,
        college = college,
        department = department,
        major = major
    )
}

fun User.toEntity() = UserEntity(
    id = id,
    name = name,
    email = email,
    grade = grade,
    semester = semester,
    graduationYear = graduationYear,
    interests = interests,
    profileImageUrl = profileImageUrl,
    entranceYear = entranceYear,
    graduationDate = graduationDate,
    college = college,
    department = department,
    major = major
)
