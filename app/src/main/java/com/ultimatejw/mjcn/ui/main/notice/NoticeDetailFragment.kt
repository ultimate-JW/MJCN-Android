package com.ultimatejw.mjcn.ui.main.notice

import android.animation.ValueAnimator
import android.graphics.RectF
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.shape.AbsoluteCornerSize
import com.ultimatejw.mjcn.R
import com.ultimatejw.mjcn.databinding.FragmentNoticeDetailBinding
import com.ultimatejw.mjcn.domain.model.Notice
import com.ultimatejw.mjcn.domain.model.NoticeCard
import com.ultimatejw.mjcn.domain.model.NoticeCategory
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NoticeDetailFragment : Fragment() {

    private var _binding: FragmentNoticeDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NoticeDetailViewModel by viewModels()

    private var isKeyboardVisible = false
    private var keyboardAnimator: ValueAnimator? = null
    private lateinit var argNotice: Notice

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNoticeDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        argNotice = Notice(
            id = arguments?.getString("noticeId").orEmpty(),
            category = arguments?.getString("noticeCategory").orEmpty(),
            title = arguments?.getString("noticeTitle").orEmpty(),
            team = arguments?.getString("noticeTeam").orEmpty(),
            date = arguments?.getString("noticeDate").orEmpty(),
            summary = arguments?.getString("noticeSummary").orEmpty()
        )
        applyNotice(argNotice)

        // API 응답으로 업데이트 — args 데이터를 fallback으로 merge
        viewModel.notice.observe(viewLifecycleOwner) { apiNotice ->
            applyNotice(apiNotice.mergeWith(argNotice))
        }

        setupQuestionBarToggle()
    }

    private fun Notice.mergeWith(fallback: Notice) = copy(
        category = category.ifBlank { fallback.category },
        title    = title.ifBlank { fallback.title },
        team     = team.ifBlank { fallback.team },
        date     = date.ifBlank { fallback.date },
        url      = url.ifBlank { fallback.url },
        summary  = summary.ifBlank { fallback.summary },
        cards    = cards.ifEmpty { fallback.cards }
    )

    /**
     * inline 질문바를 클릭하면 floating 으로 전환 + 키보드 올림.
     * 키보드 가시성 변화를 IME inset 으로 감지해서, ChatDetailFragment 와 같은 방식으로
     * floating 카드를 (가로 패딩 0 + 코너 0) 직사각형으로 morph. 키보드 내려가면 inline 복귀.
     */
    private fun setupQuestionBarToggle() {
        binding.inlineInput.setOnClickListener { showFloating() }
        setupKeyboardAnimation()
    }

    private fun setupKeyboardAnimation() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val imeBottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            val navBottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            val bottomPadding = maxOf(imeBottom, navBottom)
            val imeVisible = imeBottom > navBottom

            binding.root.setPadding(0, 0, 0, bottomPadding)

            if (imeVisible != isKeyboardVisible) {
                isKeyboardVisible = imeVisible
                animateForKeyboard(imeVisible)
                if (!imeVisible && binding.floatingInput.visibility == View.VISIBLE) {
                    binding.root.postDelayed({
                        if (!isKeyboardVisible) hideFloating()
                    }, 200L)
                }
            }
            insets
        }
    }

    private fun animateForKeyboard(keyboardVisible: Boolean) {
        val card = binding.cardInput
        val inputLayout = binding.floatingInput
        val density = resources.displayMetrics.density

        inputLayout.setPadding(
            inputLayout.paddingLeft,
            inputLayout.paddingTop,
            inputLayout.paddingRight,
            if (keyboardVisible) 0 else (20 * density).toInt()
        )

        val startPaddingH = inputLayout.paddingLeft.toFloat()
        val endPaddingH = if (keyboardVisible) 0f else 16 * density
        val startElevation = card.cardElevation
        val endElevation = 10 * density
        val bounds = RectF(0f, 0f, card.width.toFloat(), card.height.toFloat())
        val startRadius = card.shapeAppearanceModel.topLeftCornerSize.getCornerSize(bounds)
        val endRadius = if (keyboardVisible) 0f else 32 * density

        keyboardAnimator?.cancel()
        keyboardAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 200
            interpolator = DecelerateInterpolator()
            addUpdateListener { anim ->
                val f = anim.animatedFraction
                val h = lerp(startPaddingH, endPaddingH, f).toInt()
                inputLayout.setPadding(h, inputLayout.paddingTop, h, inputLayout.paddingBottom)
                card.cardElevation = lerp(startElevation, endElevation, f)
                card.shapeAppearanceModel = card.shapeAppearanceModel.toBuilder()
                    .setAllCornerSizes(AbsoluteCornerSize(lerp(startRadius, endRadius, f)))
                    .build()
            }
            start()
        }
    }

    private fun lerp(start: Float, end: Float, fraction: Float) =
        start + (end - start) * fraction

    private fun showFloating() {
        binding.inlineInput.visibility = View.INVISIBLE
        binding.floatingInput.visibility = View.VISIBLE
        binding.etMessage.requestFocus()
        val imm = requireContext().getSystemService(InputMethodManager::class.java)
        imm?.showSoftInput(binding.etMessage, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun hideFloating() {
        binding.floatingInput.visibility = View.GONE
        binding.inlineInput.visibility = View.VISIBLE
        binding.etMessage.clearFocus()
        binding.etMessage.text?.clear()
    }

    private var currentUrl: String = ""

    private fun applyNotice(notice: Notice) {
        binding.tvCategory.text = notice.category
        binding.tvTitle.text = notice.title
        binding.tvTeamDate.text = if (notice.team.isBlank()) notice.date else "${notice.team} · ${notice.date}"
        binding.tvSummary.text = notice.summary
        applyCategoryChip(notice.category)
        if (notice.cards.isNotEmpty()) {
            renderKeypoints(notice.cards)
        } else if (notice.summary.isNotBlank()) {
            renderKeypoints(listOf(NoticeCard("요약", listOf(notice.summary))))
        }
        if (notice.url.isNotBlank()) {
            currentUrl = notice.url
            binding.btnOriginalLink.visibility = View.VISIBLE
            binding.btnOriginalLink.setOnClickListener {
                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW,
                    android.net.Uri.parse(currentUrl))
                startActivity(intent)
            }
        } else {
            binding.btnOriginalLink.visibility = View.GONE
        }
    }

    private fun renderKeypoints(cards: List<NoticeCard>) {
        val container = binding.layoutKeypointsInner
        container.removeAllViews()
        val inflater = LayoutInflater.from(requireContext())
        val gap = (12 * resources.displayMetrics.density).toInt()
        cards.forEachIndexed { index, card ->
            val view = inflater.inflate(R.layout.item_keypoint_card, container, false) as LinearLayout
            view.findViewById<TextView>(R.id.tv_kp_title).text = card.title
            view.findViewById<TextView>(R.id.tv_kp_body).text =
                card.items.joinToString("\n") { "• $it" }
            val lp = view.layoutParams as LinearLayout.LayoutParams
            lp.bottomMargin = if (index == cards.lastIndex) 0 else gap
            view.layoutParams = lp
            container.addView(view)
        }
    }

    private fun applyCategoryChip(categoryLabel: String) {
        val context = binding.root.context
        val category = NoticeCategory.from(categoryLabel)
        val bgColorRes = when (category) {
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
        val textColorRes = when (category) {
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
        keyboardAnimator?.cancel()
        _binding = null
    }
}
