package com.ultimatejw.mjcn.domain.model

data class Theme(
    val id: Int,
    val title: String,
    val subtitle: String,
    val category: String,
    val iconRes: Int,
    val iconBgColor: String
)
