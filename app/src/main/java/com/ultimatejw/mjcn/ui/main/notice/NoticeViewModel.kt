package com.ultimatejw.mjcn.ui.main.notice

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ultimatejw.mjcn.domain.model.Notice
import com.ultimatejw.mjcn.domain.usecase.bookmark.ObserveNoticeBookmarksUseCase
import com.ultimatejw.mjcn.domain.usecase.bookmark.ToggleNoticeBookmarkUseCase
import com.ultimatejw.mjcn.domain.usecase.notice.GetAllNoticesUseCase
import com.ultimatejw.mjcn.domain.usecase.notice.GetNoticesByCategoryUseCase
import com.ultimatejw.mjcn.domain.usecase.notice.ToggleBookmarkUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NoticeUiState(
    val notices: List<Notice> = emptyList(),
    val selectedCategory: String = "전체",
    val bookmarkedNoticeIds: Set<String> = emptySet(),
)

@HiltViewModel
class NoticeViewModel @Inject constructor(
    private val getAllNotices: GetAllNoticesUseCase,
    private val getNoticesByCategory: GetNoticesByCategoryUseCase,
    private val toggleBookmark: ToggleBookmarkUseCase,
    private val toggleNoticeBookmark: ToggleNoticeBookmarkUseCase,
    private val observeNoticeBookmarks: ObserveNoticeBookmarksUseCase,
) : ViewModel() {

    private val _uiState = MutableLiveData(NoticeUiState())
    val uiState: LiveData<NoticeUiState> = _uiState

    init {
        observeNotices()
        observeBookmarkIds()
    }

    private fun observeBookmarkIds() {
        viewModelScope.launch {
            observeNoticeBookmarks().collect { bookmarked ->
                _uiState.postValue(
                    _uiState.value!!.copy(bookmarkedNoticeIds = bookmarked.map { it.id }.toSet())
                )
            }
        }
    }

    fun toggleBookmarkForNotice(notice: Notice) {
        viewModelScope.launch { toggleNoticeBookmark(notice) }
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
