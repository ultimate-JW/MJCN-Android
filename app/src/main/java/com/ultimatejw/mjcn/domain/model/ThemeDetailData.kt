package com.ultimatejw.mjcn.domain.model

data class ThemeDetailData(
    val id: Int,
    val title: String,
    val category: String,
    val description: String,
    val items: List<ThemeItem>
)
