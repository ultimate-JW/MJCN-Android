package com.ultimatejw.mjcn.ui.main.notice

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ultimatejw.mjcn.R
import com.ultimatejw.mjcn.databinding.FragmentNoticeBookmarkBinding
import com.ultimatejw.mjcn.domain.model.Notice
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NoticeBookmarkFragment : Fragment() {

    private var _binding: FragmentNoticeBookmarkBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NoticeBookmarkViewModel by viewModels()
    private lateinit var noticeAdapter: NoticeListAdapter
    private lateinit var chipAdapter: NoticeCategoryChipAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNoticeBookmarkBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupList()
        setupChips()
        observeViewModel()
    }

    private fun setupList() {
        noticeAdapter = NoticeListAdapter(
            onItemClick = { notice -> openDetail(notice) },
            onBookmarkClick = { notice -> viewModel.toggleBookmark(notice) }
        )
        binding.rvNotices.layoutManager = LinearLayoutManager(requireContext())
        binding.rvNotices.adapter = noticeAdapter
    }

    private fun setupChips() {
        val categories = listOf("전체", "일반", "학사", "공모전", "지원사업", "진로/취업/창업", "장학/학자금")
        chipAdapter = NoticeCategoryChipAdapter(categories) { selected ->
            viewModel.selectCategory(selected)
        }
        binding.rvCategories.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvCategories.adapter = chipAdapter
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            noticeAdapter.submitList(state.filtered)
            binding.tvTotalCount.text = "총 ${state.filtered.size}건"
        }
    }

    private fun openDetail(notice: Notice) {
        val args = bundleOf(
            "noticeId" to notice.id,
            "noticeCategory" to notice.category,
            "noticeTitle" to notice.title,
            "noticeTeam" to notice.team,
            "noticeDate" to notice.date,
            "noticeSummary" to ""
        )
        findNavController().navigate(R.id.action_noticeBookmark_to_noticeDetail, args)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
