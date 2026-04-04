package com.ultimatejw.mjcn.ui.main.notice

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.ultimatejw.mjcn.data.model.Notice
import com.ultimatejw.mjcn.data.repository.NoticeRepository

class NoticeViewModel(private val noticeRepository: NoticeRepository) : ViewModel() {

    val allNotices: LiveData<List<Notice>> = noticeRepository.getAllNotices().asLiveData()

    fun getByCategory(category: String): LiveData<List<Notice>> =
        if (category == "전체") noticeRepository.getAllNotices().asLiveData()
        else noticeRepository.getNoticesByCategory(category).asLiveData()

    suspend fun toggleBookmark(id: String, bookmarked: Boolean) {
        noticeRepository.toggleBookmark(id, bookmarked)
    }
}
