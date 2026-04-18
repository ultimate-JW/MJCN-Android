package com.ultimatejw.mjcn.domain.model

data class TodayClass(
    val id: String,
    val name: String,
    val startTime: String,  // "09:00"
    val endTime: String,    // "10:30"
    val building: String,
    val room: String,
    val professor: String
)
