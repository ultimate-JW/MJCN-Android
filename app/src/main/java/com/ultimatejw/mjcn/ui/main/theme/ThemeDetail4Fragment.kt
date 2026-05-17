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
import com.ultimatejw.mjcn.databinding.FragmentThemeDetail4Binding
import com.ultimatejw.mjcn.ui.common.CurrentUser
import dagger.hilt.android.AndroidEntryPoint

/**
 * 테마 4 — 국가 지원 사업 신청하기.
 * 프로그램 카드 N개(각 카드 안: 어떤 사업? / 혜택 / 자격요건 + 신청 사이트 바로가기 버튼)
 * + 사용자 맞춤 우선순위 카드 (우선순위 리스트 + 신청 사이트 바로가기 버튼).
 */
@AndroidEntryPoint
class ThemeDetail4Fragment : Fragment() {

    private var _binding: FragmentThemeDetail4Binding? = null
    private val binding get() = _binding!!
    private var questionBar: ThemeQuestionBarController? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentThemeDetail4Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnBack.setOnClickListener { findNavController().popBackStack() }

        binding.tvAdvice.text =
            "  ${CurrentUser.honorific}은 현재 2001년 3월 30일생 만 22세로 대부분의 청년 대상 지원사업 신청 가능 구간이에요.\n" +
                "  특히 지금은 \"취업 준비 + 실무 경험 + 경제 지원\"을 같이 가져가는 게 중요해요.\n" +
                "  신청 가능한 국가 사업을 모두 알려드릴게요!"

        binding.tvPriorityTitle.text = "${CurrentUser.honorific} 맞춤 우선순위"

        renderPrograms(binding.containerPrograms, dummyPrograms())
        renderPriorities(binding.containerPriorities, dummyPriorities())

        binding.btnPriorityApply.setOnClickListener { }

        questionBar = ThemeQuestionBarController(
            root = binding.root,
            inlineInput = binding.inlineInput,
            floatingInput = binding.floatingInput,
            cardInput = binding.cardInput,
            etMessage = binding.etMessage,
        ).also { it.setup() }
    }

    private fun dummyPrograms(): List<ThemeProgram> = listOf(
        ThemeProgram(
            title = "국민 취업제도",
            what = "•  취업 준비 지원 + 상담 + 교육 + 수당 지급",
            benefits = "•  월 최대 50만 원 × 6개월\n•  취업 컨설팅 + 직무 교육",
            requirements = "•  만 18~34세 청년\n•  미취업 상태\n•  소득 기준 충족 필요 (중위소득 기준)",
        ),
        ThemeProgram(
            title = "K-Digital Training (개발자 과정)",
            what = "•  정부 지원 IT 교육 (클라우드 / AI / 백엔드 등)",
            benefits = "•  교육비 전액 지원\n•  훈련장려금 지급",
            requirements = "•  만 18~34세\n" +
                "•  미취업자 또는 졸업예정자\n" +
                "•  졸업 전이면 일부 제한 있음(확인 필요)\n" +
                "•  졸업 직전/유예 시 매우 유리",
        ),
        ThemeProgram(
            title = "청년내일저축계좌",
            what = "•  저축하면 정부가 추가로 지원금 매칭",
            benefits = "•  월 10만 원 저축 → 정부 지원 추가 지급",
            requirements = "•  만 19~34세\n•  소득 기준 충족 (중위소득 이하)",
        ),
        ThemeProgram(
            title = "청년 도전 지원사업",
            what = "•  취업 준비 초기 청년 지원 프로그램",
            benefits = "•  참여 수당 지급 + 프로그램 지원",
            requirements = "•  최근 취업/교육 참여 이력 적은 청년\n•  현재 대학 재학 중이면 일부 제한 가능",
        ),
        ThemeProgram(
            title = "ICT 인턴십 / SW 인턴십",
            what = "•  정부 연계 IT 인턴 프로그램",
            benefits = "•  실무 경험 + 급여 지급",
            requirements = "•  대학 재학생 또는 졸업 예정자\n•  전공 관련성 필요",
        ),
    )

    private data class PriorityItem(val rank: String, val body: String)

    private fun dummyPriorities(): List<PriorityItem> = listOf(
        PriorityItem("👉 1순위", "•  ICT 인턴십 / 개발 인턴 (실무 경험)"),
        PriorityItem("👉 2순위", "•  국민취업지원제도 (금전 + 컨설팅)"),
        PriorityItem("👉 3순위", "•  청년 금융 지원 (저축계좌 등)"),
        PriorityItem("👉 4순위", "•  K-Digital Training (졸업 직전 활용)"),
    )

    private fun renderPrograms(container: LinearLayout, programs: List<ThemeProgram>) {
        container.removeAllViews()
        val inflater = LayoutInflater.from(requireContext())
        // 제목이 흰 카드 밖으로 빠지면서 프로그램 간 시각적 분리가 더 필요해짐 → 24dp 간격
        val gap = (24 * resources.displayMetrics.density).toInt()
        programs.forEachIndexed { index, program ->
            val card = inflater.inflate(R.layout.item_theme_program, container, false) as LinearLayout
            card.findViewById<TextView>(R.id.tv_program_title).text = program.title
            card.findViewById<TextView>(R.id.tv_program_what).text = program.what
            card.findViewById<TextView>(R.id.tv_program_benefits).text = program.benefits
            card.findViewById<TextView>(R.id.tv_program_requirements).text = program.requirements
            card.findViewById<View>(R.id.btn_apply).setOnClickListener { }
            val lp = card.layoutParams as LinearLayout.LayoutParams
            lp.topMargin = if (index == 0) 0 else gap
            card.layoutParams = lp
            container.addView(card)
        }
    }

    private fun renderPriorities(container: LinearLayout, items: List<PriorityItem>) {
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
            card.findViewById<TextView>(R.id.tv_kp_title).text = item.rank
            card.findViewById<TextView>(R.id.tv_kp_body).text = item.body
            val lp = card.layoutParams as LinearLayout.LayoutParams
            lp.topMargin = if (index == 0) 0 else gap
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
