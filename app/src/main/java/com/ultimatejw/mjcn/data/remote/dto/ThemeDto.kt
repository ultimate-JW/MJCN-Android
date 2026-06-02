package com.ultimatejw.mjcn.data.remote.dto

import com.google.gson.annotations.SerializedName

data class PaginatedThemeDto(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<ThemeListItemDto>
)

data class ThemeListItemDto(
    val id: Int,
    val title: String,
    val category: String,
    val description: String?,
    val order: Int
)

data class ThemeDetailDto(
    val id: Int,
    val title: String,
    val category: String,
    val description: String?,
    val order: Int,
    @SerializedName("created_at") val createdAt: String?,
    val items: List<ThemeItemDto>
)

data class ThemeItemDto(
    val id: Int,
    val title: String,
    val content: String?,
    @SerializedName("external_url") val externalUrl: String?,
    @SerializedName("item_type") val itemType: String,
    val order: Int
)

// === 수강신청 과목 추천 ===
data class CourseRecommendSectionsDto(
    @SerializedName("target_year") val targetYear: Int,
    @SerializedName("target_semester") val targetSemester: Int,
    val advice: CourseAdviceDto,
    @SerializedName("interest_courses") val interestCourses: List<RecommendCourseDto>,
    @SerializedName("linked_courses") val linkedCourses: List<RecommendCourseDto>,
    @SerializedName("quick_questions") val quickQuestions: List<QuickQuestionDto>
)

data class CourseAdviceDto(
    @SerializedName("user_insight") val userInsight: String?,
    @SerializedName("stage_message") val stageMessage: String?,
    @SerializedName("term_note") val termNote: String?,
    val text: String?
)

data class RecommendCourseDto(
    @SerializedName("course_code") val courseCode: String,
    val name: String,
    val category: String,
    val credits: Int,
    val offerings: List<RecommendOfferingDto>,
    @SerializedName("core_area") val coreArea: String?
)

data class RecommendOfferingDto(
    val id: Int,
    @SerializedName("section_no") val sectionNo: String,
    val professor: String,
    val schedules: List<RecommendScheduleDto>
)

data class RecommendScheduleDto(
    @SerializedName("day_of_week") val dayOfWeek: String,
    @SerializedName("start_time") val startTime: String,
    @SerializedName("end_time") val endTime: String,
    val room: String
)

data class QuickQuestionDto(
    val label: String,
    val prompt: String
)

// === 교환학생·해외 인턴십 가이드 ===
data class ExchangeGuideDto(
    val advice: ExchangeAdviceDto,
    val necessity: List<ExchangeNecessityDto>,
    val evaluation: List<ExchangeEvaluationDto>,
    @SerializedName("current_recommendation") val currentRecommendation: List<ExchangeRecommendDto>,
    @SerializedName("quick_questions") val quickQuestions: List<QuickQuestionDto>
)

data class ExchangeAdviceDto(
    @SerializedName("stage_message") val stageMessage: String
)

data class ExchangeNecessityDto(
    val option: String,
    val icon: String,
    val score: Int,
    val reason: String
)

data class ExchangeEvaluationDto(
    val option: String,
    val icon: String,
    val fits: List<String>,
    val caveat: String?,
    @SerializedName("interest_note") val interestNote: String?
)

data class ExchangeRecommendDto(
    val rank: Int,
    val title: String,
    val note: String
)
