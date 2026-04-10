package com.ultimatejw.mjcn.ui.splash

import androidx.lifecycle.ViewModel
import com.ultimatejw.mjcn.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    val isLoggedIn: Flow<Boolean> = userRepository.isLoggedIn
}
