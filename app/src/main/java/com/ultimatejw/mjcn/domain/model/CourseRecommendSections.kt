package com.ultimatejw.mjcn.domain.model

data class CourseRecommendSections(
    val adviceText: String,
    val interestCourses: List<RecommendCourse>,
    val linkedCourses: List<RecommendCourse>,
    val quickQuestions: List<QuickQuestion>
)

data class RecommendCourse(
    val courseCode: String,
    val name: String,
    val category: String,
    val credits: Int,
    val offerings: List<RecommendOffering>
)

data class RecommendOffering(
    val id: Int,
    val sectionNo: String,
    val professor: String,
    val scheduleText: String
)

data class QuickQuestion(
    val label: String,
    val prompt: String
)
