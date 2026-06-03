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
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.ultimatejw.mjcn.R
import com.ultimatejw.mjcn.databinding.FragmentThemeDetailBinding
import com.ultimatejw.mjcn.domain.model.ContestCard
import com.ultimatejw.mjcn.domain.model.ContestGuide
import com.ultimatejw.mjcn.domain.model.CourseRecommendSections
import com.ultimatejw.mjcn.domain.model.ExchangeGuide
import com.ultimatejw.mjcn.domain.model.ExchangeNecessityItem
import com.ultimatejw.mjcn.domain.model.ExchangeEvaluationItem
import com.ultimatejw.mjcn.domain.model.ExchangeRecommendItem
import com.ultimatejw.mjcn.domain.model.QuickQuestion
import com.ultimatejw.mjcn.domain.model.RecommendCourse
import com.ultimatejw.mjcn.domain.model.StudyTips
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
        nav.navigate(
            R.id.chatFragment,
            null,
            NavOptions.Builder()
                .setPopUpTo(R.id.homeFragment, false)
                .setLaunchSingleTop(true)
                .build()
        )
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
            "exchange" -> {
                binding.layoutContentSection.visibility = View.GONE
                binding.layoutCourseSection.visibility = View.GONE
                state.exchangeGuide?.let { renderExchangeSection(it) }
                updateQuickQuestions(state.quickQuestions)
            }
            "contest", "grant" -> {
                binding.layoutContentSection.visibility = View.GONE
                binding.layoutCourseSection.visibility = View.GONE
                binding.layoutExchangeSection.visibility = View.GONE
                binding.layoutAcademicSection.visibility = View.GONE
                state.contestGuide?.let { renderContestSection(it) }
                updateQuickQuestions(state.quickQuestions)
            }
            "academic" -> {
                binding.layoutContentSection.visibility = View.GONE
                binding.layoutCourseSection.visibility = View.GONE
                binding.layoutExchangeSection.visibility = View.GONE
                binding.layoutContestSection.visibility = View.GONE
                state.studyTips?.let { renderAcademicSection(it) }
                updateQuickQuestions(state.quickQuestions)
            }
            else -> {
                binding.layoutCourseSection.visibility = View.GONE
                binding.layoutExchangeSection.visibility = View.GONE
                binding.layoutCareerSection.visibility = View.GONE
                binding.layoutContestSection.visibility = View.GONE
                binding.layoutAcademicSection.visibility = View.GONE
                if (state.contentItems.isEmpty()) {
                    binding.layoutContentSection.visibility = View.GONE
                } else {
                    binding.layoutContentSection.visibility = View.VISIBLE
                    renderGenericItems(binding.containerContent, state.contentItems)
                }
                updateQuickQuestions(state.quickQuestions)
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
        val ctx = requireContext()
        val primary = ContextCompat.getColor(ctx, R.color.primary)
        val font1 = ContextCompat.getColor(ctx, R.color.font_color1)
        val boldFont = ResourcesCompat.getFont(ctx, R.font.pretendard_bold)
        val regularFont = ResourcesCompat.getFont(ctx, R.font.pretendard_regular)

        val gap = (8 * dp).toInt()
        items.forEachIndexed { index, item ->
            val itemCard = LinearLayout(ctx).apply {
                orientation = LinearLayout.VERTICAL
                background = ContextCompat.getDrawable(ctx, R.drawable.bg_keypoint_inner_card)
                setPadding((18 * dp).toInt(), (16 * dp).toInt(), (18 * dp).toInt(), (16 * dp).toInt())
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = if (index == items.lastIndex) 0 else gap }
            }

            val headerRow = LinearLayout(ctx).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }
            val tvOption = TextView(ctx).apply {
                text = "${item.icon} ${item.option}"
                textSize = 15f
                typeface = boldFont
                setTextColor(font1)
            }
            val tvStars = TextView(ctx).apply {
                text = scoreToStars(item.score)
                textSize = 15f
                typeface = boldFont
                setTextColor(primary)
            }
            headerRow.addView(tvOption, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
            headerRow.addView(tvStars)
            itemCard.addView(headerRow)

            val tvReason = TextView(ctx).apply {
                text = item.reason
                textSize = 14f
                typeface = regularFont
                setTextColor(Color.parseColor("#888888"))
                setLineSpacing(4 * dp, 1f)
                setPadding(0, (6 * dp).toInt(), 0, 0)
            }
            itemCard.addView(tvReason)
            container.addView(itemCard)
        }
    }

    private fun renderEvaluation(items: List<ExchangeEvaluationItem>) {
        val container = binding.containerEvaluation
        container.removeAllViews()
        val dp = resources.displayMetrics.density
        val ctx = requireContext()
        val primary = ContextCompat.getColor(ctx, R.color.primary)
        val font1 = ContextCompat.getColor(ctx, R.color.font_color1)
        val boldFont = ResourcesCompat.getFont(ctx, R.font.pretendard_bold)
        val regularFont = ResourcesCompat.getFont(ctx, R.font.pretendard_regular)

        val gap = (8 * dp).toInt()
        items.forEachIndexed { index, item ->
            val itemCard = LinearLayout(ctx).apply {
                orientation = LinearLayout.VERTICAL
                background = ContextCompat.getDrawable(ctx, R.drawable.bg_keypoint_inner_card)
                setPadding((18 * dp).toInt(), (16 * dp).toInt(), (18 * dp).toInt(), (16 * dp).toInt())
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = if (index == items.lastIndex) 0 else gap }
            }

            val tvHeader = TextView(ctx).apply {
                text = "${item.icon} ${item.option}"
                textSize = 15f
                typeface = boldFont
                setTextColor(font1)
            }
            itemCard.addView(tvHeader)

            item.fits.forEach { fit ->
                val tvFit = TextView(ctx).apply {
                    text = "• $fit"
                    textSize = 14f
                    typeface = regularFont
                    setTextColor(font1)
                    setLineSpacing(4 * dp, 1f)
                    setPadding((4 * dp).toInt(), (4 * dp).toInt(), 0, 0)
                }
                itemCard.addView(tvFit)
            }

            item.caveat?.let { caveat ->
                val tvCaveat = TextView(ctx).apply {
                    text = "※ $caveat"
                    textSize = 13f
                    typeface = regularFont
                    setTextColor(Color.parseColor("#888888"))
                    setPadding(0, (4 * dp).toInt(), 0, 0)
                }
                itemCard.addView(tvCaveat)
            }

            item.interestNote?.let { note ->
                val tvNote = TextView(ctx).apply {
                    text = "✓ $note"
                    textSize = 13f
                    typeface = regularFont
                    setTextColor(primary)
                    setPadding(0, (2 * dp).toInt(), 0, 0)
                }
                itemCard.addView(tvNote)
            }
            container.addView(itemCard)
        }
    }

    private fun renderCurrentRecommendation(items: List<ExchangeRecommendItem>) {
        val container = binding.containerCurrentRecommendation
        container.removeAllViews()
        val dp = resources.displayMetrics.density
        val ctx = requireContext()
        val primary = ContextCompat.getColor(ctx, R.color.primary)
        val font1 = ContextCompat.getColor(ctx, R.color.font_color1)
        val boldFont = ResourcesCompat.getFont(ctx, R.font.pretendard_bold)
        val semiBoldFont = ResourcesCompat.getFont(ctx, R.font.pretendard_semibold)
        val regularFont = ResourcesCompat.getFont(ctx, R.font.pretendard_regular)
        val badgeSize = (28 * dp).toInt()

        val gap = (8 * dp).toInt()
        items.forEachIndexed { index, item ->
            val itemCard = LinearLayout(ctx).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.TOP
                background = ContextCompat.getDrawable(ctx, R.drawable.bg_keypoint_inner_card)
                setPadding((18 * dp).toInt(), (16 * dp).toInt(), (18 * dp).toInt(), (16 * dp).toInt())
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = if (index == items.lastIndex) 0 else gap }
            }

            val tvRank = TextView(ctx).apply {
                text = "${item.rank}"
                textSize = 12f
                typeface = semiBoldFont
                setTextColor(Color.WHITE)
                gravity = Gravity.CENTER
                background = android.graphics.drawable.GradientDrawable().apply {
                    shape = android.graphics.drawable.GradientDrawable.OVAL
                    setColor(primary)
                }
                layoutParams = LinearLayout.LayoutParams(badgeSize, badgeSize)
            }

            val contentLayout = LinearLayout(ctx).apply { orientation = LinearLayout.VERTICAL }
            val tvTitle = TextView(ctx).apply {
                text = item.title
                textSize = 15f
                typeface = boldFont
                setTextColor(font1)
            }
            val tvNote = TextView(ctx).apply {
                text = item.note
                textSize = 14f
                typeface = regularFont
                setTextColor(Color.parseColor("#888888"))
                setLineSpacing(4 * dp, 1f)
                setPadding(0, (4 * dp).toInt(), 0, 0)
            }
            contentLayout.addView(tvTitle)
            contentLayout.addView(tvNote)

            itemCard.addView(tvRank)
            itemCard.addView(contentLayout, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginStart = (10 * dp).toInt()
            })
            container.addView(itemCard)
        }
    }

    // ── 공모전 섹션 ───────────────────────────────────────────────────

    private fun renderContestSection(data: ContestGuide) {
        binding.layoutContestSection.visibility = View.VISIBLE
        val container = binding.containerContestCards
        container.removeAllViews()
        val dp = resources.displayMetrics.density
        val context = requireContext()
        val primary = ContextCompat.getColor(context, R.color.primary)
        val font1 = ContextCompat.getColor(context, R.color.font_color1)
        val gap = (8 * dp).toInt()

        data.cards.forEachIndexed { index, card ->
            val cardView = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                background = ContextCompat.getDrawable(context, R.drawable.bg_keypoint_inner_card)
                setPadding((18 * dp).toInt(), (18 * dp).toInt(), (18 * dp).toInt(), (18 * dp).toInt())
            }
            renderContestCard(cardView, card, dp, primary, font1)
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = if (index == data.cards.lastIndex) 0 else gap }
            container.addView(cardView, lp)
        }
    }

    private fun renderContestCard(
        cardView: LinearLayout,
        card: ContestCard,
        dp: Float,
        primary: Int,
        font1: Int
    ) {
        val headerRow = LinearLayout(cardView.context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val ctx = cardView.context
        val boldFont = ResourcesCompat.getFont(ctx, R.font.pretendard_bold)
        val semiBoldFont = ResourcesCompat.getFont(ctx, R.font.pretendard_semibold)
        val regularFont = ResourcesCompat.getFont(ctx, R.font.pretendard_regular)

        val tvDday = TextView(ctx).apply {
            text = "D-${card.dday}"
            textSize = 11f
            typeface = semiBoldFont
            setTextColor(Color.WHITE)
            setPadding((6 * dp).toInt(), (2 * dp).toInt(), (6 * dp).toInt(), (2 * dp).toInt())
            background = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                setColor(primary)
                cornerRadius = 6 * dp
            }
        }
        val tvTitle = TextView(ctx).apply {
            text = card.title
            textSize = 16f
            typeface = boldFont
            setTextColor(font1)
            maxLines = 2
            ellipsize = android.text.TextUtils.TruncateAt.END
            setPadding((8 * dp).toInt(), 0, 0, 0)
        }
        headerRow.addView(tvDday)
        headerRow.addView(
            tvTitle,
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        )
        cardView.addView(headerRow)

        val tvOrganizer = TextView(ctx).apply {
            text = card.organizer
            textSize = 14f
            typeface = regularFont
            setTextColor(Color.parseColor("#888888"))
            setLineSpacing(4 * dp, 1f)
            setPadding((4 * dp).toInt(), (8 * dp).toInt(), 0, 0)
        }
        cardView.addView(tvOrganizer)

        if (card.endDate.isNotBlank()) {
            val tvEndDate = TextView(ctx).apply {
                text = "마감: ${card.endDate}"
                textSize = 14f
                typeface = regularFont
                setTextColor(Color.parseColor("#888888"))
                setPadding((4 * dp).toInt(), (2 * dp).toInt(), 0, 0)
            }
            cardView.addView(tvEndDate)
        }

        val btnLayout = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            background = ContextCompat.getDrawable(ctx, R.drawable.bg_btn_primary)
            isClickable = true
            isFocusable = true
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (46 * dp).toInt()
            ).apply { topMargin = (10 * dp).toInt() }
            setOnClickListener {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(card.url)))
            }
        }
        val tvLabel = TextView(ctx).apply {
            text = "바로가기"
            textSize = 14f
            typeface = semiBoldFont
            setTextColor(Color.WHITE)
        }
        val ivIcon = ImageView(cardView.context).apply {
            setImageDrawable(ContextCompat.getDrawable(cardView.context, R.drawable.ic_external_link))
            val size = (14 * dp).toInt()
            layoutParams = LinearLayout.LayoutParams(size, size).apply {
                marginStart = (4 * dp).toInt()
            }
        }
        btnLayout.addView(tvLabel)
        btnLayout.addView(ivIcon)
        cardView.addView(btnLayout)
    }

    // ── 학업 스트레스 & 시간관리 섹션 ─────────────────────────────────

    private fun renderAcademicSection(data: StudyTips) {
        binding.layoutAcademicSection.visibility = View.VISIBLE
        val container = binding.containerStudySections
        container.removeAllViews()
        val dp = resources.displayMetrics.density
        val inflater = LayoutInflater.from(requireContext())
        val tipGap = (8 * dp).toInt()

        data.sections.forEachIndexed { sIndex, section ->
            val tvSectionTitle = TextView(requireContext()).apply {
                text = section.title
                textSize = 18f
                typeface = ResourcesCompat.getFont(requireContext(), R.font.pretendard_bold)
                setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = if (sIndex == 0) (18 * dp).toInt() else (24 * dp).toInt() }
            }
            container.addView(tvSectionTitle)

            val outerCard = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_white_radius12)
                setPadding((18 * dp).toInt(), (18 * dp).toInt(), (18 * dp).toInt(), (18 * dp).toInt())
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = (8 * dp).toInt() }
            }

            section.tips.forEachIndexed { tIndex, tip ->
                val tipView = inflater.inflate(R.layout.item_keypoint_card, outerCard, false) as LinearLayout
                tipView.findViewById<TextView>(R.id.tv_kp_title).text = "${tip.emoji} ${tip.title}"
                tipView.findViewById<TextView>(R.id.tv_kp_body).text = tip.body
                val lp = tipView.layoutParams as LinearLayout.LayoutParams
                lp.bottomMargin = if (tIndex == section.tips.lastIndex) 0 else tipGap
                tipView.layoutParams = lp
                outerCard.addView(tipView)
            }
            container.addView(outerCard)
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

    private fun renderGenericItems(container: LinearLayout, items: List<ThemeItem>) {
        container.removeAllViews()
        val inflater = LayoutInflater.from(requireContext())
        val dp = resources.displayMetrics.density
        val boldFont = ResourcesCompat.getFont(requireContext(), R.font.pretendard_bold)
        val bg = ContextCompat.getDrawable(requireContext(), R.drawable.bg_keypoint_inner_card)
        val gap = (8 * dp).toInt()

        items.forEach { item ->
            if (item.content.isBlank()) {
                // 빈 content → 섹션 헤더
                val tv = TextView(requireContext()).apply {
                    text = item.title
                    textSize = 15f
                    typeface = boldFont
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.font_color1))
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        topMargin = if (container.childCount == 0) 0 else (14 * dp).toInt()
                        bottomMargin = (4 * dp).toInt()
                    }
                }
                container.addView(tv)
            } else {
                // content 있음 → keypoint 카드
                val card = inflater.inflate(R.layout.item_keypoint_card, container, false) as LinearLayout
                card.background = bg?.constantState?.newDrawable()?.mutate()
                card.findViewById<TextView>(R.id.tv_kp_title).text = item.title
                card.findViewById<TextView>(R.id.tv_kp_body).text = item.content
                val lp = card.layoutParams as LinearLayout.LayoutParams
                lp.bottomMargin = gap
                card.layoutParams = lp
                container.addView(card)
            }
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
