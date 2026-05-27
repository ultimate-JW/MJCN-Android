package com.ultimatejw.mjcn.ui.main.info

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ultimatejw.mjcn.domain.model.Info
import com.ultimatejw.mjcn.domain.repository.InfoRepository
import com.ultimatejw.mjcn.domain.usecase.bookmark.ObserveInfoBookmarksUseCase
import com.ultimatejw.mjcn.domain.usecase.bookmark.ToggleInfoBookmarkUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val PAGE_SIZE = 10

data class InfoUiState(
    val infos: List<Info> = emptyList(),
    val totalCount: Int = 0,
    val selectedCategory: String = "전체",
    val bookmarkedInfoIds: Set<String> = emptySet(),
    val isCustomTab: Boolean = true,
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class InfoViewModel @Inject constructor(
    private val infoRepository: InfoRepository,
    private val toggleInfoBookmark: ToggleInfoBookmarkUseCase,
    private val observeInfoBookmarks: ObserveInfoBookmarksUseCase,
) : ViewModel() {

    private val _uiState = MutableLiveData(InfoUiState())
    val uiState: LiveData<InfoUiState> = _uiState

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

    fun toggleBookmarkForInfo(info: Info) {
        viewModelScope.launch { toggleInfoBookmark(info) }
    }

    private fun resetAndLoad() {
        loadJob?.cancel()
        currentPage = 0
        _uiState.value = _uiState.value!!.copy(
            infos = emptyList(),
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
            val result = infoRepository.fetchInfosPage(
                page = page,
                pageSize = PAGE_SIZE,
                view = if (state.isCustomTab) "personalized" else "all",
                category = chipToCategory(state.selectedCategory),
                q = currentQuery.ifBlank { null }
            )
            result.fold(
                onSuccess = { infoPage ->
                    val bookmarkedIds = _uiState.value!!.bookmarkedInfoIds
                    val incoming = infoPage.infos.map {
                        it.copy(isBookmarked = it.id in bookmarkedIds)
                    }
                    val combined = if (reset) incoming
                                   else (_uiState.value!!.infos + incoming)
                    currentPage = page
                    _uiState.value = _uiState.value!!.copy(
                        infos = combined,
                        totalCount = infoPage.totalCount,
                        hasMore = infoPage.hasMore,
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
            observeInfoBookmarks().collect { bookmarked ->
                val ids = bookmarked.map { it.id }.toSet()
                val updated = _uiState.value!!.infos.map {
                    it.copy(isBookmarked = it.id in ids)
                }
                _uiState.postValue(
                    _uiState.value!!.copy(
                        bookmarkedInfoIds = ids,
                        infos = updated
                    )
                )
            }
        }
    }

    private fun chipToCategory(chipLabel: String): String? = when (chipLabel) {
        "전체" -> null
        else   -> chipLabel
    }
}
