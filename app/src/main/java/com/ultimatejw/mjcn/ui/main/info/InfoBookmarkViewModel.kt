package com.ultimatejw.mjcn.ui.main.info

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ultimatejw.mjcn.domain.model.Info
import com.ultimatejw.mjcn.domain.usecase.bookmark.ObserveInfoBookmarksUseCase
import com.ultimatejw.mjcn.domain.usecase.bookmark.ToggleInfoBookmarkUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InfoBookmarkUiState(
    val allBookmarks: List<Info> = emptyList(),
    val filtered: List<Info> = emptyList(),
    val selectedCategory: String = "전체",
)

@HiltViewModel
class InfoBookmarkViewModel @Inject constructor(
    private val observeInfoBookmarks: ObserveInfoBookmarksUseCase,
    private val toggleInfoBookmark: ToggleInfoBookmarkUseCase,
) : ViewModel() {

    private val _uiState = MutableLiveData(InfoBookmarkUiState())
    val uiState: LiveData<InfoBookmarkUiState> = _uiState

    init {
        viewModelScope.launch {
            observeInfoBookmarks().collect { bookmarks ->
                val category = _uiState.value?.selectedCategory ?: "전체"
                _uiState.postValue(
                    InfoBookmarkUiState(
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

    fun toggleBookmark(info: Info) {
        viewModelScope.launch { toggleInfoBookmark(info) }
    }

    private fun filter(list: List<Info>, category: String): List<Info> {
        if (category == "전체") return list
        return list.filter { it.category == category }
    }
}
