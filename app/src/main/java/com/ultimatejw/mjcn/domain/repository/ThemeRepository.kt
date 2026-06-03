package com.ultimatejw.mjcn.domain.repository

import com.ultimatejw.mjcn.domain.model.ContestGuide
import com.ultimatejw.mjcn.domain.model.CourseRecommendSections
import com.ultimatejw.mjcn.domain.model.ExchangeGuide
import com.ultimatejw.mjcn.domain.model.StudyTips
import com.ultimatejw.mjcn.domain.model.Theme
import com.ultimatejw.mjcn.domain.model.ThemeDetailData

interface ThemeRepository {
    suspend fun fetchThemes(): Result<List<Theme>>
    suspend fun fetchThemeDetail(id: Int): Result<ThemeDetailData>
    suspend fun fetchCourseRecommend(): Result<CourseRecommendSections>
    suspend fun fetchExchangeGuide(): Result<ExchangeGuide>
    suspend fun fetchContestGuide(): Result<ContestGuide>
    suspend fun fetchStudyTips(): Result<StudyTips>
}
