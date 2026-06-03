package com.ultimatejw.mjcn.domain.model

data class ContestGuide(
    val adviceLine1: String,
    val adviceLine2: String,
    val cards: List<ContestCard>,
    val quickQuestions: List<QuickQuestion>
)

data class ContestCard(
    val id: Int,
    val title: String,
    val organizer: String,
    val endDate: String,
    val dday: Int,
    val url: String
)
