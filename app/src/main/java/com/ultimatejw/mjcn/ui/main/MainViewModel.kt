package com.ultimatejw.mjcn.ui.main

import androidx.lifecycle.ViewModel
import com.ultimatejw.mjcn.data.local.TokenStore
import com.ultimatejw.mjcn.domain.usecase.user.LogoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val logoutUseCase: LogoutUseCase,
    private val tokenStore: TokenStore,
) : ViewModel() {

    val sessionExpiredFlow: SharedFlow<Unit> = tokenStore.sessionExpiredFlow

    suspend fun logout() {
        logoutUseCase()
        tokenStore.clearTokens()
    }
}
