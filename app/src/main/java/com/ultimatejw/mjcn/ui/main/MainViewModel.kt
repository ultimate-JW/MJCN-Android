package com.ultimatejw.mjcn.ui.main

import androidx.lifecycle.ViewModel
import com.ultimatejw.mjcn.domain.usecase.user.LogoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val logoutUseCase: LogoutUseCase,
) : ViewModel() {

    suspend fun logout() {
        logoutUseCase()
    }
}
