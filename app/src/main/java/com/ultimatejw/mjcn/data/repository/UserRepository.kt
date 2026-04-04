package com.ultimatejw.mjcn.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ultimatejw.mjcn.data.local.dao.UserDao
import com.ultimatejw.mjcn.data.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "mjcn_prefs")

class UserRepository(
    private val context: Context,
    private val userDao: UserDao
) {
    companion object {
        private val KEY_IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        private val KEY_USER_TOKEN = stringPreferencesKey("user_token")
    }

    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_IS_LOGGED_IN] ?: false
    }

    val currentUser: Flow<User?> = userDao.getUser()

    suspend fun saveLoginState(token: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_IS_LOGGED_IN] = true
            prefs[KEY_USER_TOKEN] = token
        }
    }

    suspend fun logout() {
        context.dataStore.edit { prefs ->
            prefs[KEY_IS_LOGGED_IN] = false
            prefs.remove(KEY_USER_TOKEN)
        }
        userDao.clearAll()
    }

    suspend fun saveUser(user: User) {
        userDao.insertUser(user)
    }
}
