package com.ultimatejw.mjcn.ui.main.info

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
import androidx.core.content.ContextCompat
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
        setupScrollListener()
        setupSwipeRefresh()
        observeState()
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setColorSchemeColors(
            ContextCompat.getColor(requireContext(), R.color.primary)
        )
        binding.swipeRefresh.setOnRefreshListener { viewModel.refresh() }
    }

    private fun observeState() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            infoAdapter.submitList(state.infos)
            binding.tvTotalCount.text =
                getString(R.string.info_total_count_format, state.totalCount)
            binding.progressLoadMore.visibility =
                if (state.isLoadingMore) View.VISIBLE else View.GONE
            if (!state.isLoading) binding.swipeRefresh.isRefreshing = false
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
            "전체", "공모전", "대외활동", "지원사업", "교육/강의", "부트캠프"
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

    private fun setupInfoList() {
        infoAdapter = InfoListAdapter(
            onItemClick = { info -> openDetail(info) },
            onBookmarkClick = { info -> viewModel.toggleBookmarkForInfo(info) }
        )
        binding.rvInfo.layoutManager = LinearLayoutManager(requireContext())
        binding.rvInfo.adapter = infoAdapter
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

    private fun openDetail(info: Info) {
        val args = bundleOf(
            "infoId"        to info.id,
            "infoCategory"  to info.category,
            "infoTitle"     to info.title,
            "infoTeam"      to info.team,
            "infoDday"      to info.dday,
            "infoStartDate" to (info.startDate ?: ""),
            "infoEndDate"   to (info.endDate ?: "")
        )
        findNavController().navigate(R.id.action_info_to_infoDetail, args)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
