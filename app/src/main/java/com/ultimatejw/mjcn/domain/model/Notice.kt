package com.ultimatejw.mjcn.domain.model

data class NoticeCard(val title: String, val items: List<String>)

data class NoticePage(
    val notices: List<Notice>,
    val totalCount: Int,
    val hasMore: Boolean
)

data class Notice(
    val id: String,
    val title: String,
    val category: String,
    val team: String,
    val date: String,
    val url: String = "",
    val summary: String = "",
    val cards: List<NoticeCard> = emptyList(),
    val isBookmarked: Boolean = false
)

enum class NoticeCategory(val label: String) {
    NORMAL("일반"),
    ACADEMIC("학사"),
    OVERSEAS("해외"),
    CONTEST("공모전"),
    ACTIVITY("학생활동"),
    CAREER("진로/취업/창업"),
    CAREER_SIMPLE("취업"),
    SCHOLARSHIP("장학/학자금"),
    SCHOLARSHIP_SIMPLE("장학");

    companion object {
        fun from(label: String) = entries.find { it.label == label } ?: NORMAL
    }
}
