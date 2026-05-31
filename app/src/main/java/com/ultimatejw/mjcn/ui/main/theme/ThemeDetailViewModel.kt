package com.ultimatejw.mjcn.ui.main.theme

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ultimatejw.mjcn.domain.model.ThemeItem
import com.ultimatejw.mjcn.domain.repository.ThemeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ThemeDetailUiState(
    val isLoading: Boolean = false,
    val title: String = "",
    val adviceText: String = "",
    val contentItems: List<ThemeItem> = emptyList(),
    val linkItems: List<ThemeItem> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class ThemeDetailViewModel @Inject constructor(
    private val themeRepository: ThemeRepository
) : ViewModel() {

    private val _uiState = MutableLiveData(ThemeDetailUiState())
    val uiState: LiveData<ThemeDetailUiState> = _uiState

    private var loaded = false

    fun load(themeId: Int) {
        if (themeId <= 0 || loaded) return
        loaded = true
        viewModelScope.launch {
            _uiState.value = ThemeDetailUiState(isLoading = true)
            themeRepository.fetchThemeDetail(themeId)
                .onSuccess { detail ->
                    val guideItems = detail.items.filter { it.itemType == "guide" }
                    val checklistItems = detail.items.filter { it.itemType == "checklist" }
                    val linkItems = detail.items.filter { it.itemType == "link" }

                    val adviceText = guideItems.firstOrNull()?.let { first ->
                        if (first.title.isNotBlank()) "${first.title}\n\n${first.content}"
                        else first.content
                    } ?: detail.description

                    val contentItems = guideItems.drop(1) + checklistItems

                    _uiState.value = ThemeDetailUiState(
                        isLoading = false,
                        title = detail.title,
                        adviceText = adviceText,
                        contentItems = contentItems,
                        linkItems = linkItems
                    )
                }
                .onFailure { e ->
                    _uiState.value = ThemeDetailUiState(isLoading = false, error = e.message)
                }
        }
    }
}
