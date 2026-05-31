package com.ultimatejw.mjcn.ui.main.theme

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ultimatejw.mjcn.R
import com.ultimatejw.mjcn.domain.model.Theme
import com.ultimatejw.mjcn.domain.repository.ThemeRepository
import com.ultimatejw.mjcn.ui.common.CurrentUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ThemeUiState(
    val themes: List<Theme> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val themeRepository: ThemeRepository
) : ViewModel() {

    private val _uiState = MutableLiveData(ThemeUiState())
    val uiState: LiveData<ThemeUiState> = _uiState

    init {
        loadThemes(isRefresh = false)
    }

    fun refresh() {
        loadThemes(isRefresh = true)
    }

    private fun loadThemes(isRefresh: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value!!.copy(
                isLoading = !isRefresh,
                isRefreshing = isRefresh
            )
            themeRepository.fetchThemes()
                .onSuccess { themes ->
                    _uiState.value = ThemeUiState(themes = themes.ifEmpty { fallbackThemes() })
                }
                .onFailure {
                    _uiState.value = ThemeUiState(themes = fallbackThemes())
                }
        }
    }

    private fun fallbackThemes(): List<Theme> = listOf(
        Theme(0, "${CurrentUser.gradeSemester} 수강신청 가이드", "전공필수와 선택과목 균형있게 설계하기", "course_registration", R.drawable.ic_hat, "#E1F5EE"),
        Theme(0, "나의 취업·진로 로드맵", "인턴십, 자격증, 포트폴리오 뭐 부터 하지?", "career", R.drawable.ic_bag, "#E6F1FB"),
        Theme(0, "교환학생·해외 인턴십 가이드", "나한테 필요할까? 시기는 언제로 가야하지?", "exchange", R.drawable.ic_plane, "#EAF3DE"),
        Theme(0, "국가 지원 사업 신청하기", "내가 신청할 수 있는 국가 사업 알아보기", "grant", R.drawable.ic_donate, "#FAEEDA"),
        Theme(0, "학업 스트레스&시간관리 꿀팁", "건강한 멘탈 관리와 효율적인 시간 관리", "academic", R.drawable.ic_heart, "#FBEAF0")
    )
}
