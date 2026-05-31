package com.ultimatejw.mjcn.domain.model

data class ThemeItem(
    val id: Int,
    val title: String,
    val content: String,
    val externalUrl: String?,
    val itemType: String
)
