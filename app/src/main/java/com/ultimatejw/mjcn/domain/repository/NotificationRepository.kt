package com.ultimatejw.mjcn.domain.repository

import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    val notifAll: Flow<Boolean>
    val notifChat: Flow<Boolean>
    val notifNotice: Flow<Boolean>
    val notifContest: Flow<Boolean>
    suspend fun setNotifAll(enabled: Boolean)
    suspend fun setNotifChat(enabled: Boolean)
    suspend fun setNotifNotice(enabled: Boolean)
    suspend fun setNotifContest(enabled: Boolean)
}
