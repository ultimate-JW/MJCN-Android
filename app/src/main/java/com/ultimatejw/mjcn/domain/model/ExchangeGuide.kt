package com.ultimatejw.mjcn.domain.model

data class ExchangeGuide(
    val adviceText: String,
    val necessity: List<ExchangeNecessityItem>,
    val evaluation: List<ExchangeEvaluationItem>,
    val currentRecommendation: List<ExchangeRecommendItem>,
    val quickQuestions: List<QuickQuestion>
)

data class ExchangeNecessityItem(
    val option: String,
    val icon: String,
    val score: Int,
    val reason: String
)

data class ExchangeEvaluationItem(
    val option: String,
    val icon: String,
    val fits: List<String>,
    val caveat: String?,
    val interestNote: String?
)

data class ExchangeRecommendItem(
    val rank: Int,
    val title: String,
    val note: String
)
