package com.ultimatejw.mjcn.ui.main.notice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ultimatejw.mjcn.data.repository.NoticeRepository

class NoticeViewModelFactory(
    private val noticeRepository: NoticeRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NoticeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NoticeViewModel(noticeRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
