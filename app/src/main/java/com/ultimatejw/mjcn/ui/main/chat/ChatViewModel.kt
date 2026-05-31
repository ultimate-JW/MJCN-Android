package com.ultimatejw.mjcn.ui.main.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ultimatejw.mjcn.domain.model.ChatSession
import com.ultimatejw.mjcn.domain.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatUiState(
    val sessions: List<ChatSession> = emptyList(),
    val totalCount: Int = 0,
    val selectedCategory: String = "전체",
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _uiState = MutableLiveData(ChatUiState())
    val uiState: LiveData<ChatUiState> = _uiState

    private var allSessions: List<ChatSession> = emptyList()

    init {
        loadRooms()
    }

    fun loadRooms() {
        viewModelScope.launch {
            _uiState.value = _uiState.value!!.copy(isLoading = true, error = null)
            chatRepository.getChatRooms().fold(
                onSuccess = { sessions ->
                    allSessions = sessions
                    _uiState.value = _uiState.value!!.copy(
                        totalCount = sessions.size,
                        isLoading = false
                    )
                    applyFilter()
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value!!.copy(isLoading = false, error = e.message)
                }
            )
        }
    }

    fun selectCategory(category: String) {
        if (_uiState.value?.selectedCategory == category) return
        _uiState.value = _uiState.value!!.copy(selectedCategory = category)
        applyFilter()
    }

    private fun applyFilter() {
        val cat = _uiState.value?.selectedCategory ?: "전체"
        val filtered = if (cat == "전체") allSessions
                       else allSessions.filter { it.category == cat }
        _uiState.value = _uiState.value!!.copy(sessions = filtered)
    }
}
