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
import com.ultimatejw.mjcn.databinding.FragmentThemeDetail4Binding
import com.ultimatejw.mjcn.domain.model.ThemeItem
import com.ultimatejw.mjcn.ui.common.CurrentUser
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ThemeDetail4Fragment : Fragment() {

    private var _binding: FragmentThemeDetail4Binding? = null
    private val binding get() = _binding!!
    private val viewModel: ThemeDetailViewModel by viewModels()
    private var questionBar: ThemeQuestionBarController? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentThemeDetail4Binding.inflate(inflater, container, false)
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
        binding.tvAdvice.text = state.adviceText
        binding.tvPriorityTitle.text = "${CurrentUser.honorific} 맞춤 우선순위"

        renderItemCards(binding.containerPrograms, state.contentItems)
        binding.containerPriorities.removeAllViews()

        val firstLink = state.linkItems.firstOrNull()
        if (firstLink != null) {
            binding.btnPriorityApply.setOnClickListener {
                firstLink.externalUrl?.let { url ->
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                }
            }
        }
    }

    private fun showFallback() {
        binding.tvAdvice.text =
            "  ${CurrentUser.honorific}은 현재 2001년 3월 30일생 만 22세로 대부분의 청년 대상 지원사업 신청 가능 구간이에요.\n" +
                "  특히 지금은 \"취업 준비 + 실무 경험 + 경제 지원\"을 같이 가져가는 게 중요해요.\n" +
                "  신청 가능한 국가 사업을 모두 알려드릴게요!"
        binding.tvPriorityTitle.text = "${CurrentUser.honorific} 맞춤 우선순위"
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
