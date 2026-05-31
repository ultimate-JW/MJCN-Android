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

data class InfoUiState(
    val bookmarkedInfoIds: Set<String> = emptySet()
)

@HiltViewModel
class InfoViewModel @Inject constructor(
    private val toggleInfoBookmark: ToggleInfoBookmarkUseCase,
    private val observeInfoBookmarks: ObserveInfoBookmarksUseCase,
) : ViewModel() {

    private val _uiState = MutableLiveData(InfoUiState())
    val uiState: LiveData<InfoUiState> = _uiState

    init {
        viewModelScope.launch {
            observeInfoBookmarks().collect { bookmarked ->
                _uiState.postValue(
                    InfoUiState(bookmarkedInfoIds = bookmarked.map { it.id }.toSet())
                )
            }
        }
    }

    fun toggleBookmarkForInfo(info: Info) {
        viewModelScope.launch { toggleInfoBookmark(info) }
    }
}
