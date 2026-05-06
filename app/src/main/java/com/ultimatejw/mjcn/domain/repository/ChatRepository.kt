package com.ultimatejw.mjcn.domain.repository

import com.ultimatejw.mjcn.domain.model.ChatSession
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun getAllSessions(): Flow<List<ChatSession>>
    fun getSessionsByCategory(category: String): Flow<List<ChatSession>>
    suspend fun saveSession(session: ChatSession)
    suspend fun deleteSession(session: ChatSession)
}
