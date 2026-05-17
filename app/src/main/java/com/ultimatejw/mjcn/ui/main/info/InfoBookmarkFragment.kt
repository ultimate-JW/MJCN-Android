package com.ultimatejw.mjcn.ui.main.info

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.ultimatejw.mjcn.databinding.FragmentInfoBookmarkBinding
import com.ultimatejw.mjcn.ui.main.notice.NoticeCategoryChipAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class InfoBookmarkFragment : Fragment() {

    private var _binding: FragmentInfoBookmarkBinding? = null
    private val binding get() = _binding!!

    private val viewModel: InfoBookmarkViewModel by viewModels()
    private lateinit var infoAdapter: InfoListAdapter
    private lateinit var chipAdapter: NoticeCategoryChipAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInfoBookmarkBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupList()
        setupChips()
        observeViewModel()
    }

    private fun setupList() {
        infoAdapter = InfoListAdapter(onBookmarkClick = { info -> viewModel.toggleBookmark(info) })
        binding.rvInfo.layoutManager = LinearLayoutManager(requireContext())
        binding.rvInfo.adapter = infoAdapter
    }

    private fun setupChips() {
        val categories = listOf("전체", "공모전", "부트캠프", "지원사업", "교육/강의", "대외활동")
        chipAdapter = NoticeCategoryChipAdapter(categories) { selected ->
            viewModel.selectCategory(selected)
        }
        binding.rvCategories.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvCategories.adapter = chipAdapter
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            infoAdapter.submitList(state.filtered)
            binding.tvTotalCount.text = "총 ${state.filtered.size}건"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
