package com.ultimatejw.mjcn.ui.main.theme

/** 수강 과목 추천 한 줄 (theme 1) */
data class ThemeCourse(val title: String, val schedule: String)

/** 평가 항목 (theme 2~4 의 "현재 ~ 평가" 영역 아이템) */
data class ThemeAssessment(val title: String, val subtitle: String)

/** 단계별 로드맵 STEP 한 칸 (theme 2~4) */
data class ThemeRoadmapStep(val title: String, val body: String)

/** 국가 지원 사업 한 개 (theme 4 의 프로그램 카드) */
data class ThemeProgram(
    val title: String,
    val what: String,
    val benefits: String,
    val requirements: String,
)
