package com.ultimatejw.mjcn.ui.main.home

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.ultimatejw.mjcn.R
import com.ultimatejw.mjcn.databinding.FragmentHomeBinding
import com.ultimatejw.mjcn.databinding.ItemHomeInfoBinding
import com.ultimatejw.mjcn.databinding.ItemHomeNoticeBinding
import com.ultimatejw.mjcn.databinding.ItemHomeThemeBinding
import com.ultimatejw.mjcn.domain.model.Info
import com.ultimatejw.mjcn.domain.model.InfoCategory
import com.ultimatejw.mjcn.domain.model.Notice
import com.ultimatejw.mjcn.domain.model.NoticeCategory
import com.ultimatejw.mjcn.domain.model.Theme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()

    private val todayClassAdapter = TodayClassAdapter()

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
    }

    private fun bindNoticeItems(notices: List<Notice>) {
        val container = binding.llNoticeItems
        container.removeAllViews()
        val inflater = LayoutInflater.from(requireContext())
        val context = requireContext()
        val radius = context.resources.displayMetrics.density * 22
        notices.take(3).forEach { notice ->
            val itemBinding = ItemHomeNoticeBinding.inflate(inflater, container, false)
            itemBinding.tvCategory.text = notice.category
            itemBinding.tvTitle.text = notice.title
            itemBinding.tvDate.text = notice.date
            itemBinding.btnBookmark.setBackgroundResource(
                if (notice.isBookmarked) R.drawable.ic_bookmark_filled else R.drawable.ic_bookmark
            )
            itemBinding.root.setOnClickListener { openNoticeDetail(notice) }
            itemBinding.btnBookmark.setOnClickListener { viewModel.toggleNoticeBookmark(notice) }
            val category = NoticeCategory.from(notice.category)
            val bgColor = when (category) {
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
            val textColor = when (category) {
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
            itemBinding.layoutCategoryChip.background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                setColor(ContextCompat.getColor(context, bgColor))
                cornerRadius = radius
            }
            itemBinding.tvCategory.setTextColor(ContextCompat.getColor(context, textColor))
            container.addView(itemBinding.root)
        }
    }

    private fun bindInfoItems(infos: List<Info>) {
        val container = binding.llInfoItems
        container.removeAllViews()
        val inflater = LayoutInflater.from(requireContext())
        val context = requireContext()
        val radius = context.resources.displayMetrics.density * 22
        infos.take(3).forEach { info ->
            val itemBinding = ItemHomeInfoBinding.inflate(inflater, container, false)
            itemBinding.tvCategory.text = info.category
            itemBinding.tvTitle.text = info.title
            itemBinding.tvDday.text = "D-${info.dday}"
            itemBinding.tvGroup.text = if (info.isGroup) "팀/개인" else "개인"
            itemBinding.btnBookmark.setBackgroundResource(
                if (info.isBookmarked) R.drawable.ic_bookmark_filled else R.drawable.ic_bookmark
            )
            itemBinding.btnBookmark.setOnClickListener { viewModel.toggleInfoBookmark(info) }
            val category = InfoCategory.from(info.category)
            val bgColor = when (category) {
                InfoCategory.BOOTCAMP  -> R.color.category_academic_bg
                InfoCategory.CONTEST   -> R.color.category_contest_bg
                InfoCategory.SUPPORT   -> R.color.category_scholarship_bg
                InfoCategory.ACTIVITY  -> R.color.category_activity_bg
                InfoCategory.EDUCATION -> R.color.category_career_bg
            }
            val textColor = when (category) {
                InfoCategory.BOOTCAMP  -> R.color.category_academic_text
                InfoCategory.CONTEST   -> R.color.category_contest_text
                InfoCategory.SUPPORT   -> R.color.category_scholarship_text
                InfoCategory.ACTIVITY  -> R.color.category_activity_text
                InfoCategory.EDUCATION -> R.color.category_career_text
            }
            itemBinding.layoutCategoryChip.background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                setColor(ContextCompat.getColor(context, bgColor))
                cornerRadius = radius
            }
            itemBinding.tvCategory.setTextColor(ContextCompat.getColor(context, textColor))
            container.addView(itemBinding.root)
        }
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
            "noticeSummary" to notice.summary
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

            val noticeEmpty = state.noticeList.isEmpty()
            binding.tvNoticeEmpty.visibility = if (noticeEmpty) View.VISIBLE else View.GONE
            binding.llNoticeItems.visibility = if (noticeEmpty) View.GONE else View.VISIBLE
            bindNoticeItems(state.noticeList)

            val infoEmpty = state.infoList.isEmpty()
            binding.tvInfoEmpty.visibility = if (infoEmpty) View.VISIBLE else View.GONE
            binding.llInfoItems.visibility = if (infoEmpty) View.GONE else View.VISIBLE
            bindInfoItems(state.infoList)

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
