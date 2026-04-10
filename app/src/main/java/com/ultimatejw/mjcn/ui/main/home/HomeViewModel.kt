package com.ultimatejw.mjcn.ui.main.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ultimatejw.mjcn.data.model.Notice
import com.ultimatejw.mjcn.data.model.User
import com.ultimatejw.mjcn.data.repository.NoticeRepository
import com.ultimatejw.mjcn.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val currentUser: User? = null,
    val notices: List<Notice> = emptyList(),
    val courseCount: Int = 0,
    val graduationCredits: Int = 0,
    val dday: String = "D-?",
    val gradProgress: String = "0%",
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val noticeRepository: NoticeRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeUser()
        observeNotices()
        // TODO: 실제 API에서 데이터 불러오기
        _uiState.update { it.copy(courseCount = 3, graduationCredits = 12, dday = "D-42", gradProgress = "87%") }
    }

    private fun observeUser() {
        viewModelScope.launch {
            userRepository.currentUser.collect { user ->
                _uiState.update { it.copy(currentUser = user) }
            }
        }
    }

    private fun observeNotices() {
        viewModelScope.launch {
            noticeRepository.getAllNotices().collect { notices ->
                _uiState.update { it.copy(notices = notices) }
            }
        }
    }
}
