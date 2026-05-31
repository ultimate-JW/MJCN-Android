package com.ultimatejw.mjcn.ui.main.info

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.ultimatejw.mjcn.R
import com.ultimatejw.mjcn.databinding.FragmentInfoDetailBinding
import com.ultimatejw.mjcn.domain.model.Info
import com.ultimatejw.mjcn.domain.model.InfoCategory
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class InfoDetailFragment : Fragment() {

    private var _binding: FragmentInfoDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: InfoDetailViewModel by viewModels()
    private lateinit var argInfo: Info

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInfoDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        argInfo = Info(
            id = arguments?.getString("infoId").orEmpty(),
            category = arguments?.getString("infoCategory").orEmpty(),
            title = arguments?.getString("infoTitle").orEmpty(),
            team = arguments?.getString("infoTeam").orEmpty(),
            isGroup = false,
            dday = arguments?.getInt("infoDday") ?: 0,
            startDate = arguments?.getString("infoStartDate"),
            endDate = arguments?.getString("infoEndDate")
        )
        applyInfo(argInfo)

        viewModel.info.observe(viewLifecycleOwner) { apiInfo ->
            applyInfo(apiInfo.mergeWith(argInfo))
        }
    }

    private fun Info.mergeWith(fallback: Info) = copy(
        category  = category.ifBlank { fallback.category },
        title     = title.ifBlank { fallback.title },
        team      = team.ifBlank { fallback.team },
        url       = url.ifBlank { fallback.url },
        dday      = if (dday == 0) fallback.dday else dday,
        startDate = startDate ?: fallback.startDate,
        endDate   = endDate ?: fallback.endDate
    )

    private var currentUrl: String = ""

    private fun applyInfo(info: Info) {
        binding.tvCategory.text = info.category
        binding.tvTitle.text = info.title
        binding.tvTeam.text = info.team
        binding.tvDateRange.text = when {
            info.startDate != null && info.endDate != null -> "${info.startDate} ~ ${info.endDate}"
            info.startDate != null -> info.startDate
            info.endDate != null -> "~ ${info.endDate}"
            else -> ""
        }
        binding.tvDday.text = if (info.dday == 0) "D-Day" else "D-${info.dday}"
        applyCategoryChip(info.category)
        if (info.url.isNotBlank()) {
            currentUrl = info.url
            binding.btnOriginalLink.visibility = View.VISIBLE
            binding.btnOriginalLink.setOnClickListener {
                val intent = android.content.Intent(
                    android.content.Intent.ACTION_VIEW,
                    android.net.Uri.parse(currentUrl)
                )
                startActivity(intent)
            }
        } else {
            binding.btnOriginalLink.visibility = View.GONE
        }
    }

    private fun applyCategoryChip(categoryLabel: String) {
        val context = binding.root.context
        val category = InfoCategory.from(categoryLabel)
        val bgColorRes = when (category) {
            InfoCategory.BOOTCAMP  -> R.color.category_academic_bg
            InfoCategory.CONTEST   -> R.color.category_contest_bg
            InfoCategory.SUPPORT   -> R.color.category_scholarship_bg
            InfoCategory.ACTIVITY  -> R.color.category_activity_bg
            InfoCategory.EDUCATION -> R.color.category_career_bg
        }
        val textColorRes = when (category) {
            InfoCategory.BOOTCAMP  -> R.color.category_academic_text
            InfoCategory.CONTEST   -> R.color.category_contest_text
            InfoCategory.SUPPORT   -> R.color.category_scholarship_text
            InfoCategory.ACTIVITY  -> R.color.category_activity_text
            InfoCategory.EDUCATION -> R.color.category_career_text
        }
        val radius = context.resources.displayMetrics.density * 22
        binding.layoutCategoryChip.background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(ContextCompat.getColor(context, bgColorRes))
            cornerRadius = radius
        }
        binding.tvCategory.setTextColor(ContextCompat.getColor(context, textColorRes))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
