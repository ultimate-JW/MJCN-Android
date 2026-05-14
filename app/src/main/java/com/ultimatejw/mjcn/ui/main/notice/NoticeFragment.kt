package com.ultimatejw.mjcn.ui.main.notice

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.ultimatejw.mjcn.R
import com.ultimatejw.mjcn.databinding.FragmentNoticeBinding
import com.ultimatejw.mjcn.domain.model.Notice
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NoticeFragment : Fragment() {

    private var _binding: FragmentNoticeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NoticeViewModel by viewModels()

    private lateinit var noticeAdapter: NoticeListAdapter
    private lateinit var chipAdapter: NoticeCategoryChipAdapter

    private var isCustomTab = true
    private var selectedCategory = "전체"
    private var searchQuery = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNoticeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        setupNoticeList()
        setupCategoryChips()
        setupSearch()
        setupTabs()
    }

    private fun setupTabs() {
        binding.tabCustom.setOnClickListener { selectTab(custom = true) }
        binding.tabAll.setOnClickListener { selectTab(custom = false) }
        selectTab(custom = true)
    }

    private fun selectTab(custom: Boolean) {
        isCustomTab = custom
        val primary = requireContext().getColor(R.color.point_color1)
        val muted = requireContext().getColor(R.color.font_color2)

        binding.tvTabCustom.setTextColor(if (custom) primary else muted)
        binding.tvTabAll.setTextColor(if (custom) muted else primary)
        binding.tvTabCustom.setTypeface(
            androidx.core.content.res.ResourcesCompat.getFont(
                requireContext(),
                if (custom) R.font.pretendard_bold else R.font.pretendard_medium
            )
        )
        binding.tvTabAll.setTypeface(
            androidx.core.content.res.ResourcesCompat.getFont(
                requireContext(),
                if (custom) R.font.pretendard_medium else R.font.pretendard_bold
            )
        )
        binding.indicatorCustom.visibility = if (custom) View.VISIBLE else View.INVISIBLE
        binding.indicatorAll.visibility = if (custom) View.INVISIBLE else View.VISIBLE

        applyFilter()
    }

    private fun setupCategoryChips() {
        val categories = listOf(
            "전체", "일반", "학사", "공모전/대외활동",
            "장학/학자금", "취업", "해외", "지원사업"
        )
        chipAdapter = NoticeCategoryChipAdapter(categories) { selected ->
            selectedCategory = selected
            applyFilter()
        }
        binding.rvCategories.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvCategories.adapter = chipAdapter
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                searchQuery = s?.toString().orEmpty().trim()
                applyFilter()
            }
        })
    }

    private fun setupNoticeList() {
        noticeAdapter = NoticeListAdapter(
            onItemClick = { /* TODO: 상세 화면 이동 */ },
            onBookmarkClick = { /* TODO: 북마크 토글 */ }
        )
        binding.rvNotices.layoutManager = LinearLayoutManager(requireContext())
        binding.rvNotices.adapter = noticeAdapter
    }

    private fun applyFilter() {
        val source = if (isCustomTab) buildCustomNotices() else buildAllNotices()
        val filtered = source
            .filter { matchCategory(it.category, selectedCategory) }
            .filter { searchQuery.isEmpty() || it.title.contains(searchQuery, ignoreCase = true) }
        noticeAdapter.submitList(filtered)
        binding.tvTotalCount.text = getString(R.string.notice_total_count_format, filtered.size)
    }

    // 칩 라벨에 "/"가 있으면 부분 중 하나라도 일치하면 통과. 양방향 substring 비교로 부분 일치도 매칭.
    private fun matchCategory(itemCategory: String, chipLabel: String): Boolean {
        if (chipLabel == "전체") return true
        val parts = chipLabel.split("/").map { it.trim() }.filter { it.isNotEmpty() }
        return parts.any { part ->
            itemCategory == part ||
                itemCategory.contains(part) ||
                part.contains(itemCategory)
        }
    }

    // TODO: API 연결 시 ViewModel.uiState로 교체
    private fun buildCustomNotices(): List<Notice> = listOf(
        Notice("c1", "2026 동문초청 멘토링(Legend 명지 人) 개최",
            "진로/취업/창업", "자연진로취업지원팀", "1시간 전"),
        Notice("c2", "파견교환학생 및 토플 설명회 안내",
            "해외", "국제교류처", "23시간 전"),
        Notice("c3", "국제교류학생클럽(어우라미) 29기 신규 회원 모집",
            "일반", "국제교류지원팀", "5시간 전"),
        Notice("c4", "「제13회 국가승인통계 활용 디지털콘텐츠 공모전」",
            "공모전", "", "5시간 전"),
        Notice("c5", "2026학년도 2학기 수강 희망 교과목 사전조사",
            "학사", "", "5시간 전"),
        Notice("c6", "2026년 화성시인재육성재단 「꿈드림 장학금」",
            "장학/학자금", "", "5시간 전"),
        Notice("c7", "서울영커리언스 캠프 참여자 모집",
            "취업", "서울특별시", "5시간 전"),
        Notice("c8", "일학습병행 학생용 소개자료 공유",
            "일반", "컴퓨터공학과", "5시간 전"),
    )

    // TODO: API 연결 시 ViewModel.uiState로 교체
    private fun buildAllNotices(): List<Notice> = listOf(
        Notice("a1", "[ABI-X사업단] 특임교수 채용 공고",
            "학사", "AI-Bigdata-ICT융합교육", "1시간 전"),
        Notice("a2", "전북특별자치도 2026년도 향토인재 장학생 …",
            "지원사업", "", "3시간 전"),
        Notice("a3", "2026학년도「전공·진로 Festival」학부 학과 전…",
            "일반", "학사지원팀", "8시간 전"),
        Notice("a4", "2026학년도 1학기 강좌폐강 안내(최종)",
            "대외활동", "학사지원팀", "23시간 전"),
        Notice("a5", "공기업(한국가스기술공사)동문선배와의 TAL…",
            "진로/취업/창업", "MJ대학일자리플러스센터", "2일 전"),
        Notice("a6", "2026 선배와의 취업멘토링 참여학생 모집(4월)",
            "교육/강의", "MJ대학일자리플러스센터", "5시간 전"),
        Notice("a7", "2026 선배와의 취업멘토링 참여학생 모집(4월)",
            "공모전", "AI-Bigdata-ICT융합교육", "오늘"),
        Notice("a8", "2026 창업 아이디어 경진대회 참가자 모집(5월)",
            "진로/취업/창업", "스타트업·혁신", "내일"),
        Notice("a9", "2026 빅데이터 분석 워크숍 신청 안내(6월)",
            "장학", "데이터과학·분석", "이번주"),
        Notice("a10", "2026 여름방학 AI 캠프 참가자 모집(7월)",
            "진로/취업/창업", "인공지능·교육", "다음달"),
    )

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
