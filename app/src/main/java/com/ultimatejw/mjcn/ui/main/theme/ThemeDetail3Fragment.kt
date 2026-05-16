package com.ultimatejw.mjcn.ui.main.theme

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.ultimatejw.mjcn.R
import com.ultimatejw.mjcn.databinding.FragmentThemeDetail3Binding
import com.ultimatejw.mjcn.ui.common.CurrentUser
import dagger.hilt.android.AndroidEntryPoint

/**
 * 테마 3 — 교환학생 · 해외 인턴십 가이드.
 * 3개 흰 카드 섹션: 나에게 필요한 선택일까? / 판단 기준에 따른 평가 / 지금 시점 기준 추천.
 * 각 섹션 안의 항목은 공지 상세의 핵심정리 카드 디자인 재사용.
 */
@AndroidEntryPoint
class ThemeDetail3Fragment : Fragment() {

    private var _binding: FragmentThemeDetail3Binding? = null
    private val binding get() = _binding!!
    private var questionBar: ThemeQuestionBarController? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentThemeDetail3Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnBack.setOnClickListener { findNavController().popBackStack() }

        binding.tvAdvice.text =
            "  ${CurrentUser.honorific}은 현재 ${CurrentUser.major} ${CurrentUser.gradeSemesterShort}로 지금 시점에서는 \"해외 경험\"보다 취업 준비 완성도가 더 중요한 단계예요.\n" +
                "  하지만 방향에 따라 교환학생이나 해외 인턴도 충분히 좋은 선택이 될 수 있어요."

        renderItems(
            binding.containerChoice,
            listOf(
                ThemeRoadmapStep(
                    "🎓 교환학생",
                    "•  필요도: ⭐⭐☆☆☆ (2점)\n👉 경험/언어에는 도움 되지만, 취업 직접 영향은 적은 편"
                ),
                ThemeRoadmapStep(
                    "🌏 해외 인턴십",
                    "•  필요도: ⭐⭐⭐⭐☆ (4점)\n👉 잘만 가면 \"강력한 스펙\"이 될 수 있음"
                ),
            )
        )

        renderItems(
            binding.containerEvaluation,
            listOf(
                ThemeRoadmapStep(
                    "✓ 교환학생이 맞는 경우",
                    "•  영어 경험 쌓고 싶을 때\n" +
                        "•  대학 생활 경험 중요하게 생각할 때\n" +
                        "👉 BUT 취업 준비는 늦어질 수 있음"
                ),
                ThemeRoadmapStep(
                    "✓ 해외 인턴이 맞는 경우",
                    "•  실무 경험 쌓고 싶을 때\n" +
                        "•  차별화된 스펙 만들고 싶을 때\n" +
                        "👉 클라우드/개발이면 훨씬 가치 있음"
                ),
            )
        )

        renderItems(
            binding.containerRecommendation,
            listOf(
                ThemeRoadmapStep(
                    "1순위: 프로젝트 + 포트폴리오",
                    "•  경험 쌓아서 취업 준비"
                ),
                ThemeRoadmapStep(
                    "2순위: 해외 인턴십",
                    "•  3-2 방학 / 4-1 방학 추천\n👉 포폴 만든 뒤 지원하는 게 베스트"
                ),
                ThemeRoadmapStep(
                    "3순위: 교환학생",
                    "•  시기는 3-2학기 또는 4-2 학기 추천\n👉 너무 늦으면 취업 준비 영향 있음"
                ),
            )
        )

        binding.btnQuick1.setOnClickListener { }
        binding.btnQuick2.setOnClickListener { }
        binding.btnQuick3.setOnClickListener { }

        questionBar = ThemeQuestionBarController(
            root = binding.root,
            inlineInput = binding.inlineInput,
            floatingInput = binding.floatingInput,
            cardInput = binding.cardInput,
            etMessage = binding.etMessage,
        ).also { it.setup() }
    }

    /** 핵심정리 카드 디자인 재사용 — 배경은 #F9F8FF (bg_theme_assessment_item) */
    private fun renderItems(container: LinearLayout, items: List<ThemeRoadmapStep>) {
        container.removeAllViews()
        val inflater = LayoutInflater.from(requireContext())
        val gap = (12 * resources.displayMetrics.density).toInt()
        val itemBg = ContextCompat.getDrawable(
            requireContext(),
            R.drawable.bg_theme_assessment_item
        )
        items.forEachIndexed { index, item ->
            val card = inflater.inflate(R.layout.item_keypoint_card, container, false) as LinearLayout
            card.background = itemBg?.constantState?.newDrawable()?.mutate()
            card.findViewById<TextView>(R.id.tv_kp_title).text = item.title
            card.findViewById<TextView>(R.id.tv_kp_body).text = item.body
            val lp = card.layoutParams as LinearLayout.LayoutParams
            lp.bottomMargin = if (index == items.lastIndex) 0 else gap
            card.layoutParams = lp
            container.addView(card)
        }
    }

    override fun onDestroyView() {
        questionBar?.cancel()
        questionBar = null
        super.onDestroyView()
        _binding = null
    }
}
