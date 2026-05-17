package com.ultimatejw.mjcn.ui.main.info

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ultimatejw.mjcn.domain.model.Info
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

data class InfoBookmarkUiState(
    val allBookmarks: List<Info> = emptyList(),
    val filtered: List<Info> = emptyList(),
    val selectedCategory: String = "전체",
)

@HiltViewModel
class InfoBookmarkViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableLiveData(InfoBookmarkUiState())
    val uiState: LiveData<InfoBookmarkUiState> = _uiState

    init {
        val bookmarks = buildBookmarks()
        _uiState.value = InfoBookmarkUiState(allBookmarks = bookmarks, filtered = bookmarks)
    }

    fun selectCategory(category: String) {
        val all = _uiState.value!!.allBookmarks
        val filtered = if (category == "전체") all
        else all.filter { it.category == category }
        _uiState.value = _uiState.value!!.copy(selectedCategory = category, filtered = filtered)
    }

    private fun buildBookmarks() = listOf(
        Info("b1", "2026 AI 기반 서비스 아이디어 공모전", "부트캠프", "과학기술정보통신부", false, 22, true),
        Info("b2", "2026 AI 아이디어 공모전 참가자 모집", "공모전", "과학기술정보통신부", false, 35, true),
        Info("b3", "SQL 데이터베이스 입문 무료 특강 안내", "교육/강의", "인포포트", false, 45, true),
        Info("b4", "청년 월세 지원사업 신청 안내", "지원사업", "국토교통부", false, 50, true),
        Info("b5", "클라우드 기반 백엔드 개발 실무 과정 안내", "교육/강의", "AWS Educate", false, 88, true),
        Info("b6", "2026 AI 기반 서비스 아이디어 공모전", "부트캠프", "과학기술정보통신부", false, 22, true),
        Info("b7", "2026 AI 아이디어 공모전 참가자 모집", "공모전", "과학기술정보통신부", false, 35, true),
        Info("b8", "SQL 데이터베이스 입문 무료 특강 안내", "교육/강의", "인포포트", false, 45, true),
        Info("b9", "청년 월세 지원사업 신청 안내", "지원사업", "국토교통부", false, 50, true),
        Info("b10", "클라우드 기반 백엔드 개발 실무 과정 안내", "교육/강의", "AWS Educate", false, 88, true),
    )
}
