package com.ultimatejw.mjcn.domain.repository

import com.ultimatejw.mjcn.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    val isLoggedIn: Flow<Boolean>
    val currentUser: Flow<User?>
    suspend fun saveLoginState(token: String)
    suspend fun logout()
    suspend fun saveUser(user: User)
}
