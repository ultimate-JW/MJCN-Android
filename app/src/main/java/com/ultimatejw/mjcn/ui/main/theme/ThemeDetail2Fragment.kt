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
import com.ultimatejw.mjcn.databinding.FragmentThemeDetail2Binding
import com.ultimatejw.mjcn.ui.common.CurrentUser
import dagger.hilt.android.AndroidEntryPoint

/**
 * 테마 2 — 나의 취업 · 진로 로드맵.
 * 평가 항목 + 단계별 로드맵 STEP 카드 구조.
 */
@AndroidEntryPoint
class ThemeDetail2Fragment : Fragment() {

    private var _binding: FragmentThemeDetail2Binding? = null
    private val binding get() = _binding!!
    private var questionBar: ThemeQuestionBarController? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentThemeDetail2Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnBack.setOnClickListener { findNavController().popBackStack() }

        binding.tvAdvice.text =
            "  ${CurrentUser.honorific}은 현재 클라우드 개발자를 목표로 하고 계시네요. 지금까지 이수 과목을 보면 기초는 충분해서 이제는 \"취업을 위한 실무 준비 단계\"에요😊"

        renderAssessments(
            binding.containerAssessment,
            listOf(
                ThemeAssessment("✅ 전공 기초", "클라우드 개발자 기준 충분합니다."),
                ThemeAssessment("⚠ 실무 경험", "프로젝트 경험이 부족합니다."),
                ThemeAssessment("💡 인턴십 경험 (선택)", "프로젝트가 부족하다면 인턴으로 보완할 수 있어요."),
            )
        )
        renderRoadmap(
            binding.containerRoadmap,
            listOf(
                ThemeRoadmapStep(
                    "📍 STEP 1. 방향 확정",
                    "•  클라우드 + 백엔드 병행 추천\n•  AWS or GCP 선택"
                ),
                ThemeRoadmapStep(
                    "📍 STEP 2. 기술 준비",
                    "•  AWS 기본 (EC2, S3)\n•  Docker\n•  Linux"
                ),
                ThemeRoadmapStep(
                    "📍 STEP 3. 포트폴리오",
                    "•  배포 경험 있는 프로젝트 2개 이상 추천\n" +
                        "•  추천 프로젝트 유형\n" +
                        "    •  AWS 배포 웹서비스\n" +
                        "    •  간단한 서버 프로젝트"
                ),
                ThemeRoadmapStep(
                    "📍 STEP 4. 인턴십 (선택 전략)",
                    "필수는 아니지만, 실무 경험을 빠르게 쌓는 방법!\n" +
                        "•  추천 상황\n" +
                        "    •  프로젝트 경험이 부족할 때\n" +
                        "    •  실무 경험 어필이 어려울 때\n" +
                        "•  활용 방법\n" +
                        "    •  여름방학 / 겨울방학 인턴 지원\n" +
                        "    •  스타트업 / 중소기업 위주 지원 추천"
                ),
                ThemeRoadmapStep(
                    "📍 STEP 5. 학기 전략",
                    "•  컴퓨터네트워크\n•  데이터베이스\n•  캡스톤디자인"
                ),
                ThemeRoadmapStep(
                    "📍 STEP 6. 취업 준비",
                    "•  AWS 자격증 1개 (선택)\n•  GitHub 정리"
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

    private fun renderAssessments(container: LinearLayout, items: List<ThemeAssessment>) {
        container.removeAllViews()
        val inflater = LayoutInflater.from(requireContext())
        val gap = (8 * resources.displayMetrics.density).toInt()
        items.forEachIndexed { index, item ->
            val view = inflater.inflate(R.layout.item_theme_assessment, container, false) as LinearLayout
            view.findViewById<TextView>(R.id.tv_assessment_title).text = item.title
            view.findViewById<TextView>(R.id.tv_assessment_subtitle).text = item.subtitle
            val lp = view.layoutParams as LinearLayout.LayoutParams
            lp.topMargin = if (index == 0) 0 else gap
            view.layoutParams = lp
            container.addView(view)
        }
    }

    private fun renderRoadmap(container: LinearLayout, steps: List<ThemeRoadmapStep>) {
        container.removeAllViews()
        val inflater = LayoutInflater.from(requireContext())
        val gap = (12 * resources.displayMetrics.density).toInt()
        val stepBg = ContextCompat.getDrawable(
            requireContext(),
            R.drawable.bg_theme_assessment_item
        )
        steps.forEachIndexed { index, step ->
            val card = inflater.inflate(R.layout.item_keypoint_card, container, false) as LinearLayout
            card.background = stepBg?.constantState?.newDrawable()?.mutate()
            card.findViewById<TextView>(R.id.tv_kp_title).text = step.title
            card.findViewById<TextView>(R.id.tv_kp_body).text = step.body
            val lp = card.layoutParams as LinearLayout.LayoutParams
            lp.bottomMargin = if (index == steps.lastIndex) 0 else gap
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
