package com.ultimatejw.mjcn.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ultimatejw.mjcn.domain.model.Notice

@Entity(tableName = "notices")
data class NoticeEntity(
    @PrimaryKey val id: String,
    val title: String,
    val summary: String,
    val content: String,
    val category: String,
    val source: String,
    val date: String,
    val isBookmarked: Boolean = false,
    val hasSummary: Boolean = false
) {
    fun toDomain() = Notice(
        id = id,
        title = title,
        summary = summary,
        content = content,
        category = category,
        source = source,
        date = date,
        isBookmarked = isBookmarked,
        hasSummary = hasSummary
    )
}

fun Notice.toEntity() = NoticeEntity(
    id = id,
    title = title,
    summary = summary,
    content = content,
    category = category,
    source = source,
    date = date,
    isBookmarked = isBookmarked,
    hasSummary = hasSummary
)
