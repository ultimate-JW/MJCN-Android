package com.ultimatejw.mjcn.ui.main.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ultimatejw.mjcn.domain.model.ChatSession
import com.ultimatejw.mjcn.domain.usecase.chat.GetAllChatSessionsUseCase
import com.ultimatejw.mjcn.domain.usecase.chat.GetChatSessionsByCategoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

data class ChatUiState(
    val sessions: List<ChatSession> = emptyList(),
    val selectedCategory: String = "전체",
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val getAllChatSessions: GetAllChatSessionsUseCase,
    private val getChatSessionsByCategory: GetChatSessionsByCategoryUseCase,
) : ViewModel() {

    private val _uiState = MutableLiveData(ChatUiState())
    val uiState: LiveData<ChatUiState> = _uiState

    private val allSessions = listOf(
        ChatSession("1", "2026학년도 1학기 수강신청 정정 기간", "수강신청 정정 기간은 다음 주 월요일부터 시작됩니다.", "수강·졸업", "5분 전"),
        ChatSession("2", "국가장학금 2차 신청 조건 알려줘", "성적 기준 3.5 이상이면 신청 가능합니다.", "장학·등록금", "1시간 전"),
        ChatSession("3", "요즘 뜨는 공모전 알려줘", "디자인, IT 분야 공모전이 활발하게 열리고 있어요.", "공모전", "어제"),
        ChatSession("4", "취업 자소서 첨삭 부탁해", "자소서 첨삭을 도와드릴게요. 내용을 붙여넣어 주세요.", "취업·진로", "2일 전"),
        ChatSession("5", "졸업요건 학점 확인 방법", "포털 → 학사정보 → 졸업요건 조회에서 확인하실 수 있어요.", "수강·졸업", "3일 전"),
        ChatSession("6", "이번 학기 학사 일정 알려줘", "개강은 3월 3일, 중간고사는 4월 21일부터입니다.", "학사공지", "4일 전"),
    )

    init {
        _uiState.value = _uiState.value!!.copy(sessions = allSessions)
    }

    fun selectCategory(category: String) {
        val filtered = if (category == "전체") allSessions
                       else allSessions.filter { it.category == category }
        _uiState.value = _uiState.value!!.copy(
            selectedCategory = category,
            sessions = filtered
        )
    }
}
