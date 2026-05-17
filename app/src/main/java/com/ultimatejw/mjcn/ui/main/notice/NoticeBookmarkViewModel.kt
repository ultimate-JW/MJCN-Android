package com.ultimatejw.mjcn.ui.main.notice

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ultimatejw.mjcn.domain.model.Notice
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

data class NoticeBookmarkUiState(
    val allBookmarks: List<Notice> = emptyList(),
    val filtered: List<Notice> = emptyList(),
    val selectedCategory: String = "전체",
)

@HiltViewModel
class NoticeBookmarkViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableLiveData(NoticeBookmarkUiState())
    val uiState: LiveData<NoticeBookmarkUiState> = _uiState

    init {
        val bookmarks = buildBookmarks()
        _uiState.value = NoticeBookmarkUiState(allBookmarks = bookmarks, filtered = bookmarks)
    }

    fun selectCategory(category: String) {
        val all = _uiState.value!!.allBookmarks
        val filtered = if (category == "전체") all
        else all.filter { matchCategory(it.category, category) }
        _uiState.value = _uiState.value!!.copy(selectedCategory = category, filtered = filtered)
    }

    private fun matchCategory(itemCategory: String, chip: String): Boolean {
        val parts = chip.split("/").map { it.trim() }
        return parts.any { part ->
            itemCategory == part || itemCategory.contains(part) || part.contains(itemCategory)
        }
    }

    private fun buildBookmarks() = listOf(
        Notice("a1", "[ABI-X사업단] 특임교수 채용 공고",
            "학사", "AI-Bigdata ICT융합교육", "1시간 전", isBookmarked = true),
        Notice("a2", "전북특별자치도 2026년도 향토인재 장학생 모집",
            "지원사업", "", "3시간 전", isBookmarked = true),
        Notice("a3", "2026학년도「전공·진로 Festival」학부 학과 전공 체험",
            "일반", "학사지원팀", "8시간 전", isBookmarked = true),
        Notice("a4", "2026학년도 1학기 강좌폐강 안내(최종)",
            "대외활동", "학사지원팀", "23시간 전", isBookmarked = true),
        Notice("a5", "공기업(한국가스기술공사)동문선배와의 TAL…",
            "진로/취업/창업", "MJ대학일자리플러스센터", "2일 전", isBookmarked = true),
        Notice("a6", "2026 선배와의 취업멘토링 참여학생 모집(4월)",
            "교육/강의", "MJ대학일자리플러스센터", "5시간 전", isBookmarked = true),
        Notice("a7", "2026 선배와의 취업멘토링 참여학생 모집(4월)",
            "공모전", "AI-Bigdata ICT융합교육", "오늘", isBookmarked = true),
        Notice("a8", "2026 창업 아이디어 경진대회 참가자 모집(5월)",
            "진로/취업/창업", "스타트업·혁신", "내일", isBookmarked = true),
        Notice("a9", "2026 빅데이터 분석 워크숍 신청 안내(6월)",
            "장학/학자금", "데이터과학·분석", "이번주", isBookmarked = true),
        Notice("a10", "2026 여름방학 AI 캠프 참가자 모집(7월)",
            "진로/취업/창업", "인공지능·교육", "다음달", isBookmarked = true),
    )
}
