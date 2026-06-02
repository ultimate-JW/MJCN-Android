package com.ultimatejw.mjcn.data.repository

import com.ultimatejw.mjcn.R
import com.ultimatejw.mjcn.data.remote.MjcnApiService
import com.ultimatejw.mjcn.data.remote.dto.RecommendScheduleDto
import com.ultimatejw.mjcn.data.remote.dto.ThemeListItemDto
import com.ultimatejw.mjcn.domain.model.CourseRecommendSections
import com.ultimatejw.mjcn.domain.model.ExchangeEvaluationItem
import com.ultimatejw.mjcn.domain.model.ExchangeGuide
import com.ultimatejw.mjcn.domain.model.ExchangeNecessityItem
import com.ultimatejw.mjcn.domain.model.ExchangeRecommendItem
import com.ultimatejw.mjcn.domain.model.QuickQuestion
import com.ultimatejw.mjcn.domain.model.RecommendCourse
import com.ultimatejw.mjcn.domain.model.RecommendOffering
import com.ultimatejw.mjcn.domain.model.Theme
import com.ultimatejw.mjcn.domain.model.ThemeDetailData
import com.ultimatejw.mjcn.domain.model.ThemeItem
import com.ultimatejw.mjcn.domain.repository.ThemeRepository
import javax.inject.Inject

class ThemeRepositoryImpl @Inject constructor(
    private val apiService: MjcnApiService
) : ThemeRepository {

    override suspend fun fetchThemes(): Result<List<Theme>> = runCatching {
        val response = apiService.getThemes(pageSize = 20)
        val body = response.body() ?: error("테마 목록 응답이 비어있습니다 (${response.code()})")
        body.results.map { it.toDomain() }
    }

    override suspend fun fetchThemeDetail(id: Int): Result<ThemeDetailData> = runCatching {
        val response = apiService.getThemeDetail(id)
        val body = response.body() ?: error("테마 상세 응답이 비어있습니다 (${response.code()})")
        ThemeDetailData(
            id = body.id,
            title = body.title,
            category = body.category,
            description = body.description.orEmpty(),
            items = body.items.map { dto ->
                ThemeItem(
                    id = dto.id,
                    title = dto.title,
                    content = dto.content.orEmpty(),
                    externalUrl = dto.externalUrl?.takeIf { it.isNotBlank() },
                    itemType = dto.itemType
                )
            }
        )
    }

    private fun ThemeListItemDto.toDomain(): Theme {
        val (iconRes, iconBgColor) = categoryToIcon(category)
        return Theme(
            id = id,
            title = title,
            subtitle = description.orEmpty(),
            category = category,
            iconRes = iconRes,
            iconBgColor = iconBgColor
        )
    }

    override suspend fun fetchCourseRecommend(): Result<CourseRecommendSections> = runCatching {
        val response = apiService.getCourseRecommendSections()
        val body = response.body() ?: error("수강신청 추천 응답이 비어있습니다 (${response.code()})")
        CourseRecommendSections(
            adviceText = body.advice.text.orEmpty(),
            interestCourses = body.interestCourses.map { course ->
                RecommendCourse(
                    courseCode = course.courseCode,
                    name = course.name,
                    category = course.category,
                    credits = course.credits,
                    offerings = course.offerings.map { offering ->
                        RecommendOffering(
                            id = offering.id,
                            sectionNo = offering.sectionNo,
                            professor = offering.professor,
                            scheduleText = offering.schedules.toScheduleText()
                        )
                    }
                )
            },
            linkedCourses = body.linkedCourses.map { course ->
                RecommendCourse(
                    courseCode = course.courseCode,
                    name = course.name,
                    category = course.category,
                    credits = course.credits,
                    offerings = course.offerings.map { offering ->
                        RecommendOffering(
                            id = offering.id,
                            sectionNo = offering.sectionNo,
                            professor = offering.professor,
                            scheduleText = offering.schedules.toScheduleText()
                        )
                    }
                )
            },
            quickQuestions = body.quickQuestions.map { QuickQuestion(it.label, it.prompt) }
        )
    }

    override suspend fun fetchExchangeGuide(): Result<ExchangeGuide> = runCatching {
        val response = apiService.getExchangeGuide()
        val body = response.body() ?: error("교환학생 가이드 응답이 비어있습니다 (${response.code()})")
        ExchangeGuide(
            adviceText = body.advice.stageMessage,
            necessity = body.necessity.map {
                ExchangeNecessityItem(it.option, it.icon, it.score, it.reason)
            },
            evaluation = body.evaluation.map {
                ExchangeEvaluationItem(it.option, it.icon, it.fits, it.caveat, it.interestNote)
            },
            currentRecommendation = body.currentRecommendation.map {
                ExchangeRecommendItem(it.rank, it.title, it.note)
            },
            quickQuestions = body.quickQuestions.map { QuickQuestion(it.label, it.prompt) }
        )
    }

    private fun List<RecommendScheduleDto>.toScheduleText(): String =
        joinToString(" · ") { "${it.dayOfWeek}(${it.startTime.take(5)}~${it.endTime.take(5)})" }

    private fun categoryToIcon(category: String): Pair<Int, String> = when (category) {
        "course_registration" -> R.drawable.ic_hat to "#E1F5EE"
        "career"              -> R.drawable.ic_bag to "#E6F1FB"
        "exchange"            -> R.drawable.ic_plane to "#EAF3DE"
        "grant"               -> R.drawable.ic_donate to "#FAEEDA"
        "academic"            -> R.drawable.ic_heart to "#FBEAF0"
        else                  -> R.drawable.ic_theme to "#F0F0F0"
    }
}
