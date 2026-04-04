package com.ultimatejw.mjcn.ui.main.theme

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ultimatejw.mjcn.R
import com.ultimatejw.mjcn.data.model.Theme

class ThemeViewModel : ViewModel() {

    private val _themes = MutableLiveData<List<Theme>>()
    val themes: LiveData<List<Theme>> = _themes

    init {
        loadThemes()
    }

    private fun loadThemes() {
        // TODO: 실제 API에서 불러오기
        _themes.value = listOf(
            Theme("1", "3학년 1학기 수강신청 가이드", "전공필수와 선택과목 균형있게 설계하기", R.drawable.ic_nav_theme, "#E8F5E9"),
            Theme("2", "나의 취업·진로 로드맵", "인턴십, 자격증, 포트폴리오 뭐 부터 하지?", R.drawable.ic_nav_info, "#E3F2FD"),
            Theme("3", "교환학생·해외 인턴십 가이드", "나한테 필요할까? 시기는 언제로 가야하지?", R.drawable.ic_nav_notice, "#FFF8E1"),
            Theme("4", "국가 지원 사업 신청하기", "내가 신청할 수 있는 국가 사업 알아보기", R.drawable.ic_nav_home, "#FCE4EC"),
            Theme("5", "학업 스트레스&시간관리 꿀팁", "건강한 멘탈 관리와 효율적인 시간 관리", R.drawable.ic_nav_chat, "#F3E5F5")
        )
    }
}
