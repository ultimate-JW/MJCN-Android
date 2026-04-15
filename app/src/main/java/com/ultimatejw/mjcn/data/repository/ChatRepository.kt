package com.ultimatejw.mjcn.data.repository

import com.ultimatejw.mjcn.data.local.dao.ChatDao
import com.ultimatejw.mjcn.data.local.entity.toEntity
import com.ultimatejw.mjcn.domain.model.ChatSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ChatRepository @Inject constructor(private val chatDao: ChatDao) {

    fun getAllSessions(): Flow<List<ChatSession>> =
        chatDao.getAllSessions().map { entities -> entities.map { it.toDomain() } }

    fun getSessionsByCategory(category: String): Flow<List<ChatSession>> =
        chatDao.getSessionsByCategory(category).map { entities -> entities.map { it.toDomain() } }

    suspend fun saveSession(session: ChatSession) {
        chatDao.insertSession(session.toEntity())
    }

    suspend fun deleteSession(session: ChatSession) {
        chatDao.deleteSession(session.toEntity())
    }
}
