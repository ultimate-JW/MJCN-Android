package com.ultimatejw.mjcn.ui.main.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ultimatejw.mjcn.R
import com.ultimatejw.mjcn.domain.model.Info
import com.ultimatejw.mjcn.domain.model.Notice
import com.ultimatejw.mjcn.domain.model.Theme
import com.ultimatejw.mjcn.domain.model.TodayClass
import com.ultimatejw.mjcn.domain.model.User
import com.ultimatejw.mjcn.domain.usecase.notice.GetAllNoticesUseCase
import com.ultimatejw.mjcn.domain.usecase.user.ObserveCurrentUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val currentUser: User? = null,
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
    private val getAllNotices: GetAllNoticesUseCase,
) : ViewModel() {

    private val _uiState = MutableLiveData(HomeUiState())
    val uiState: LiveData<HomeUiState> = _uiState

    init {
        observeUser()
        loadTodayClasses()
        loadInfoList()
        loadNoticeList()
        loadThemeList()
        // TODO: 실제 API에서 데이터 불러오기
        _uiState.value = _uiState.value!!.copy(courseCount = 3, graduationCredits = 12, dday = "D-42", gradProgress = "87%")
    }

    private fun observeUser() {
        viewModelScope.launch {
            observeCurrentUser().collect { user ->
                _uiState.postValue(_uiState.value!!.copy(currentUser = user))
            }
        }
    }

    private fun loadTodayClasses() {
        // TODO: 실제 API 연동
        _uiState.value = _uiState.value!!.copy(
            todayClasses = listOf(
                TodayClass("1", "캡스톤디자인", "09:00", "10:30", "5공학관", "Y5411", "김교수"),
                TodayClass("2", "운영체제", "13:00", "14:30", "3공학관", "Y9010", "이교수"),
                TodayClass("3", "모바일프로그래밍", "15:00", "16:30", "함박관", "Y7044", "박교수"),
            )
        )
    }

    private fun loadInfoList() {
        // TODO: 실제 API 연동
        _uiState.value = _uiState.value!!.copy(
            infoList = listOf(
                Info("1", "2026학년도 1학기 수강신청 정정 안내", "부트캠프", "과학기술정보통신부", true, 22),
                Info("2", "졸업요건 확인 방법 총정리", "공모전", "과학기술정보통신부", true, 35),
                Info("3", "국가근로장학금 2차 모집 안내", "교육/강의", "과학기술정보통신부", false, 45),
            )
        )
    }

    private fun loadNoticeList() {
        // TODO: 실제 API 연동
        _uiState.value = _uiState.value!!.copy(
            noticeList = listOf(
                Notice("1", "2026 선배와의 취업멘토링 참여학생 모집", "진로/취업/창업", "자연취업진로지원팀", "1시간 전", true),
                Notice("2", "졸업요건 확인 방법 총정리", "학사", "인문지로취업지원팀", "1시간 전", false),
                Notice("3", "국가근로장학금 2차 모집 안내", "일반", "교육지원팀", "1시간 전", false),
            )
        )
    }

    private fun loadThemeList() {
        // TODO: 실제 API 연동
        _uiState.value = _uiState.value!!.copy(
            themeList = listOf(
                Theme("1", "3학년 1학기 수강신청 가이드", "전공필수와 선택과목 균형있게 설계하기", R.drawable.ic_nav_theme, "#E8F5E9"),
                Theme("2", "나의 취업·진로 로드맵", "인턴십, 자격증, 포트폴리오 뭐 부터 하지?", R.drawable.ic_nav_info, "#E3F2FD"),
                Theme("3", "교환학생·해외 인턴십 가이드", "나한테 필요할까? 시기는 언제로 가야하지?", R.drawable.ic_nav_notice, "#FFF8E1"),
            )
        )
    }
}
