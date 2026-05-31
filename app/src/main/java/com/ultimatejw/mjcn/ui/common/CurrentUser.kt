package com.ultimatejw.mjcn.ui.common

/** 더미 사용자 정보. API 연결 시 이 값들만 ViewModel/저장소에서 받아오면 됨. */
object CurrentUser {
    val name = "김지현"
    val major = "컴퓨터공학과"
    val grade = 3
    val semester = 1
    val gradeSemester = "${grade}학년 ${semester}학기"
    val gradeSemesterShort = "${grade}-${semester}학기"
    val honorific = "${name}님"
}
