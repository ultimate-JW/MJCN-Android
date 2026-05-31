package com.ultimatejw.mjcn.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 로그인 토큰과 온보딩 진행 상태를 보관한다.
 * - 토큰은 OkHttp Interceptor에서 동기 접근이 필요하므로 메모리 캐시(@Volatile)도 유지.
 * - 진행 상태(is_onboarding_completed)는 SplashActivity 분기에 사용.
 */
@Singleton
class TokenStore @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val KEY_ACCESS_TOKEN = stringPreferencesKey("auth_access_token")
        private val KEY_REFRESH_TOKEN = stringPreferencesKey("auth_refresh_token")
        private val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("auth_onboarding_completed")
    }

    @Volatile private var cachedAccess: String? = null
    @Volatile private var cachedRefresh: String? = null

    private val _sessionExpiredFlow = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val sessionExpiredFlow: SharedFlow<Unit> = _sessionExpiredFlow.asSharedFlow()

    fun notifySessionExpired() {
        _sessionExpiredFlow.tryEmit(Unit)
    }

    init {
        // Singleton이 처음 주입될 때 1회 동기 로드. 짧은 IO이므로 cold start 시 허용 가능.
        runBlocking {
            val prefs = dataStore.data.first()
            cachedAccess = prefs[KEY_ACCESS_TOKEN]
            cachedRefresh = prefs[KEY_REFRESH_TOKEN]
        }
    }

    fun currentAccessToken(): String? = cachedAccess
    fun currentRefreshToken(): String? = cachedRefresh

    suspend fun saveTokens(access: String, refresh: String?) {
        cachedAccess = access
        cachedRefresh = refresh
        dataStore.edit { prefs ->
            prefs[KEY_ACCESS_TOKEN] = access
            if (refresh != null) prefs[KEY_REFRESH_TOKEN] = refresh
        }
    }

    suspend fun clearTokens() {
        cachedAccess = null
        cachedRefresh = null
        dataStore.edit { prefs ->
            prefs.remove(KEY_ACCESS_TOKEN)
            prefs.remove(KEY_REFRESH_TOKEN)
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { prefs -> prefs[KEY_ONBOARDING_COMPLETED] = completed }
    }

    suspend fun isOnboardingCompleted(): Boolean =
        dataStore.data.first()[KEY_ONBOARDING_COMPLETED] ?: false
}
