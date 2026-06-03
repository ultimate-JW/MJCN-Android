package com.ultimatejw.mjcn.domain.model

data class StudyTips(
    val advice: String,
    val sections: List<StudyTipSection>,
    val quickQuestions: List<QuickQuestion>
)

data class StudyTipSection(
    val title: String,
    val tips: List<StudyTip>
)

data class StudyTip(
    val emoji: String,
    val title: String,
    val body: String
)
