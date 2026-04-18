package com.ultimatejw.mjcn.data.repository

import com.ultimatejw.mjcn.data.local.dao.ChatDao
import com.ultimatejw.mjcn.data.local.entity.toEntity
import com.ultimatejw.mjcn.domain.model.ChatSession
import com.ultimatejw.mjcn.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ChatRepositoryImpl @Inject constructor(
    private val chatDao: ChatDao
) : ChatRepository {

    override fun getAllSessions(): Flow<List<ChatSession>> =
        chatDao.getAllSessions().map { entities -> entities.map { it.toDomain() } }

    override fun getSessionsByCategory(category: String): Flow<List<ChatSession>> =
        chatDao.getSessionsByCategory(category).map { entities -> entities.map { it.toDomain() } }

    override suspend fun saveSession(session: ChatSession) {
        chatDao.insertSession(session.toEntity())
    }

    override suspend fun deleteSession(session: ChatSession) {
        chatDao.deleteSession(session.toEntity())
    }
}
