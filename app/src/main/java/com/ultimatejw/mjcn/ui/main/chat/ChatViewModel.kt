package com.ultimatejw.mjcn.ui.main.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.ultimatejw.mjcn.data.model.ChatSession
import com.ultimatejw.mjcn.data.repository.ChatRepository

class ChatViewModel(private val chatRepository: ChatRepository) : ViewModel() {

    val allSessions: LiveData<List<ChatSession>> = chatRepository.getAllSessions().asLiveData()

    private var currentCategory = "전체"

    fun getSessionsByCategory(category: String): LiveData<List<ChatSession>> {
        currentCategory = category
        return if (category == "전체") {
            chatRepository.getAllSessions().asLiveData()
        } else {
            chatRepository.getSessionsByCategory(category).asLiveData()
        }
    }
}
