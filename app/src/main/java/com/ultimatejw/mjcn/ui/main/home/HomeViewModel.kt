package com.ultimatejw.mjcn.ui.main.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ultimatejw.mjcn.domain.model.Notice
import com.ultimatejw.mjcn.domain.model.User
import com.ultimatejw.mjcn.domain.usecase.notice.GetAllNoticesUseCase
import com.ultimatejw.mjcn.domain.usecase.user.ObserveCurrentUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val currentUser: User? = null,
    val notices: List<Notice> = emptyList(),
    val courseCount: Int = 0,
    val graduationCredits: Int = 0,
    val dday: String = "D-?",
    val gradProgress: String = "0%",
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val observeCurrentUser: ObserveCurrentUserUseCase,
    private val getAllNotices: GetAllNoticesUseCase,
) : ViewModel() {

    private val _uiState = MutableLiveData(HomeUiState())
    val uiState: LiveData<HomeUiState> = _uiState

    init {
        observeUser()
        observeNotices()
        // TODO: 실제 API에서 데이터 불러오기
        _uiState.value = _uiState.value!!.copy(courseCount = 3, graduationCredits = 12, dday = "D-42", gradProgress = "87%")
    }

    private fun observeUser() {
        viewModelScope.launch {
            observeCurrentUser().collect { user ->
                _uiState.postValue(_uiState.value!!.copy(currentUser = user))
            }
        }
    }

    private fun observeNotices() {
        viewModelScope.launch {
            getAllNotices().collect { notices ->
                _uiState.postValue(_uiState.value!!.copy(notices = notices))
            }
        }
    }
}
