package com.ultimatejw.mjcn.ui.auth.signup

/** 성적 대신 이수 횟수를 입력해야 하는 특수 과목(채플)의 이름. */
const val CHAPEL_COURSE_NAME = "채플"

/**
 * 수강 이력/현재 수강 과목 화면에서 표시할 과목 정보.
 * @param code 학수번호 등 과목명 앞에 붙는 접두 코드. 비어있으면 카드에는 과목명만 표시된다.
 */
data class Course(
    val name: String,
    val meta: String,
    val code: String = ""
)

/** 사용자가 선택한 과목과 (선택적으로) 성적. meta는 전공/교양 분류 판단용. */
data class SelectedCourse(
    val name: String,
    var grade: String? = null,
    val meta: String = ""
)
