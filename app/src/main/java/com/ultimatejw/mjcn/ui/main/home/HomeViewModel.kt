package com.ultimatejw.mjcn.ui.main.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ultimatejw.mjcn.R
import com.ultimatejw.mjcn.data.model.ApiResult
import com.ultimatejw.mjcn.domain.model.Info
import com.ultimatejw.mjcn.domain.model.Notice
import com.ultimatejw.mjcn.domain.model.Theme
import com.ultimatejw.mjcn.domain.model.TodayClass
import com.ultimatejw.mjcn.domain.model.User
import com.ultimatejw.mjcn.domain.usecase.bookmark.ObserveInfoBookmarksUseCase
import com.ultimatejw.mjcn.domain.usecase.bookmark.ObserveNoticeBookmarksUseCase
import com.ultimatejw.mjcn.domain.usecase.bookmark.ToggleInfoBookmarkUseCase
import com.ultimatejw.mjcn.domain.usecase.bookmark.ToggleNoticeBookmarkUseCase
import com.ultimatejw.mjcn.domain.usecase.home.GetDashboardUseCase
import com.ultimatejw.mjcn.domain.usecase.user.ObserveCurrentUserUseCase
import com.ultimatejw.mjcn.ui.common.CurrentUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val currentUser: User? = null,
    val dashboardUserName: String = "",
    val todayClasses: List<TodayClass> = emptyList(),
    val infoList: List<Info> = emptyList(),
    val noticeList: List<Notice> = emptyList(),
    val themeList: List<Theme> = emptyList(),
    val courseCount: Int = 0,
    val graduationCredits: Int = 0,
    val dday: String = "D-?",
    val gradProgress: String = "0%",
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val observeCurrentUser: ObserveCurrentUserUseCase,
    private val getDashboard: GetDashboardUseCase,
    private val toggleNoticeBookmark: ToggleNoticeBookmarkUseCase,
    private val toggleInfoBookmark: ToggleInfoBookmarkUseCase,
    private val observeNoticeBookmarks: ObserveNoticeBookmarksUseCase,
    private val observeInfoBookmarks: ObserveInfoBookmarksUseCase,
) : ViewModel() {

    private var rawNoticeList: List<Notice> = emptyList()
    private var rawInfoList: List<Info> = emptyList()
    private var noticeBookmarkedIds: Set<String> = emptySet()
    private var infoBookmarkedIds: Set<String> = emptySet()

    private val _uiState = MutableLiveData(HomeUiState())
    val uiState: LiveData<HomeUiState> = _uiState

    init {
        observeUser()
        loadDashboard()
        observeBookmarks()
        loadThemeList()
    }

    private fun loadDashboard() {
        viewModelScope.launch {
            when (val result = getDashboard()) {
                is ApiResult.Success -> {
                    val data = result.body
                    rawNoticeList = data.notices
                    rawInfoList = data.infoList
                    _uiState.postValue(
                        _uiState.value!!.copy(
                            dashboardUserName = data.userName,
                            todayClasses = data.todayClasses,
                            noticeList = rawNoticeList.map { it.copy(isBookmarked = it.id in noticeBookmarkedIds) },
                            infoList = rawInfoList.map { it.copy(isBookmarked = it.id in infoBookmarkedIds) },
                            courseCount = data.unreadNotificationCount,
                            gradProgress = "${data.graduationProgressPercent}%"
                        )
                    )
                }
                is ApiResult.Error -> { /* 기본 빈 상태 유지 */ }
            }
        }
    }

    private fun observeBookmarks() {
        viewModelScope.launch {
            observeNoticeBookmarks().collect { bookmarked ->
                noticeBookmarkedIds = bookmarked.map { it.id }.toSet()
                _uiState.postValue(
                    _uiState.value!!.copy(
                        noticeList = rawNoticeList.map { it.copy(isBookmarked = it.id in noticeBookmarkedIds) }
                    )
                )
            }
        }
        viewModelScope.launch {
            observeInfoBookmarks().collect { bookmarked ->
                infoBookmarkedIds = bookmarked.map { it.id }.toSet()
                _uiState.postValue(
                    _uiState.value!!.copy(
                        infoList = rawInfoList.map { it.copy(isBookmarked = it.id in infoBookmarkedIds) }
                    )
                )
            }
        }
    }

    fun toggleNoticeBookmark(notice: Notice) {
        viewModelScope.launch { toggleNoticeBookmark.invoke(notice) }
    }

    fun toggleInfoBookmark(info: Info) {
        viewModelScope.launch { toggleInfoBookmark.invoke(info) }
    }

    private fun observeUser() {
        viewModelScope.launch {
            observeCurrentUser().collect { user ->
                _uiState.postValue(_uiState.value!!.copy(currentUser = user))
            }
        }
    }

    private fun loadThemeList() {
        _uiState.value = _uiState.value!!.copy(
            themeList = listOf(
                Theme("1", "${CurrentUser.gradeSemester} 수강신청 가이드", "전공필수와 선택과목 균형있게 설계하기", R.drawable.ic_hat, "#E1F5EE"),
                Theme("2", "나의 취업·진로 로드맵", "인턴십, 자격증, 포트폴리오 뭐 부터 하지?", R.drawable.ic_bag, "#E6F1FB"),
                Theme("3", "교환학생·해외 인턴십 가이드", "나한테 필요할까? 시기는 언제로 가야하지?", R.drawable.ic_plane, "#EAF3DE"),
            )
        )
    }
}
