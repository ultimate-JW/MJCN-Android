package com.ultimatejw.mjcn.ui.main.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

data class SettingsUiState(
    val notifAll: Boolean = true,
    val notifChat: Boolean = true,
    val notifNotice: Boolean = true,
    val notifContest: Boolean = true
)

@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableLiveData(SettingsUiState())
    val uiState: LiveData<SettingsUiState> = _uiState

    fun toggleAll(enabled: Boolean) {
        _uiState.value = _uiState.value!!.copy(
            notifAll = enabled,
            notifChat = enabled,
            notifNotice = enabled,
            notifContest = enabled
        )
    }

    fun toggleChat(enabled: Boolean) {
        val new = _uiState.value!!.copy(notifChat = enabled)
        _uiState.value = new.copy(notifAll = new.notifChat && new.notifNotice && new.notifContest)
    }

    fun toggleNotice(enabled: Boolean) {
        val new = _uiState.value!!.copy(notifNotice = enabled)
        _uiState.value = new.copy(notifAll = new.notifChat && new.notifNotice && new.notifContest)
    }

    fun toggleContest(enabled: Boolean) {
        val new = _uiState.value!!.copy(notifContest = enabled)
        _uiState.value = new.copy(notifAll = new.notifChat && new.notifNotice && new.notifContest)
    }
}
