package com.ultimatejw.mjcn.ui.main.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ultimatejw.mjcn.domain.model.ChatMessage
import com.ultimatejw.mjcn.domain.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatDetailUiState(
    val roomId: String? = null,
    val title: String = "새 대화",
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ChatDetailViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _uiState = MutableLiveData(ChatDetailUiState())
    val uiState: LiveData<ChatDetailUiState> = _uiState

    fun loadRoom(sessionId: String) {
        if (sessionId.isBlank()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value!!.copy(isLoading = true, error = null)
            chatRepository.getChatRoomDetail(sessionId).fold(
                onSuccess = { detail ->
                    _uiState.value = _uiState.value!!.copy(
                        roomId = detail.id,
                        title = detail.title,
                        messages = detail.messages,
                        isLoading = false
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value!!.copy(isLoading = false, error = e.message)
                }
            )
        }
    }

    fun sendMessage(content: String) {
        val state = _uiState.value ?: return
        if (state.isSending) return
        viewModelScope.launch {
            _uiState.value = state.copy(isSending = true, error = null)

            val roomId = state.roomId ?: run {
                chatRepository.createChatRoom().getOrElse { e ->
                    _uiState.value = _uiState.value!!.copy(isSending = false, error = e.message)
                    return@launch
                }.id
            }

            val userMsg = ChatMessage(
                id = "temp_${System.currentTimeMillis()}",
                role = "user",
                content = content,
                createdAt = ""
            )
            _uiState.value = _uiState.value!!.copy(
                roomId = roomId,
                messages = _uiState.value!!.messages + userMsg
            )

            chatRepository.sendMessage(roomId, content).fold(
                onSuccess = { aiMsg ->
                    _uiState.value = _uiState.value!!.copy(
                        messages = _uiState.value!!.messages + aiMsg,
                        isSending = false
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value!!.copy(isSending = false, error = e.message)
                }
            )
        }
    }

    fun deleteRoom(onDeleted: () -> Unit) {
        val roomId = _uiState.value?.roomId
        if (roomId == null) {
            onDeleted()
            return
        }
        viewModelScope.launch {
            chatRepository.deleteChatRoom(roomId).fold(
                onSuccess = { onDeleted() },
                onFailure = { e ->
                    _uiState.value = _uiState.value!!.copy(error = e.message)
                }
            )
        }
    }
}
