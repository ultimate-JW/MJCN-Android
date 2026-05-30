package com.ultimatejw.mjcn.domain.repository

import com.ultimatejw.mjcn.domain.model.Theme
import com.ultimatejw.mjcn.domain.model.ThemeDetailData

interface ThemeRepository {
    suspend fun fetchThemes(): Result<List<Theme>>
    suspend fun fetchThemeDetail(id: Int): Result<ThemeDetailData>
}
