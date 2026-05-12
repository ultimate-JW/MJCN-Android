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
        ChatSession("1", "수강신청 정정 기간 언제야?", "수강신청 정정 기간은 다음 주 월요일부터 시작됩니다.", "수강신청", "5분 전"),
        ChatSession("2", "장학금 신청 조건 알려줘", "성적 기준 3.5 이상이면 신청 가능합니다.", "학교생활", "1시간 전"),
        ChatSession("3", "요즘 뜨는 공모전 알려줘", "디자인, IT 분야 공모전이 활발하게 열리고 있어요.", "공모전", "어제"),
        ChatSession("4", "취업 자소서 첨삭 부탁해", "자소서 첨삭을 도와드릴게요. 내용을 붙여넣어 주세요.", "취업·진로", "2일 전"),
        ChatSession("5", "교내 동아리 모집 현황", "현재 모집 중인 동아리 목록을 안내해 드릴게요.", "학교생활", "3일 전"),
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
