package com.ultimatejw.mjcn.ui.main.info

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ultimatejw.mjcn.R
import com.ultimatejw.mjcn.databinding.FragmentInfoBinding
import com.ultimatejw.mjcn.domain.model.Info
import com.ultimatejw.mjcn.ui.main.notice.NoticeCategoryChipAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class InfoFragment : Fragment() {

    private var _binding: FragmentInfoBinding? = null
    private val binding get() = _binding!!

    private val viewModel: InfoViewModel by viewModels()

    private lateinit var infoAdapter: InfoListAdapter
    private lateinit var chipAdapter: NoticeCategoryChipAdapter

    private var isCustomTab = true
    private var selectedCategory = "전체"
    private var searchQuery = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupInfoList()
        setupCategoryChips()
        setupSearch()
        setupTabs()
        observeBookmarks()
    }

    private fun observeBookmarks() {
        viewModel.uiState.observe(viewLifecycleOwner) { applyFilter() }
    }

    private fun setupTabs() {
        binding.tabCustom.setOnClickListener { selectTab(custom = true) }
        binding.tabAll.setOnClickListener { selectTab(custom = false) }
        selectTab(custom = isCustomTab)
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
            "전체", "공모전", "대외활동", "지원사업", "교육/강의", "부트캠프"
        )
        val initialIdx = categories.indexOf(selectedCategory).coerceAtLeast(0)
        chipAdapter = NoticeCategoryChipAdapter(categories, initialIdx) { selected ->
            selectedCategory = selected
            applyFilter()
        }
        binding.rvCategories.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvCategories.adapter = chipAdapter
    }

    private fun setupSearch() {
        if (searchQuery.isNotEmpty()) binding.etSearch.setText(searchQuery)
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                searchQuery = s?.toString().orEmpty().trim()
                applyFilter()
            }
        })
    }

    private fun setupInfoList() {
        infoAdapter = InfoListAdapter(
            onItemClick = { info -> openDetail(info) },
            onBookmarkClick = { info -> viewModel.toggleBookmarkForInfo(info) }
        )
        binding.rvInfo.layoutManager = LinearLayoutManager(requireContext())
        binding.rvInfo.adapter = infoAdapter
    }

    private fun openDetail(info: Info) {
        val args = bundleOf(
            "infoId" to info.id,
            "infoCategory" to info.category,
            "infoTitle" to info.title,
            "infoTeam" to info.team,
            "infoDday" to info.dday,
            "infoStartDate" to (info.startDate ?: ""),
            "infoEndDate" to (info.endDate ?: "")
        )
        findNavController().navigate(R.id.action_info_to_infoDetail, args)
    }

    private fun applyFilter() {
        val bookmarkedIds = viewModel.uiState.value?.bookmarkedInfoIds ?: emptySet()
        val source = (if (isCustomTab) buildCustomInfos() else buildAllInfos())
            .map { it.copy(isBookmarked = it.id in bookmarkedIds) }
        val filtered = source
            .filter { matchCategory(it.category, selectedCategory) }
            .filter { searchQuery.isEmpty() || it.title.contains(searchQuery, ignoreCase = true) }
        infoAdapter.submitList(filtered)
        binding.tvTotalCount.text = getString(R.string.info_total_count_format, filtered.size)
    }

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
    private fun buildCustomInfos(): List<Info> = listOf(
        Info("ic1", "2026 AI 기반 서비스 아이디어 공모전",
            "부트캠프", "과학기술정보통신부", isGroup = true, dday = 22),
        Info("ic2", "2026 AI 아이디어 공모전 참가자 모집",
            "공모전", "과학기술정보통신부", isGroup = true, dday = 35),
        Info("ic3", "SQL 데이터베이스 입문 무료 특강 안내",
            "교육/강의", "인프런", isGroup = false, dday = 45),
        Info("ic4", "청년 월세 지원사업 신청 안내",
            "지원사업", "국토교통부", isGroup = false, dday = 50),
        Info("ic5", "클라우드 기반 백엔드 개발 실무 과정 안내",
            "교육/강의", "AWS Educate", isGroup = false, dday = 88),
        Info("ic6", "2026 AI 기반 서비스 아이디어 공모전",
            "부트캠프", "과학기술정보통신부", isGroup = true, dday = 22),
        Info("ic7", "2026 AI 아이디어 공모전 참가자 모집",
            "공모전", "과학기술정보통신부", isGroup = true, dday = 35),
        Info("ic8", "SQL 데이터베이스 입문 무료 특강 안내",
            "교육/강의", "인프런", isGroup = false, dday = 45),
        Info("ic9", "청년 월세 지원사업 신청 안내",
            "지원사업", "국토교통부", isGroup = false, dday = 50),
        Info("ic10", "클라우드 기반 백엔드 개발 실무 과정 안내",
            "교육/강의", "AWS Educate", isGroup = false, dday = 88),
    )

    // TODO: API 연결 시 ViewModel.uiState로 교체
    private fun buildAllInfos(): List<Info> = listOf(
        Info("ia1", "2026 AI 기반 서비스 아이디어 공모전",
            "부트캠프", "과학기술정보통신부", isGroup = true, dday = 22),
        Info("ia2", "2026 AI 아이디어 공모전 참가자 모집",
            "공모전", "과학기술정보통신부", isGroup = true, dday = 35),
        Info("ia3", "SQL 데이터베이스 입문 무료 특강 안내",
            "교육/강의", "인프런", isGroup = false, dday = 45),
        Info("ia4", "청년 월세 지원사업 신청 안내",
            "지원사업", "국토교통부", isGroup = false, dday = 50),
        Info("ia5", "클라우드 기반 백엔드 개발 실무 과정 안내",
            "교육/강의", "AWS Educate", isGroup = false, dday = 88),
        Info("ia6", "2026 청년 창업 부트캠프 모집",
            "부트캠프", "중소벤처기업부", isGroup = true, dday = 12),
        Info("ia7", "2026 디자인씽킹 대외활동 참가자 모집",
            "대외활동", "한국디자인진흥원", isGroup = true, dday = 28),
        Info("ia8", "2026 스타트업 지원사업 안내",
            "지원사업", "창업진흥원", isGroup = false, dday = 60),
        Info("ia9", "데이터 분석 입문 무료 특강",
            "교육/강의", "패스트캠퍼스", isGroup = false, dday = 15),
        Info("ia10", "AI 서비스 기획 공모전",
            "공모전", "한국지능정보사회진흥원", isGroup = true, dday = 40),
    )

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
