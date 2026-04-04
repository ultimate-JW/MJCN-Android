package com.ultimatejw.mjcn.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notices")
data class Notice(
    @PrimaryKey val id: String,
    val title: String,
    val summary: String,
    val content: String,
    val category: String,
    val source: String,
    val date: String,
    val isBookmarked: Boolean = false,
    val hasSummary: Boolean = false
)

enum class NoticeCategory(val label: String) {
    ALL("전체"),
    ACADEMIC("학사"),
    SCHOLARSHIP("장학"),
    CAREER("취업"),
    EVENT("행사"),
    CONTEST("공모전")
}
