package com.ultimatejw.mjcn.ui.main.home

import android.content.res.ColorStateList
import android.graphics.Color
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
import com.ultimatejw.mjcn.databinding.ItemHomeThemeBinding
import com.ultimatejw.mjcn.domain.model.Notice
import com.ultimatejw.mjcn.domain.model.Theme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()

    private val todayClassAdapter = TodayClassAdapter()
    private val noticeAdapter = HomeNoticeAdapter(
        onItemClick = { notice -> openNoticeDetail(notice) },
        onBookmarkClick = { notice -> viewModel.toggleNoticeBookmark(notice) }
    )
    private val infoAdapter = HomeInfoAdapter(
        onBookmarkClick = { info -> viewModel.toggleInfoBookmark(info) }
    )

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
    }

    private fun bindThemeItems(themes: List<Theme>) {
        val container = binding.llThemeItems
        container.removeAllViews()
        val inflater = LayoutInflater.from(requireContext())
        themes.forEach { theme ->
            val itemBinding = ItemHomeThemeBinding.inflate(inflater, container, false)
            itemBinding.tvTitle.text = theme.title
            itemBinding.tvSubtitle.text = theme.subtitle
            itemBinding.ivIcon.setImageResource(theme.iconRes)
            itemBinding.layoutThemeIcon.backgroundTintList =
                ColorStateList.valueOf(Color.parseColor(theme.iconBgColor))
            itemBinding.root.setOnClickListener {
                findNavController().navigate(
                    R.id.action_home_to_chatDetail,
                    bundleOf("sessionId" to "")
                )
            }
            container.addView(itemBinding.root)
        }
    }

    private fun setupNavigation() {
        binding.ivProfile.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_settings)
        }

        binding.btnCheckNotice.setOnClickListener {
            openNoticeDetailWithDummy("c1")
        }
        binding.btnAskAi.setOnClickListener {
            findNavController().navigate(
                R.id.action_home_to_chatDetail,
                bundleOf("sessionId" to "")
            )
        }

        binding.tvNoticeShowall.setOnClickListener {
            requireActivity().findViewById<BottomNavigationView>(R.id.bottom_nav)
                ?.let { it.selectedItemId = R.id.noticeFragment }
        }

        binding.tvInfoShowall.setOnClickListener {
            requireActivity().findViewById<BottomNavigationView>(R.id.bottom_nav)
                ?.let { it.selectedItemId = R.id.infoFragment }
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
            val displayName = state.dashboardUserName.ifBlank { user?.name ?: "" }
            binding.tvTitle.text = if (displayName.isNotBlank()) "안녕하세요, ${displayName}님" else "안녕하세요!"
            binding.tvSubtitle.text = if (user != null && !user.major.isNullOrBlank()) {
                val year = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
                "${user.major} ${user.grade}학년 · ${year}학년도 ${user.semester}학기"
            } else ""

            todayClassAdapter.submitList(state.todayClasses)
            val todayEmpty = state.todayClasses.isEmpty()
            binding.tvTodayClassEmpty.visibility = if (todayEmpty) View.VISIBLE else View.GONE
            binding.rvTodayClass.visibility = if (todayEmpty) View.GONE else View.VISIBLE

            noticeAdapter.submitList(state.noticeList)
            val noticeEmpty = state.noticeList.isEmpty()
            binding.tvNoticeEmpty.visibility = if (noticeEmpty) View.VISIBLE else View.GONE
            binding.rvNotice.visibility = if (noticeEmpty) View.GONE else View.VISIBLE

            infoAdapter.submitList(state.infoList)
            val infoEmpty = state.infoList.isEmpty()
            binding.tvInfoEmpty.visibility = if (infoEmpty) View.VISIBLE else View.GONE
            binding.rvInfo.visibility = if (infoEmpty) View.GONE else View.VISIBLE

            bindThemeItems(state.themeList)

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
