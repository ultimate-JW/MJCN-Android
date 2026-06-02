package com.ultimatejw.mjcn.ui.main.theme

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.ultimatejw.mjcn.R
import com.ultimatejw.mjcn.databinding.FragmentThemeDetailBinding
import com.ultimatejw.mjcn.domain.model.CourseRecommendSections
import com.ultimatejw.mjcn.domain.model.ExchangeGuide
import com.ultimatejw.mjcn.domain.model.ExchangeNecessityItem
import com.ultimatejw.mjcn.domain.model.ExchangeEvaluationItem
import com.ultimatejw.mjcn.domain.model.ExchangeRecommendItem
import com.ultimatejw.mjcn.domain.model.QuickQuestion
import com.ultimatejw.mjcn.domain.model.RecommendCourse
import com.ultimatejw.mjcn.domain.model.ThemeItem
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ThemeDetailFragment : Fragment() {

    private var _binding: FragmentThemeDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ThemeDetailViewModel by viewModels()
    private var questionBar: ThemeQuestionBarController? = null

    private val navigateToChat: (String) -> Unit = { message ->
        val nav = findNavController()
        nav.popBackStack(R.id.themeDetailFragment, true)
        nav.navigate(R.id.chatFragment)
        nav.navigate(
            R.id.action_chat_to_detail,
            bundleOf("sessionId" to "", "initialMessage" to message)
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentThemeDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnBack.setOnClickListener { findNavController().popBackStack() }

        val themeId = arguments?.getInt("themeId") ?: 0
        val category = arguments?.getString("category") ?: ""
        val themeTitle = arguments?.getString("themeTitle") ?: ""
        viewModel.load(themeId, category, themeTitle)

        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            binding.progressLoading.visibility = if (state.isLoading) View.VISIBLE else View.GONE
            if (!state.isLoading && (state.title.isNotBlank() || state.adviceText.isNotBlank())) {
                renderWithApiData(state)
            }
        }

        questionBar = ThemeQuestionBarController(
            root = binding.root,
            inlineInput = binding.inlineInput,
            floatingInput = binding.floatingInput,
            cardInput = binding.cardInput,
            etMessage = binding.etMessage,
        ).also {
            it.onSendMessage = navigateToChat
            it.setup()
        }
    }

    private fun renderWithApiData(state: ThemeDetailUiState) {
        binding.tvHeaderTitle.text = state.title
        binding.tvAdvice.text = state.adviceText

        when (state.category) {
            "course_registration" -> {
                binding.layoutContentSection.visibility = View.GONE
                binding.layoutExchangeSection.visibility = View.GONE
                state.courseRecommend?.let { renderCourseSection(it) }
                updateQuickQuestions(state.quickQuestions)
            }
            "career" -> {
                binding.layoutContentSection.visibility = View.GONE
                binding.layoutCourseSection.visibility = View.GONE
                binding.layoutExchangeSection.visibility = View.GONE
                renderCareerSection(state.assessmentItems, state.contentItems)
                updateQuickQuestions(state.quickQuestions)
            }
            "exchange" -> {
                binding.layoutContentSection.visibility = View.GONE
                binding.layoutCourseSection.visibility = View.GONE
                state.exchangeGuide?.let { renderExchangeSection(it) }
                updateQuickQuestions(state.quickQuestions)
            }
            else -> {
                binding.layoutCourseSection.visibility = View.GONE
                binding.layoutExchangeSection.visibility = View.GONE
                if (state.contentItems.isEmpty()) {
                    binding.layoutContentSection.visibility = View.GONE
                } else {
                    binding.layoutContentSection.visibility = View.VISIBLE
                    renderItemCards(binding.containerContent, state.contentItems)
                }
                updateLinkButtons(state.linkItems)
            }
        }
    }

    // ── 수강신청 섹션 ──────────────────────────────────────────────

    private fun renderCourseSection(data: CourseRecommendSections) {
        binding.layoutCourseSection.visibility = View.VISIBLE
        renderCourseList(binding.containerInterestCourses, data.interestCourses)
        renderCourseList(binding.containerLinkedCourses, data.linkedCourses)
    }

    private fun renderCourseList(container: LinearLayout, courses: List<RecommendCourse>) {
        container.removeAllViews()
        val gap = (8 * resources.displayMetrics.density).toInt()
        courses.forEachIndexed { index, course ->
            val cardView = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_recommend_course, container, false)

            cardView.findViewById<TextView>(R.id.tv_course_name).text = course.name
            cardView.findViewById<TextView>(R.id.tv_course_meta).text =
                "${course.category} · ${course.credits}학점"

            val previewText = course.offerings.firstOrNull()
                ?.let { "${it.professor} · ${it.scheduleText}" } ?: ""
            val tvPreview = cardView.findViewById<TextView>(R.id.tv_course_preview)
            tvPreview.text = previewText

            val expandChip = cardView.findViewById<TextView>(R.id.tv_expand_chip)
            expandChip.text = "${course.offerings.size}분반 ∨"

            val offeringsContainer =
                cardView.findViewById<LinearLayout>(R.id.container_offerings)

            course.offerings.forEach { offering ->
                val offeringView = LayoutInflater.from(requireContext())
                    .inflate(R.layout.item_recommend_offering, offeringsContainer, false)
                offeringView.findViewById<TextView>(R.id.tv_offering_info).text =
                    "${offering.sectionNo} · ${offering.professor}"
                offeringView.findViewById<TextView>(R.id.tv_offering_schedule).text =
                    offering.scheduleText
                val toggleBtn = offeringView.findViewById<ImageView>(R.id.iv_offering_toggle)
                offeringView.setOnClickListener {
                    toggleBtn.isSelected = !toggleBtn.isSelected
                }
                offeringsContainer.addView(offeringView)
            }

            cardView.findViewById<View>(R.id.layout_course_header).setOnClickListener {
                val expanded = offeringsContainer.visibility == View.VISIBLE
                offeringsContainer.visibility = if (expanded) View.GONE else View.VISIBLE
                tvPreview.visibility = if (expanded) View.VISIBLE else View.GONE
                expandChip.text = if (expanded) "${course.offerings.size}분반 ∨" else "접기 ∧"
            }

            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            lp.bottomMargin = if (index == courses.lastIndex) 0 else gap
            cardView.layoutParams = lp
            container.addView(cardView)
        }
    }

    // ── 취업 로드맵 섹션 ──────────────────────────────────────────

    private fun renderCareerSection(assessments: List<ThemeItem>, roadmapSteps: List<ThemeItem>) {
        binding.layoutCareerSection.visibility = View.VISIBLE
        val inflater = LayoutInflater.from(requireContext())
        val gap = (8 * resources.displayMetrics.density).toInt()

        binding.containerAssessment.removeAllViews()
        assessments.forEachIndexed { index, item ->
            val view = inflater.inflate(
                R.layout.item_theme_assessment, binding.containerAssessment, false
            ) as LinearLayout
            view.findViewById<TextView>(R.id.tv_assessment_title).text = item.title
            view.findViewById<TextView>(R.id.tv_assessment_subtitle).text = item.content
            val lp = view.layoutParams as LinearLayout.LayoutParams
            lp.topMargin = if (index == 0) 0 else gap
            view.layoutParams = lp
            binding.containerAssessment.addView(view)
        }

        renderItemCards(binding.containerRoadmap, roadmapSteps)
    }

    // ── 교환학생 섹션 ──────────────────────────────────────────────

    private fun renderExchangeSection(data: ExchangeGuide) {
        binding.layoutExchangeSection.visibility = View.VISIBLE
        renderNecessity(data.necessity)
        renderEvaluation(data.evaluation)
        renderCurrentRecommendation(data.currentRecommendation)
    }

    private fun renderNecessity(items: List<ExchangeNecessityItem>) {
        val container = binding.containerNecessity
        container.removeAllViews()
        val dp = resources.displayMetrics.density
        val primary = ContextCompat.getColor(requireContext(), R.color.primary)
        val font1 = ContextCompat.getColor(requireContext(), R.color.font_color1)

        items.forEachIndexed { index, item ->
            val headerRow = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }
            val tvOption = TextView(requireContext()).apply {
                text = "${item.icon} ${item.option}"
                textSize = 15f
                setTypeface(typeface, Typeface.BOLD)
                setTextColor(font1)
            }
            val tvStars = TextView(requireContext()).apply {
                text = scoreToStars(item.score)
                textSize = 15f
                setTextColor(primary)
            }
            headerRow.addView(
                tvOption,
                LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            )
            headerRow.addView(tvStars)
            container.addView(headerRow)

            val tvReason = TextView(requireContext()).apply {
                text = item.reason
                textSize = 13f
                setTextColor(Color.parseColor("#888888"))
                setPadding(0, (4 * dp).toInt(), 0, 0)
            }
            container.addView(tvReason)

            if (index < items.lastIndex) {
                val divider = View(requireContext())
                divider.setBackgroundColor(Color.parseColor("#F0F0F0"))
                val dlp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, (1 * dp).toInt()
                ).apply {
                    topMargin = (12 * dp).toInt()
                    bottomMargin = (12 * dp).toInt()
                }
                container.addView(divider, dlp)
            }
        }
    }

    private fun renderEvaluation(items: List<ExchangeEvaluationItem>) {
        val container = binding.containerEvaluation
        container.removeAllViews()
        val dp = resources.displayMetrics.density
        val primary = ContextCompat.getColor(requireContext(), R.color.primary)
        val font1 = ContextCompat.getColor(requireContext(), R.color.font_color1)

        items.forEachIndexed { index, item ->
            val tvHeader = TextView(requireContext()).apply {
                text = "${item.icon} ${item.option}"
                textSize = 15f
                setTypeface(typeface, Typeface.BOLD)
                setTextColor(font1)
            }
            container.addView(tvHeader)

            item.fits.forEach { fit ->
                val tvFit = TextView(requireContext()).apply {
                    text = "• $fit"
                    textSize = 13f
                    setTextColor(font1)
                    setPadding((4 * dp).toInt(), (3 * dp).toInt(), 0, 0)
                }
                container.addView(tvFit)
            }

            item.caveat?.let { caveat ->
                val tvCaveat = TextView(requireContext()).apply {
                    text = "※ $caveat"
                    textSize = 12f
                    setTextColor(Color.parseColor("#888888"))
                    setPadding(0, (4 * dp).toInt(), 0, 0)
                }
                container.addView(tvCaveat)
            }

            item.interestNote?.let { note ->
                val tvNote = TextView(requireContext()).apply {
                    text = "✓ $note"
                    textSize = 12f
                    setTextColor(primary)
                    setPadding(0, (2 * dp).toInt(), 0, 0)
                }
                container.addView(tvNote)
            }

            if (index < items.lastIndex) {
                val divider = View(requireContext())
                divider.setBackgroundColor(Color.parseColor("#F0F0F0"))
                val dlp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, (1 * dp).toInt()
                ).apply {
                    topMargin = (12 * dp).toInt()
                    bottomMargin = (12 * dp).toInt()
                }
                container.addView(divider, dlp)
            }
        }
    }

    private fun renderCurrentRecommendation(items: List<ExchangeRecommendItem>) {
        val container = binding.containerCurrentRecommendation
        container.removeAllViews()
        val dp = resources.displayMetrics.density
        val primary = ContextCompat.getColor(requireContext(), R.color.primary)
        val font1 = ContextCompat.getColor(requireContext(), R.color.font_color1)
        val badgeSize = (28 * dp).toInt()

        items.forEachIndexed { index, item ->
            val rowLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.TOP
            }

            val tvRank = TextView(requireContext()).apply {
                text = "${item.rank}"
                textSize = 12f
                setTextColor(Color.WHITE)
                gravity = Gravity.CENTER
                background = android.graphics.drawable.GradientDrawable().apply {
                    shape = android.graphics.drawable.GradientDrawable.OVAL
                    setColor(primary)
                }
                layoutParams = LinearLayout.LayoutParams(badgeSize, badgeSize)
            }

            val contentLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
            }
            val tvTitle = TextView(requireContext()).apply {
                text = item.title
                textSize = 14f
                setTypeface(typeface, Typeface.BOLD)
                setTextColor(font1)
            }
            val tvNote = TextView(requireContext()).apply {
                text = item.note
                textSize = 13f
                setTextColor(Color.parseColor("#888888"))
                setPadding(0, (2 * dp).toInt(), 0, 0)
            }
            contentLayout.addView(tvTitle)
            contentLayout.addView(tvNote)

            rowLayout.addView(tvRank)
            val clp = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            clp.marginStart = (10 * dp).toInt()
            rowLayout.addView(contentLayout, clp)

            val rlp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            rlp.bottomMargin = if (index < items.lastIndex) (12 * dp).toInt() else 0
            container.addView(rowLayout, rlp)
        }
    }

    // ── 공통 ────────────────────────────────────────────────────────

    private fun updateQuickQuestions(questions: List<QuickQuestion>) {
        if (questions.isEmpty()) {
            binding.layoutLinksSection.visibility = View.GONE
            return
        }
        binding.layoutLinksSection.visibility = View.VISIBLE

        fun setup(btnLayout: LinearLayout, question: QuickQuestion?) {
            if (question == null) { btnLayout.visibility = View.GONE; return }
            btnLayout.visibility = View.VISIBLE
            (btnLayout.getChildAt(0) as? TextView)?.text = question.label
            btnLayout.setOnClickListener { navigateToChat(question.prompt) }
        }
        setup(binding.btnQuick1, questions.getOrNull(0))
        setup(binding.btnQuick2, questions.getOrNull(1))
        setup(binding.btnQuick3, questions.getOrNull(2))
    }

    private fun updateLinkButtons(links: List<ThemeItem>) {
        if (links.isEmpty()) {
            binding.layoutLinksSection.visibility = View.GONE
            return
        }
        binding.layoutLinksSection.visibility = View.VISIBLE

        fun setup(btnLayout: LinearLayout, item: ThemeItem?) {
            if (item == null) { btnLayout.visibility = View.GONE; return }
            btnLayout.visibility = View.VISIBLE
            (btnLayout.getChildAt(0) as? TextView)?.text = item.title
            btnLayout.setOnClickListener {
                item.externalUrl?.let { url ->
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                }
            }
        }
        setup(binding.btnQuick1, links.getOrNull(0))
        setup(binding.btnQuick2, links.getOrNull(1))
        setup(binding.btnQuick3, links.getOrNull(2))
    }

    private fun renderItemCards(container: LinearLayout, items: List<ThemeItem>) {
        container.removeAllViews()
        val inflater = LayoutInflater.from(requireContext())
        val gap = (12 * resources.displayMetrics.density).toInt()
        val bg = ContextCompat.getDrawable(requireContext(), R.drawable.bg_theme_assessment_item)
        items.forEachIndexed { index, item ->
            val card = inflater.inflate(R.layout.item_keypoint_card, container, false) as LinearLayout
            card.background = bg?.constantState?.newDrawable()?.mutate()
            card.findViewById<TextView>(R.id.tv_kp_title).text = item.title
            card.findViewById<TextView>(R.id.tv_kp_body).text = item.content
            val lp = card.layoutParams as LinearLayout.LayoutParams
            lp.bottomMargin = if (index == items.lastIndex) 0 else gap
            card.layoutParams = lp
            container.addView(card)
        }
    }

    private fun scoreToStars(score: Int): String {
        val clamped = score.coerceIn(0, 5)
        return "★".repeat(clamped) + "☆".repeat(5 - clamped)
    }

    override fun onDestroyView() {
        questionBar?.cancel()
        questionBar = null
        super.onDestroyView()
        _binding = null
    }
}
