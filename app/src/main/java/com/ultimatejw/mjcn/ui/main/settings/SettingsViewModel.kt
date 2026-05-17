package com.ultimatejw.mjcn.ui.main.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.ultimatejw.mjcn.domain.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val notifAll: Boolean = true,
    val notifChat: Boolean = true,
    val notifNotice: Boolean = true,
    val notifContest: Boolean = true
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    val uiState: LiveData<SettingsUiState> = combine(
        notificationRepository.notifAll,
        notificationRepository.notifChat,
        notificationRepository.notifNotice,
        notificationRepository.notifContest
    ) { all, chat, notice, contest ->
        SettingsUiState(all, chat, notice, contest)
    }.asLiveData()

    fun toggleAll(enabled: Boolean) {
        viewModelScope.launch { notificationRepository.setNotifAll(enabled) }
    }

    fun toggleChat(enabled: Boolean) {
        viewModelScope.launch { notificationRepository.setNotifChat(enabled) }
    }

    fun toggleNotice(enabled: Boolean) {
        viewModelScope.launch { notificationRepository.setNotifNotice(enabled) }
    }

    fun toggleContest(enabled: Boolean) {
        viewModelScope.launch { notificationRepository.setNotifContest(enabled) }
    }
}
