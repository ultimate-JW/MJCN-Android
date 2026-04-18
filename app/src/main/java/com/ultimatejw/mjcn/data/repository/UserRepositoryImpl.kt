package com.ultimatejw.mjcn.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.ultimatejw.mjcn.data.local.dao.UserDao
import com.ultimatejw.mjcn.data.local.entity.toEntity
import com.ultimatejw.mjcn.domain.model.User
import com.ultimatejw.mjcn.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val userDao: UserDao
) : UserRepository {

    companion object {
        private val KEY_IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        private val KEY_USER_TOKEN = stringPreferencesKey("user_token")
    }

    override val isLoggedIn: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_IS_LOGGED_IN] ?: false
    }

    override val currentUser: Flow<User?> = userDao.getUser().map { it?.toDomain() }

    override suspend fun saveLoginState(token: String) {
        dataStore.edit { prefs ->
            prefs[KEY_IS_LOGGED_IN] = true
            prefs[KEY_USER_TOKEN] = token
        }
    }

    override suspend fun logout() {
        dataStore.edit { prefs ->
            prefs[KEY_IS_LOGGED_IN] = false
            prefs.remove(KEY_USER_TOKEN)
        }
        userDao.clearAll()
    }

    override suspend fun saveUser(user: User) {
        userDao.insertUser(user.toEntity())
    }
}
