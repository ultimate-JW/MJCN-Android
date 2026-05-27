package com.ultimatejw.mjcn.ui.main.notice

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
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
        setupScrollListener()
        observeState()
    }

    private fun observeState() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            noticeAdapter.submitList(state.notices)
            binding.tvTotalCount.text =
                getString(R.string.notice_total_count_format, state.totalCount)
            binding.progressLoadMore.visibility =
                if (state.isLoadingMore) View.VISIBLE else View.GONE
        }
    }

    private fun setupTabs() {
        binding.tabCustom.setOnClickListener { selectTab(custom = true) }
        binding.tabAll.setOnClickListener { selectTab(custom = false) }
        selectTab(custom = true)
    }

    private fun selectTab(custom: Boolean) {
        val primary = requireContext().getColor(R.color.point_color1)
        val muted = requireContext().getColor(R.color.font_color2)

        binding.tvTabCustom.setTextColor(if (custom) primary else muted)
        binding.tvTabAll.setTextColor(if (custom) muted else primary)
        binding.tvTabCustom.typeface = androidx.core.content.res.ResourcesCompat.getFont(
            requireContext(), if (custom) R.font.pretendard_bold else R.font.pretendard_medium
        )
        binding.tvTabAll.typeface = androidx.core.content.res.ResourcesCompat.getFont(
            requireContext(), if (custom) R.font.pretendard_medium else R.font.pretendard_bold
        )
        binding.indicatorCustom.visibility = if (custom) View.VISIBLE else View.INVISIBLE
        binding.indicatorAll.visibility    = if (custom) View.INVISIBLE else View.VISIBLE

        viewModel.selectTab(isCustom = custom)
    }

    private fun setupCategoryChips() {
        val categories = listOf(
            "전체", "일반", "학사", "공모전/대외활동",
            "장학/학자금", "취업", "해외", "지원사업"
        )
        chipAdapter = NoticeCategoryChipAdapter(categories, 0) { selected ->
            viewModel.selectCategory(selected)
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
                viewModel.onSearchQuery(s?.toString().orEmpty().trim())
            }
        })
    }

    private fun setupNoticeList() {
        noticeAdapter = NoticeListAdapter(
            onItemClick = { notice -> openDetail(notice) },
            onBookmarkClick = { notice -> viewModel.toggleBookmarkForNotice(notice) }
        )
        binding.rvNotices.layoutManager = LinearLayoutManager(requireContext())
        binding.rvNotices.adapter = noticeAdapter
    }

    private fun setupScrollListener() {
        binding.nestedScrollView.setOnScrollChangeListener(
            NestedScrollView.OnScrollChangeListener { v, _, scrollY, _, _ ->
                val child = v.getChildAt(0) ?: return@OnScrollChangeListener
                if (scrollY >= child.measuredHeight - v.measuredHeight - 200) {
                    viewModel.loadMore()
                }
            }
        )
    }

    private fun openDetail(notice: Notice) {
        val args = bundleOf(
            "noticeId"       to notice.id,
            "noticeCategory" to notice.category,
            "noticeTitle"    to notice.title,
            "noticeTeam"     to notice.team,
            "noticeDate"     to notice.date,
            "noticeSummary"  to notice.summary
        )
        findNavController().navigate(R.id.action_notice_to_detail, args)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
