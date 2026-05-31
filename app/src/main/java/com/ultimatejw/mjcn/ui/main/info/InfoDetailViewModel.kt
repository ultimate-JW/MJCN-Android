package com.ultimatejw.mjcn.ui.main.info

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ultimatejw.mjcn.data.model.ApiResult
import com.ultimatejw.mjcn.domain.model.Info
import com.ultimatejw.mjcn.domain.repository.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InfoDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: HomeRepository
) : ViewModel() {

    private val infoId: String = savedStateHandle.get<String>("infoId").orEmpty()

    private val _info = MutableLiveData<Info>()
    val info: LiveData<Info> = _info

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        if (infoId.isNotBlank()) loadDetail()
    }

    private fun loadDetail() {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = repository.getInfoDetail(infoId)) {
                is ApiResult.Success -> _info.value = result.body
                is ApiResult.Error -> { /* args 데이터로 유지 */ }
            }
            _isLoading.value = false
        }
    }
}
