package com.ultimatejw.mjcn.ui.main.notice

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ultimatejw.mjcn.domain.model.Notice
import com.ultimatejw.mjcn.domain.usecase.bookmark.ObserveNoticeBookmarksUseCase
import com.ultimatejw.mjcn.domain.usecase.bookmark.ToggleNoticeBookmarkUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NoticeBookmarkUiState(
    val allBookmarks: List<Notice> = emptyList(),
    val filtered: List<Notice> = emptyList(),
    val selectedCategory: String = "전체",
)

@HiltViewModel
class NoticeBookmarkViewModel @Inject constructor(
    private val observeNoticeBookmarks: ObserveNoticeBookmarksUseCase,
    private val toggleNoticeBookmark: ToggleNoticeBookmarkUseCase,
) : ViewModel() {

    private val _uiState = MutableLiveData(NoticeBookmarkUiState())
    val uiState: LiveData<NoticeBookmarkUiState> = _uiState

    init {
        viewModelScope.launch {
            observeNoticeBookmarks().collect { bookmarks ->
                val category = _uiState.value?.selectedCategory ?: "전체"
                _uiState.postValue(
                    NoticeBookmarkUiState(
                        allBookmarks = bookmarks,
                        filtered = filter(bookmarks, category),
                        selectedCategory = category,
                    )
                )
            }
        }
    }

    fun selectCategory(category: String) {
        val all = _uiState.value?.allBookmarks ?: return
        _uiState.value = _uiState.value!!.copy(
            selectedCategory = category,
            filtered = filter(all, category)
        )
    }

    fun toggleBookmark(notice: Notice) {
        viewModelScope.launch { toggleNoticeBookmark(notice) }
    }

    private fun filter(list: List<Notice>, category: String): List<Notice> {
        if (category == "전체") return list
        return list.filter { matchCategory(it.category, category) }
    }

    private fun matchCategory(itemCategory: String, chip: String): Boolean {
        val parts = chip.split("/").map { it.trim() }
        return parts.any { part ->
            itemCategory == part || itemCategory.contains(part) || part.contains(itemCategory)
        }
    }
}
