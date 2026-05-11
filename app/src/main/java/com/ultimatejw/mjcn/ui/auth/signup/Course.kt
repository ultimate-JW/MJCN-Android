package com.ultimatejw.mjcn.ui.auth.signup

/**
 * 수강 이력/현재 수강 과목 화면에서 표시할 과목 정보.
 * @param code 학수번호 등 과목명 앞에 붙는 접두 코드. 비어있으면 카드에는 과목명만 표시된다.
 */
data class Course(
    val name: String,
    val meta: String,
    val code: String = ""
)

/** 사용자가 선택한 과목과 (선택적으로) 성적 */
data class SelectedCourse(
    val name: String,
    var grade: String? = null
)
