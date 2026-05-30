package com.ultimatejw.mjcn.data.repository

import com.ultimatejw.mjcn.R
import com.ultimatejw.mjcn.data.remote.MjcnApiService
import com.ultimatejw.mjcn.data.remote.dto.ThemeListItemDto
import com.ultimatejw.mjcn.domain.model.Theme
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

    override suspend fun fetchThemeDetail(id: Int): Result<List<ThemeItem>> = runCatching {
        val response = apiService.getThemeDetail(id)
        val body = response.body() ?: error("테마 상세 응답이 비어있습니다 (${response.code()})")
        body.items.map { dto ->
            ThemeItem(
                id = dto.id,
                title = dto.title,
                content = dto.content.orEmpty(),
                externalUrl = dto.externalUrl,
                itemType = dto.itemType
            )
        }
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

    private fun categoryToIcon(category: String): Pair<Int, String> = when (category) {
        "course_registration" -> R.drawable.ic_hat to "#E1F5EE"
        "career"              -> R.drawable.ic_bag to "#E6F1FB"
        "exchange"            -> R.drawable.ic_plane to "#EAF3DE"
        "grant"               -> R.drawable.ic_donate to "#FAEEDA"
        "academic"            -> R.drawable.ic_heart to "#FBEAF0"
        else                  -> R.drawable.ic_theme to "#F0F0F0"
    }
}
