package com.ultimatejw.mjcn.ui.main.notice

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ultimatejw.mjcn.data.model.ApiResult
import com.ultimatejw.mjcn.domain.model.Notice
import com.ultimatejw.mjcn.domain.repository.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoticeDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: HomeRepository
) : ViewModel() {

    private val noticeId: String = savedStateHandle.get<String>("noticeId").orEmpty()

    private val _notice = MutableLiveData<Notice>()
    val notice: LiveData<Notice> = _notice

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        if (noticeId.isNotBlank()) loadDetail()
    }

    private fun loadDetail() {
        viewModelScope.launch {
            _isLoading.value = true
            Log.d("NoticeDetail", "loadDetail: id=$noticeId")
            when (val result = repository.getNoticeDetail(noticeId)) {
                is ApiResult.Success -> {
                    Log.d("NoticeDetail", "success: cards=${result.body.cards.size}, summary.len=${result.body.summary.length}")
                    _notice.value = result.body
                }
                is ApiResult.Error -> {
                    Log.e("NoticeDetail", "error: msg=${result.message}, code=${result.code}")
                }
            }
            _isLoading.value = false
        }
    }
}
