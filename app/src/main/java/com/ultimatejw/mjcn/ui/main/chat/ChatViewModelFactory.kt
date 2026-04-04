package com.ultimatejw.mjcn.ui.main.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ultimatejw.mjcn.data.repository.ChatRepository

class ChatViewModelFactory(
    private val chatRepository: ChatRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(chatRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
