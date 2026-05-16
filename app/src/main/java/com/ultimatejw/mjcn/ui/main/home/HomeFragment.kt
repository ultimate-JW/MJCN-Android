package com.ultimatejw.mjcn.ui.main.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.ultimatejw.mjcn.R
import com.ultimatejw.mjcn.databinding.FragmentHomeBinding
import com.ultimatejw.mjcn.domain.model.Notice
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()

    private val todayClassAdapter = TodayClassAdapter()
    private val noticeAdapter = HomeNoticeAdapter { notice -> openNoticeDetail(notice) }
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
        setupNavigation()
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

    private fun setupNavigation() {
        // AI 가이드 카드의 두 버튼
        binding.btnCheckNotice.setOnClickListener {
            // 더미: 첫 번째 공지 mockup 으로 이동
            openNoticeDetailWithDummy("c1")
        }
        binding.btnAskAi.setOnClickListener {
            findNavController().navigate(
                R.id.action_home_to_chatDetail,
                bundleOf("sessionId" to "")
            )
        }

        // 공지/테마 더보기는 BottomNav 의 탭 전환과 동일한 동작이 되도록
        // selectedItemId 를 직접 변경해서 NavigationUI 가 popUpTo/launchSingleTop 처리하도록 위임.
        // 그래야 다른 탭에서 홈 아이콘을 눌렀을 때도 일관되게 홈으로 복귀한다.
        binding.tvNoticeShowall.setOnClickListener {
            requireActivity().findViewById<BottomNavigationView>(R.id.bottom_nav)
                ?.let { it.selectedItemId = R.id.noticeFragment }
        }

        binding.tvThemeShowall.setOnClickListener {
            requireActivity().findViewById<BottomNavigationView>(R.id.bottom_nav)
                ?.let { it.selectedItemId = R.id.themeFragment }
        }
    }

    private fun openNoticeDetail(notice: Notice) {
        val args = bundleOf(
            "noticeId" to notice.id,
            "noticeCategory" to notice.category,
            "noticeTitle" to notice.title,
            "noticeTeam" to notice.team,
            "noticeDate" to notice.date,
            "noticeSummary" to ""
        )
        findNavController().navigate(R.id.action_home_to_noticeDetail, args)
    }

    private fun openNoticeDetailWithDummy(mockupId: String) {
        val args = bundleOf(
            "noticeId" to mockupId,
            "noticeCategory" to "",
            "noticeTitle" to "",
            "noticeTeam" to "",
            "noticeDate" to "",
            "noticeSummary" to ""
        )
        findNavController().navigate(R.id.action_home_to_noticeDetail, args)
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
