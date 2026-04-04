package com.ultimatejw.mjcn.ui.main.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.ultimatejw.mjcn.data.model.Notice
import com.ultimatejw.mjcn.data.model.User
import com.ultimatejw.mjcn.data.repository.NoticeRepository
import com.ultimatejw.mjcn.data.repository.UserRepository

class HomeViewModel(
    private val userRepository: UserRepository,
    private val noticeRepository: NoticeRepository
) : ViewModel() {

    val currentUser: LiveData<User?> = userRepository.currentUser.asLiveData()
    val notices: LiveData<List<Notice>> = noticeRepository.getAllNotices().asLiveData()

    private val _courseCount = MutableLiveData(0)
    val courseCount: LiveData<Int> = _courseCount

    private val _graduationCredits = MutableLiveData(0)
    val graduationCredits: LiveData<Int> = _graduationCredits

    private val _dday = MutableLiveData("D-?")
    val dday: LiveData<String> = _dday

    private val _gradProgress = MutableLiveData("0%")
    val gradProgress: LiveData<String> = _gradProgress

    init {
        // TODO: 실제 API에서 데이터 불러오기
        _courseCount.value = 3
        _graduationCredits.value = 12
        _dday.value = "D-42"
        _gradProgress.value = "87%"
    }
}
