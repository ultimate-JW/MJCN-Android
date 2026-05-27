package com.ultimatejw.mjcn.ui.main.notice

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ultimatejw.mjcn.domain.model.Notice
import com.ultimatejw.mjcn.domain.repository.NoticeRepository
import com.ultimatejw.mjcn.domain.usecase.bookmark.ObserveNoticeBookmarksUseCase
import com.ultimatejw.mjcn.domain.usecase.bookmark.ToggleNoticeBookmarkUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val PAGE_SIZE = 10

data class NoticeUiState(
    val notices: List<Notice> = emptyList(),
    val totalCount: Int = 0,
    val selectedCategory: String = "전체",
    val bookmarkedNoticeIds: Set<String> = emptySet(),
    val isCustomTab: Boolean = true,
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class NoticeViewModel @Inject constructor(
    private val noticeRepository: NoticeRepository,
    private val toggleNoticeBookmark: ToggleNoticeBookmarkUseCase,
    private val observeNoticeBookmarks: ObserveNoticeBookmarksUseCase,
) : ViewModel() {

    private val _uiState = MutableLiveData(NoticeUiState())
    val uiState: LiveData<NoticeUiState> = _uiState

    private var currentPage = 0
    private var loadJob: Job? = null
    private var searchJob: Job? = null
    private var currentQuery = ""

    init {
        observeBookmarkIds()
        resetAndLoad()
    }

    fun selectTab(isCustom: Boolean) {
        if (_uiState.value?.isCustomTab == isCustom) return
        _uiState.value = _uiState.value!!.copy(isCustomTab = isCustom)
        resetAndLoad()
    }

    fun selectCategory(category: String) {
        if (_uiState.value?.selectedCategory == category) return
        _uiState.value = _uiState.value!!.copy(selectedCategory = category)
        resetAndLoad()
    }

    fun onSearchQuery(q: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300)
            if (q == currentQuery) return@launch
            currentQuery = q
            resetAndLoad()
        }
    }

    fun loadMore() {
        val state = _uiState.value ?: return
        if (state.isLoading || state.isLoadingMore || !state.hasMore) return
        loadPage(currentPage + 1, reset = false)
    }

    fun refresh() = resetAndLoad()

    fun toggleBookmarkForNotice(notice: Notice) {
        viewModelScope.launch { toggleNoticeBookmark(notice) }
    }

    private fun resetAndLoad() {
        loadJob?.cancel()
        currentPage = 0
        _uiState.value = _uiState.value!!.copy(
            notices = emptyList(),
            totalCount = 0,
            hasMore = true,
            error = null
        )
        loadPage(1, reset = true)
    }

    private fun loadPage(page: Int, reset: Boolean) {
        val state = _uiState.value ?: return
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.value = state.copy(
                isLoading = page == 1,
                isLoadingMore = page > 1
            )
            val result = noticeRepository.fetchNoticesPage(
                page = page,
                pageSize = PAGE_SIZE,
                view = if (state.isCustomTab) "personalized" else "all",
                source = chipToSource(state.selectedCategory),
                q = currentQuery.ifBlank { null }
            )
            result.fold(
                onSuccess = { noticePage ->
                    val bookmarkedIds = _uiState.value!!.bookmarkedNoticeIds
                    val incoming = noticePage.notices.map {
                        it.copy(isBookmarked = it.id in bookmarkedIds)
                    }
                    val combined = if (reset) incoming
                                   else (_uiState.value!!.notices + incoming)
                    currentPage = page
                    _uiState.value = _uiState.value!!.copy(
                        notices = combined,
                        totalCount = noticePage.totalCount,
                        hasMore = noticePage.hasMore,
                        isLoading = false,
                        isLoadingMore = false,
                        error = null
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value!!.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        error = e.message
                    )
                }
            )
        }
    }

    private fun observeBookmarkIds() {
        viewModelScope.launch {
            observeNoticeBookmarks().collect { bookmarked ->
                val ids = bookmarked.map { it.id }.toSet()
                val updated = _uiState.value!!.notices.map {
                    it.copy(isBookmarked = it.id in ids)
                }
                _uiState.postValue(
                    _uiState.value!!.copy(
                        bookmarkedNoticeIds = ids,
                        notices = updated
                    )
                )
            }
        }
    }

    private fun chipToSource(chipLabel: String): String? = when (chipLabel) {
        "전체"         -> null
        "일반"         -> "general"
        "학사"         -> "academic"
        "공모전/대외활동" -> "contest,student_activity"
        "장학/학자금"   -> "scholarship"
        "취업"         -> "career"
        "해외"         -> "overseas"
        "지원사업"      -> "event"
        else           -> null
    }
}
