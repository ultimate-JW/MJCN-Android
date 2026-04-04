package com.ultimatejw.mjcn.data.repository

import com.ultimatejw.mjcn.data.local.dao.ChatDao
import com.ultimatejw.mjcn.data.model.ChatSession
import kotlinx.coroutines.flow.Flow

class ChatRepository(private val chatDao: ChatDao) {

    fun getAllSessions(): Flow<List<ChatSession>> = chatDao.getAllSessions()

    fun getSessionsByCategory(category: String): Flow<List<ChatSession>> =
        chatDao.getSessionsByCategory(category)

    suspend fun saveSession(session: ChatSession) {
        chatDao.insertSession(session)
    }

    suspend fun deleteSession(session: ChatSession) {
        chatDao.deleteSession(session)
    }
}
