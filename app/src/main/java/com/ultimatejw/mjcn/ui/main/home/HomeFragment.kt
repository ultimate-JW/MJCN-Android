package com.ultimatejw.mjcn.ui.main.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.ultimatejw.mjcn.databinding.FragmentHomeBinding
import com.ultimatejw.mjcn.ui.main.theme.ThemeAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()

    private val todayClassAdapter = TodayClassAdapter()
    private val noticeAdapter = HomeNoticeAdapter()
    private val infoAdapter = HomeInfoAdapter()
    private val themeAdapter = HomeThemeAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        setupRecyclerViews()
        observeViewModel()
    }

    private fun setupRecyclerViews() {
        binding.rvTodayClass.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTodayClass.adapter = todayClassAdapter

        binding.rvNotice.layoutManager = LinearLayoutManager(requireContext())
        binding.rvNotice.adapter = noticeAdapter

        binding.rvInfo.layoutManager = LinearLayoutManager(requireContext())
        binding.rvInfo.adapter = infoAdapter

        binding.rvTheme.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTheme.adapter = themeAdapter
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            val user = state.currentUser
//            binding.tvTitle.text = if (user != null) "안녕하세요, ${user.name}님" else "안녕하세요!"
//            binding.tvSubtitle.text = if (user != null) {
//                "${user.department ?: ""} ${user.grade}학년 · ${user.entranceYear ?: ""}학년도 ${user.semester}학기"
//            } else ""

            binding.tvTitle.text = "안녕하세요, 김지현님"
            binding.tvSubtitle.text = "컴퓨터공학과 3학년 · 2026학년도 1학기"

            todayClassAdapter.submitList(state.todayClasses)
            noticeAdapter.submitList(state.noticeList)
            infoAdapter.submitList(state.infoList)
            themeAdapter.submitList(state.themeList)

            binding.tvNewNoti.text = state.courseCount.toString()
            binding.tvNewInfo.text = state.graduationCredits.toString()
            binding.tvJonggang.text = state.dday
            binding.tvGradu.text = state.gradProgress
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
