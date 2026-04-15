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
    val selectedCategory: String = "전체",
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _uiState = MutableLiveData(ChatUiState())
    val uiState: LiveData<ChatUiState> = _uiState

    init {
        observeSessions()
    }

    private fun observeSessions() {
        viewModelScope.launch {
            chatRepository.getAllSessions().collect { sessions ->
                _uiState.postValue(_uiState.value!!.copy(sessions = sessions))
            }
        }
    }

    fun selectCategory(category: String) {
        _uiState.value = _uiState.value!!.copy(selectedCategory = category)
        viewModelScope.launch {
            val flow = if (category == "전체") chatRepository.getAllSessions()
                       else chatRepository.getSessionsByCategory(category)
            flow.collect { sessions ->
                _uiState.postValue(_uiState.value!!.copy(sessions = sessions))
            }
        }
    }
}
