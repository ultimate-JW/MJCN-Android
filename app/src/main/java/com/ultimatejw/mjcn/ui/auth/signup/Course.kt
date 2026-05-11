package com.ultimatejw.mjcn.ui.auth.signup

/** 수강 이력 화면에서 표시할 과목 정보 */
data class Course(
    val name: String,
    val meta: String
)

/** 사용자가 수강 이력으로 선택한 과목과 (선택적으로) 성적 */
data class SelectedCourse(
    val name: String,
    var grade: String? = null
)
