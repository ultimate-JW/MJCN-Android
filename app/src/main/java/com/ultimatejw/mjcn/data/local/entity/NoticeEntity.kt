package com.ultimatejw.mjcn.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ultimatejw.mjcn.domain.model.Notice

@Entity(tableName = "notices")
data class NoticeEntity(
    @PrimaryKey val id: String,
    val title: String,
    val category: String,
    val team: String,
    val date: String,
    val isBookmarked: Boolean = false
) {
    fun toDomain() = Notice(
        id = id,
        title = title,
        category = category,
        team = team,
        date = date,
        isBookmarked = isBookmarked
    )
}

fun Notice.toEntity() = NoticeEntity(
    id = id,
    title = title,
    category = category,
    team = team,
    date = date,
    isBookmarked = isBookmarked
)
