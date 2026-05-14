package com.ultimatejw.mjcn.ui.main.notice

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.ultimatejw.mjcn.R
import com.ultimatejw.mjcn.databinding.FragmentNoticeDetailBinding
import com.ultimatejw.mjcn.domain.model.NoticeCategory
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NoticeDetailFragment : Fragment() {

    private var _binding: FragmentNoticeDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNoticeDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val noticeId = arguments?.getString("noticeId").orEmpty()
        val mockup = pickMockup(noticeId)
        applyMockup(mockup)

        // TODO: 원문 링크 연결
        binding.btnOriginalLink.setOnClickListener { /* TODO: open original URL */ }

        setupQuestionBarToggle()
    }

    /**
     * inline 질문바를 클릭하면 화면 하단(키보드 위)에 떠 있는 floating 질문바로 전환되어
     * 사용자가 입력하는 동안 텍스트가 가려지지 않도록 한다. 키보드가 내려가면 다시 inline 으로 복귀.
     */
    private fun setupQuestionBarToggle() {
        binding.inlineInput.setOnClickListener { showFloating() }

        // 키보드(IME) 가시성 변화를 감지해서 닫히면 inline 으로 복귀
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            if (!imeVisible && binding.floatingInput.visibility == View.VISIBLE) {
                hideFloating()
            }
            insets
        }
    }

    private fun showFloating() {
        binding.inlineInput.visibility = View.INVISIBLE
        binding.floatingInput.visibility = View.VISIBLE
        binding.etMessage.requestFocus()
        val imm = requireContext()
            .getSystemService(InputMethodManager::class.java)
        imm?.showSoftInput(binding.etMessage, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun hideFloating() {
        binding.floatingInput.visibility = View.GONE
        binding.inlineInput.visibility = View.VISIBLE
        binding.etMessage.clearFocus()
        binding.etMessage.text?.clear()
    }

    private fun applyMockup(m: NoticeMockup) {
        binding.tvCategory.text = m.category
        binding.tvTitle.text = m.title
        binding.tvTeamDate.text = if (m.team.isBlank()) m.date else "${m.team} · ${m.date}"
        binding.tvSummary.text = m.summary
        applyCategoryChip(m.category)
        renderKeypoints(m.keypoints)
    }

    private fun renderKeypoints(items: List<Keypoint>) {
        val container = binding.layoutKeypointsInner
        container.removeAllViews()
        val inflater = LayoutInflater.from(requireContext())
        val gap = (12 * resources.displayMetrics.density).toInt()
        items.forEachIndexed { index, kp ->
            val card = inflater.inflate(R.layout.item_keypoint_card, container, false) as LinearLayout
            card.findViewById<TextView>(R.id.tv_kp_title).text = kp.title
            card.findViewById<TextView>(R.id.tv_kp_body).text = kp.body
            val lp = card.layoutParams as LinearLayout.LayoutParams
            lp.bottomMargin = if (index == items.lastIndex) 0 else gap
            card.layoutParams = lp
            container.addView(card)
        }
    }

    private fun applyCategoryChip(categoryLabel: String) {
        val context = binding.root.context
        val category = NoticeCategory.from(categoryLabel)
        val bgColorRes = when (category) {
            NoticeCategory.NORMAL             -> R.color.category_normal_bg
            NoticeCategory.ACADEMIC           -> R.color.category_academic_bg
            NoticeCategory.OVERSEAS           -> R.color.category_overseas_bg
            NoticeCategory.CONTEST            -> R.color.category_contest_bg
            NoticeCategory.ACTIVITY           -> R.color.category_activity_bg
            NoticeCategory.CAREER             -> R.color.category_career_bg
            NoticeCategory.CAREER_SIMPLE      -> R.color.category_career_simple_bg
            NoticeCategory.SCHOLARSHIP        -> R.color.category_scholarship_bg
            NoticeCategory.SCHOLARSHIP_SIMPLE -> R.color.category_scholarship_simple_bg
        }
        val textColorRes = when (category) {
            NoticeCategory.NORMAL             -> R.color.category_normal_text
            NoticeCategory.ACADEMIC           -> R.color.category_academic_text
            NoticeCategory.OVERSEAS           -> R.color.category_overseas_text
            NoticeCategory.CONTEST            -> R.color.category_contest_text
            NoticeCategory.ACTIVITY           -> R.color.category_activity_text
            NoticeCategory.CAREER             -> R.color.category_career_text
            NoticeCategory.CAREER_SIMPLE      -> R.color.category_career_simple_text
            NoticeCategory.SCHOLARSHIP        -> R.color.category_scholarship_text
            NoticeCategory.SCHOLARSHIP_SIMPLE -> R.color.category_scholarship_simple_text
        }
        val radius = context.resources.displayMetrics.density * 22
        binding.layoutCategoryChip.background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(ContextCompat.getColor(context, bgColorRes))
            cornerRadius = radius
        }
        binding.tvCategory.setTextColor(ContextCompat.getColor(context, textColorRes))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // TODO: API 연결 시 도메인 모델 + ViewModel로 대체. 지금은 리스트 위치별 더미 mockup.
    private fun pickMockup(noticeId: String): NoticeMockup = when (noticeId) {
        "c2", "a2" -> mockup2()
        else -> mockup1()
    }

    private data class Keypoint(val title: String, val body: String)

    private data class NoticeMockup(
        val category: String,
        val title: String,
        val team: String,
        val date: String,
        val summary: String,
        val keypoints: List<Keypoint>,
    )

    private fun mockup1(): NoticeMockup = NoticeMockup(
        category = "학사",
        title = "2026학년도 1학기 대학 재학생 등록금 구제 납부 안내",
        team = "교육지원팀",
        date = "1시간 전",
        summary = "등록기간 3/23~3/25, 고지서 MSI 출력 후 가상계좌·은행 납부 가능, " +
            "다음날 납부 확인, 환불 없이 이월 처리됩니다.",
        keypoints = listOf(
            Keypoint(
                "📌 등록 기간",
                "•  재학생: 2026.03.23 ~ 03.25\n" +
                    "•  초과학기 / 유예자: 2026.03.16 ~ 03.25"
            ),
            Keypoint(
                "📃 고지서 출력",
                "•  MSI(학생정보시스템) 접속\n" +
                    "•  로그인 → 등록금 관련 출력 → 고지서 확인"
            ),
            Keypoint(
                "💵 납부 방법",
                "1. 가상계좌 납부\n" +
                    "    •  고지서에 있는 계좌로 이체\n" +
                    "    •  납부시간: 00:00 ~ 23:30\n" +
                    "2. 은행 방문 납부\n" +
                    "    •  전국 은행 방문\n" +
                    "    •  납부시간: 09:00 ~ 16:00"
            ),
            Keypoint(
                "✅ 납부 확인",
                "•  하나은행 홈페이지 → 즉시 확인 가능\n" +
                    "•  MSI → 다음날 12시 이후 확인 가능"
            ),
            Keypoint(
                "⚠ 주의사항",
                "•  등록금은 환불되지 않고 이월 처리됨\n" +
                    "•  초과학기생은 학점확인서 제출 필요"
            ),
            Keypoint(
                "📞 문의",
                "•  1577-0020"
            ),
        )
    )

    private fun mockup2(): NoticeMockup = NoticeMockup(
        category = "학사",
        title = "2026-1학기 과(전공)변경자 이수구분 일괄 변경 안내",
        team = "교육지원팀",
        date = "1시간 전",
        summary = "전과·다전공 등으로 전공 이수구분이 변경되었습니다. " +
            "MSI에서 이수구분을 반드시 확인하고, 오류 시 교학팀/학사지원팀에 문의하세요.",
        keypoints = listOf(
            Keypoint(
                "👤 대상 확인",
                "•  전과(전공변경) 승인 학생\n" +
                    "•  다전공 신청/포기 학생\n" +
                    "•  학과·전공 선택 변경 학생"
            ),
            Keypoint(
                "🔄 변경 내용",
                "•  전공 교과목 이수구분 자동 변경됨"
            ),
            Keypoint(
                "🔍 확인 방법",
                "•  MSI 접속\n" +
                    "•  성적/졸업 → 성적조회 → 이수구분 확인"
            ),
            Keypoint(
                "⚠ 꼭 확인할 것 (중요)",
                "1. 교양 과목\n" +
                    "    •  자동 변경 아님\n" +
                    "    •  잘못된 경우 → 교학팀 방문\n" +
                    "2. 전공 과목\n" +
                    "    •  소속 기준과 다르면 → 학사지원팀 문의"
            ),
            Keypoint(
                "📞 문의",
                "•  인문: 02-300-1471\n" +
                    "•  자연: 031-330-6026"
            ),
        )
    )
}
