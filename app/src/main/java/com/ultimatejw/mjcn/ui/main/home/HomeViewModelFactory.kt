package com.ultimatejw.mjcn.ui.main.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ultimatejw.mjcn.data.repository.NoticeRepository
import com.ultimatejw.mjcn.data.repository.UserRepository

class HomeViewModelFactory(
    private val userRepository: UserRepository,
    private val noticeRepository: NoticeRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(userRepository, noticeRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
