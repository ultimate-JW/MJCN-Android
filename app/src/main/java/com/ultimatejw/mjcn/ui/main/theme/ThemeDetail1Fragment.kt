package com.ultimatejw.mjcn.ui.main.theme

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.ultimatejw.mjcn.R
import com.ultimatejw.mjcn.databinding.FragmentThemeDetail1Binding
import com.ultimatejw.mjcn.domain.model.ThemeItem
import com.ultimatejw.mjcn.ui.common.CurrentUser
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ThemeDetail1Fragment : Fragment() {

    private var _binding: FragmentThemeDetail1Binding? = null
    private val binding get() = _binding!!
    private val viewModel: ThemeDetailViewModel by viewModels()
    private var questionBar: ThemeQuestionBarController? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentThemeDetail1Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnBack.setOnClickListener { findNavController().popBackStack() }

        showFallback()

        val themeId = arguments?.getInt("themeId") ?: 0
        viewModel.load(themeId)

        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            if (state.title.isNotBlank()) renderWithApiData(state)
        }

        questionBar = ThemeQuestionBarController(
            root = binding.root,
            inlineInput = binding.inlineInput,
            floatingInput = binding.floatingInput,
            cardInput = binding.cardInput,
            etMessage = binding.etMessage,
        ).also { it.setup() }
    }

    private fun renderWithApiData(state: ThemeDetailUiState) {
        binding.tvHeaderTitle.text = state.title
        binding.tvAdvice.text = state.adviceText

        renderItemCards(binding.containerCoursesAi, state.contentItems)
        binding.containerCoursesPrev.removeAllViews()

        updateLinkButtons(state.linkItems)
    }

    private fun showFallback() {
        binding.tvHeaderTitle.text = "${CurrentUser.gradeSemester} 수강신청 가이드"
        binding.tvAdvice.text =
            "  ${CurrentUser.honorific}, 이번 학기는 전공 심화 수업이 집중되어 있어 어느 때보다 전략이 필요해요.\n" +
                "  ${CurrentUser.gradeSemester}는 취업 준비의 본격적인 시작점인 만큼, 전공 공부와 대외활동 사이의 균형을 잡는 것이 핵심입니다!"
    }

    private fun updateLinkButtons(links: List<ThemeItem>) {
        fun setup(btnLayout: LinearLayout, item: ThemeItem) {
            (btnLayout.getChildAt(0) as? TextView)?.text = item.title
            btnLayout.setOnClickListener {
                item.externalUrl?.let { url ->
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                }
            }
        }
        links.getOrNull(0)?.let { setup(binding.btnQuick1, it) }
        links.getOrNull(1)?.let { setup(binding.btnQuick2, it) }
        links.getOrNull(2)?.let { setup(binding.btnQuick3, it) }
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

    override fun onDestroyView() {
        questionBar?.cancel()
        questionBar = null
        super.onDestroyView()
        _binding = null
    }
}
