package com.ultimatejw.mjcn.ui.common

import com.ultimatejw.mjcn.domain.model.User

object CurrentUser {
    var name = ""
    var major = ""
    var grade = 0
    var semester = 0
    val gradeSemester get() = "${grade}학년 ${semester}학기"
    val gradeSemesterShort get() = "${grade}-${semester}학기"
    val honorific get() = if (name.isNotBlank()) "${name}님" else "사용자님"

    fun update(user: User) {
        name = user.name
        major = user.major ?: user.department ?: ""
        grade = user.grade
        semester = user.semester
    }
}
