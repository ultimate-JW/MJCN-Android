package com.ultimatejw.mjcn.ui.splash

import androidx.lifecycle.ViewModel
import com.ultimatejw.mjcn.data.local.TokenStore
import com.ultimatejw.mjcn.domain.usecase.user.GetLoginStateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val getLoginState: GetLoginStateUseCase,
    private val tokenStore: TokenStore,
) : ViewModel() {
    val isLoggedIn: Flow<Boolean> = getLoginState()

    /**
     * 메인 화면으로 바로 들어갈 조건: isLoggedIn=true AND onboarding_completed=true.
     * 그 외(처음 실행 / 온보딩 미완료)는 로그인/회원가입 화면으로 보낸다.
     */
    suspend fun shouldGoToMain(): Boolean {
        val loggedIn = isLoggedIn.first()
        if (!loggedIn) return false
        return tokenStore.isOnboardingCompleted()
    }
}
