package com.ultimatejw.mjcn.ui.main.notice

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ultimatejw.mjcn.domain.model.Notice
import com.ultimatejw.mjcn.domain.usecase.notice.GetAllNoticesUseCase
import com.ultimatejw.mjcn.domain.usecase.notice.GetNoticesByCategoryUseCase
import com.ultimatejw.mjcn.domain.usecase.notice.ToggleBookmarkUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NoticeUiState(
    val notices: List<Notice> = emptyList(),
    val selectedCategory: String = "전체",
)

@HiltViewModel
class NoticeViewModel @Inject constructor(
    private val getAllNotices: GetAllNoticesUseCase,
    private val getNoticesByCategory: GetNoticesByCategoryUseCase,
    private val toggleBookmark: ToggleBookmarkUseCase,
) : ViewModel() {

    private val _uiState = MutableLiveData(NoticeUiState())
    val uiState: LiveData<NoticeUiState> = _uiState

    init {
        observeNotices()
    }

    private fun observeNotices() {
        viewModelScope.launch {
            getAllNotices().collect { notices ->
                _uiState.postValue(_uiState.value!!.copy(notices = notices))
            }
        }
    }

    fun selectCategory(category: String) {
        _uiState.value = _uiState.value!!.copy(selectedCategory = category)
        viewModelScope.launch {
            val flow = if (category == "전체") getAllNotices()
                       else getNoticesByCategory(category)
            flow.collect { notices ->
                _uiState.postValue(_uiState.value!!.copy(notices = notices))
            }
        }
    }

    fun toggleBookmark(id: String, bookmarked: Boolean) {
        viewModelScope.launch {
            toggleBookmark(id, bookmarked)
        }
    }
}
