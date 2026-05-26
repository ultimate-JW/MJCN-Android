package com.ultimatejw.mjcn.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ultimatejw.mjcn.domain.model.Info

@Entity(tableName = "infos")
data class InfoEntity(
    @PrimaryKey val id: String,
    val title: String,
    val category: String,
    val team: String,
    val isGroup: Boolean,
    val dday: Int,
    val url: String = "",
    val startDate: String? = null,
    val endDate: String? = null,
    val isBookmarked: Boolean = false
) {
    fun toDomain() = Info(
        id = id,
        title = title,
        category = category,
        team = team,
        isGroup = isGroup,
        dday = dday,
        url = url,
        startDate = startDate,
        endDate = endDate,
        isBookmarked = isBookmarked
    )
}

fun Info.toEntity() = InfoEntity(
    id = id,
    title = title,
    category = category,
    team = team,
    isGroup = isGroup,
    dday = dday,
    url = url,
    startDate = startDate,
    endDate = endDate,
    isBookmarked = isBookmarked
)
