package com.ultimatejw.mjcn.ui.main.theme

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ultimatejw.mjcn.domain.model.CourseRecommendSections
import com.ultimatejw.mjcn.domain.model.ExchangeGuide
import com.ultimatejw.mjcn.domain.model.QuickQuestion
import com.ultimatejw.mjcn.domain.model.ThemeItem
import com.ultimatejw.mjcn.domain.repository.ThemeRepository
import com.ultimatejw.mjcn.ui.common.CurrentUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ThemeDetailUiState(
    val isLoading: Boolean = false,
    val category: String = "",
    val title: String = "",
    val adviceText: String = "",
    val contentItems: List<ThemeItem> = emptyList(),
    val assessmentItems: List<ThemeItem> = emptyList(),
    val linkItems: List<ThemeItem> = emptyList(),
    val quickQuestions: List<QuickQuestion> = emptyList(),
    val courseRecommend: CourseRecommendSections? = null,
    val exchangeGuide: ExchangeGuide? = null,
    val error: String? = null
)

@HiltViewModel
class ThemeDetailViewModel @Inject constructor(
    private val themeRepository: ThemeRepository
) : ViewModel() {

    private val _uiState = MutableLiveData(ThemeDetailUiState())
    val uiState: LiveData<ThemeDetailUiState> = _uiState

    private var loaded = false

    fun load(themeId: Int, category: String, themeTitle: String) {
        if (loaded) return
        loaded = true
        viewModelScope.launch {
            _uiState.value = ThemeDetailUiState(isLoading = true)
            when (category) {
                "course_registration" -> loadCourseRegistration(themeTitle)
                "career" -> loadCareer(themeTitle)
                "exchange" -> loadExchange(themeTitle)
                else -> {
                    if (themeId <= 0) {
                        _uiState.value = ThemeDetailUiState(isLoading = false, error = "유효하지 않은 테마입니다")
                        return@launch
                    }
                    loadGeneric(themeId)
                }
            }
        }
    }

    private suspend fun loadCourseRegistration(themeTitle: String) {
        themeRepository.fetchCourseRecommend()
            .onSuccess { data ->
                _uiState.value = ThemeDetailUiState(
                    isLoading = false,
                    category = "course_registration",
                    title = themeTitle.ifBlank { "수강신청 가이드" },
                    adviceText = data.adviceText,
                    quickQuestions = data.quickQuestions,
                    courseRecommend = data
                )
            }
            .onFailure { e ->
                _uiState.value = ThemeDetailUiState(isLoading = false, error = e.message)
            }
    }

    private fun loadCareer(themeTitle: String) {
        _uiState.value = ThemeDetailUiState(
            isLoading = false,
            category = "career",
            title = themeTitle.ifBlank { "나의 취업·진로 로드맵" },
            adviceText = "  ${CurrentUser.honorific}은 현재 클라우드 개발자를 목표로 하고 계시네요. " +
                "지금까지 이수 과목을 보면 기초는 충분해서 이제는 \"취업을 위한 실무 준비 단계\"에요😊",
            assessmentItems = listOf(
                ThemeItem(0, "✅ 전공 기초", "클라우드 개발자 기준 충분합니다.", null, "assessment"),
                ThemeItem(0, "⚠ 실무 경험", "프로젝트 경험이 부족합니다.", null, "assessment"),
                ThemeItem(0, "💡 인턴십 경험 (선택)", "프로젝트가 부족하다면 인턴으로 보완할 수 있어요.", null, "assessment"),
            ),
            contentItems = listOf(
                ThemeItem(0, "📍 STEP 1. 방향 확정", "•  클라우드 + 백엔드 병행 추천\n•  AWS or GCP 선택", null, "guide"),
                ThemeItem(0, "📍 STEP 2. 기술 준비", "•  AWS 기본 (EC2, S3)\n•  Docker\n•  Linux", null, "guide"),
                ThemeItem(0, "📍 STEP 3. 포트폴리오",
                    "•  배포 경험 있는 프로젝트 2개 이상 추천\n•  추천 프로젝트 유형\n    •  AWS 배포 웹서비스\n    •  간단한 서버 프로젝트",
                    null, "guide"),
                ThemeItem(0, "📍 STEP 4. 인턴십 (선택 전략)",
                    "필수는 아니지만, 실무 경험을 빠르게 쌓는 방법!\n•  추천 상황\n    •  프로젝트 경험이 부족할 때\n" +
                        "    •  실무 경험 어필이 어려울 때\n•  활용 방법\n    •  여름방학 / 겨울방학 인턴 지원\n" +
                        "    •  스타트업 / 중소기업 위주 지원 추천",
                    null, "guide"),
                ThemeItem(0, "📍 STEP 5. 학기 전략", "•  컴퓨터네트워크\n•  데이터베이스\n•  캡스톤디자인", null, "guide"),
                ThemeItem(0, "📍 STEP 6. 취업 준비", "•  AWS 자격증 1개 (선택)\n•  GitHub 정리", null, "guide"),
            ),
            quickQuestions = listOf(
                QuickQuestion("해외 인턴 준비 방법 보기", "해외 인턴십은 어떻게 준비하면 좋을까?"),
                QuickQuestion("영어 공부 루틴 추천 받기", "취업을 위한 영어 공부 루틴 추천해줘"),
                QuickQuestion("클라우드 취업 로드맵 다시보기", "클라우드 개발자 취업 로드맵 알려줘"),
            )
        )
    }

    private suspend fun loadExchange(themeTitle: String) {
        themeRepository.fetchExchangeGuide()
            .onSuccess { data ->
                _uiState.value = ThemeDetailUiState(
                    isLoading = false,
                    category = "exchange",
                    title = themeTitle.ifBlank { "교환학생·해외 인턴십 가이드" },
                    adviceText = data.adviceText,
                    quickQuestions = data.quickQuestions,
                    exchangeGuide = data
                )
            }
            .onFailure { e ->
                _uiState.value = ThemeDetailUiState(isLoading = false, error = e.message)
            }
    }

    private suspend fun loadGeneric(themeId: Int) {
        themeRepository.fetchThemeDetail(themeId)
            .onSuccess { detail ->
                val guideItems = detail.items.filter { it.itemType == "guide" }
                val checklistItems = detail.items.filter { it.itemType == "checklist" }
                val linkItems = detail.items.filter { it.itemType == "link" }

                val adviceText = guideItems.firstOrNull()?.let { first ->
                    if (first.title.isNotBlank()) "${first.title}\n\n${first.content}"
                    else first.content
                } ?: detail.description

                _uiState.value = ThemeDetailUiState(
                    isLoading = false,
                    category = detail.category,
                    title = detail.title,
                    adviceText = adviceText,
                    contentItems = guideItems.drop(1) + checklistItems,
                    linkItems = linkItems
                )
            }
            .onFailure { e ->
                _uiState.value = ThemeDetailUiState(isLoading = false, error = e.message)
            }
    }
}
