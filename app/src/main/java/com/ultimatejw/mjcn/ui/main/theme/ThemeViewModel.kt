package com.ultimatejw.mjcn.ui.main.theme

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ultimatejw.mjcn.domain.model.Theme
import com.ultimatejw.mjcn.domain.repository.ThemeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ThemeUiState(
    val themes: List<Theme> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val themeRepository: ThemeRepository
) : ViewModel() {

    private val _uiState = MutableLiveData(ThemeUiState())
    val uiState: LiveData<ThemeUiState> = _uiState

    init {
        loadThemes()
    }

    private fun loadThemes() {
        viewModelScope.launch {
            _uiState.value = _uiState.value!!.copy(isLoading = true)
            themeRepository.fetchThemes()
                .onSuccess { themes ->
                    _uiState.value = ThemeUiState(themes = themes, isLoading = false)
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value!!.copy(isLoading = false, error = e.message)
                }
        }
    }
}
