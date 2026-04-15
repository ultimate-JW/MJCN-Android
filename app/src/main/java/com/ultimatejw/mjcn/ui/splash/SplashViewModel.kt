package com.ultimatejw.mjcn.ui.splash

import androidx.lifecycle.ViewModel
import com.ultimatejw.mjcn.domain.usecase.user.GetLoginStateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val getLoginState: GetLoginStateUseCase,
) : ViewModel() {
    val isLoggedIn: Flow<Boolean> = getLoginState()
}
