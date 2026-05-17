package com.ultimatejw.mjcn.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.ultimatejw.mjcn.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : NotificationRepository {

    companion object {
        private val KEY_NOTIF_ALL = booleanPreferencesKey("notif_all")
        private val KEY_NOTIF_CHAT = booleanPreferencesKey("notif_chat")
        private val KEY_NOTIF_NOTICE = booleanPreferencesKey("notif_notice")
        private val KEY_NOTIF_CONTEST = booleanPreferencesKey("notif_contest")
    }

    override val notifAll: Flow<Boolean> = dataStore.data.map { it[KEY_NOTIF_ALL] ?: true }
    override val notifChat: Flow<Boolean> = dataStore.data.map { it[KEY_NOTIF_CHAT] ?: true }
    override val notifNotice: Flow<Boolean> = dataStore.data.map { it[KEY_NOTIF_NOTICE] ?: true }
    override val notifContest: Flow<Boolean> = dataStore.data.map { it[KEY_NOTIF_CONTEST] ?: true }

    override suspend fun setNotifAll(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_NOTIF_ALL] = enabled
            prefs[KEY_NOTIF_CHAT] = enabled
            prefs[KEY_NOTIF_NOTICE] = enabled
            prefs[KEY_NOTIF_CONTEST] = enabled
        }
    }

    override suspend fun setNotifChat(enabled: Boolean) {
        dataStore.edit { it[KEY_NOTIF_CHAT] = enabled }
        syncAll()
    }

    override suspend fun setNotifNotice(enabled: Boolean) {
        dataStore.edit { it[KEY_NOTIF_NOTICE] = enabled }
        syncAll()
    }

    override suspend fun setNotifContest(enabled: Boolean) {
        dataStore.edit { it[KEY_NOTIF_CONTEST] = enabled }
        syncAll()
    }

    private suspend fun syncAll() {
        dataStore.edit { prefs ->
            val all = (prefs[KEY_NOTIF_CHAT] ?: true) &&
                    (prefs[KEY_NOTIF_NOTICE] ?: true) &&
                    (prefs[KEY_NOTIF_CONTEST] ?: true)
            prefs[KEY_NOTIF_ALL] = all
        }
    }
}
