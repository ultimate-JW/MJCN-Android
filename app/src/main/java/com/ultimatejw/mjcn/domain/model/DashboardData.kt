package com.ultimatejw.mjcn.domain.model

data class DashboardData(
    val userName: String,
    val todayClasses: List<TodayClass>,
    val notices: List<Notice>,
    val infoList: List<Info>,
    val unreadNotificationCount: Int,
    val graduationProgressPercent: Int
)
