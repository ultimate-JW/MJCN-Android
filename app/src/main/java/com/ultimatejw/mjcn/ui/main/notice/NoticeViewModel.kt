package com.ultimatejw.mjcn.ui.main.notice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ultimatejw.mjcn.data.model.Notice
import com.ultimatejw.mjcn.data.repository.NoticeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NoticeUiState(
    val notices: List<Notice> = emptyList(),
    val selectedCategory: String = "전체",
)

@HiltViewModel
class NoticeViewModel @Inject constructor(
    private val noticeRepository: NoticeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NoticeUiState())
    val uiState: StateFlow<NoticeUiState> = _uiState.asStateFlow()

    init {
        observeNotices()
    }

    private fun observeNotices() {
        viewModelScope.launch {
            noticeRepository.getAllNotices().collect { notices ->
                _uiState.update { it.copy(notices = notices) }
            }
        }
    }

    fun selectCategory(category: String) {
        _uiState.update { it.copy(selectedCategory = category) }
        viewModelScope.launch {
            val flow = if (category == "전체") noticeRepository.getAllNotices()
                       else noticeRepository.getNoticesByCategory(category)
            flow.collect { notices ->
                _uiState.update { it.copy(notices = notices) }
            }
        }
    }

    fun toggleBookmark(id: String, bookmarked: Boolean) {
        viewModelScope.launch {
            noticeRepository.toggleBookmark(id, bookmarked)
        }
    }
}
