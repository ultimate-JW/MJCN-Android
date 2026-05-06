package com.ultimatejw.mjcn.domain.model

data class Info(
    val id: String,
    val title: String,
    val category: String,
    val team: String,
    val isGroup: Boolean,
    val dday: Int,
)

enum class InfoCategory(val label: String) {
    BOOTCAMP("부트캠프"),
    CONTEST("공모전"),
    SUPPORT("지원사업"),
    ACTIVITY("대외활동"),
    EDUCATION("교육/강의");

    companion object {
        fun from(label: String) = entries.find { it.label == label } ?: BOOTCAMP
    }
}
