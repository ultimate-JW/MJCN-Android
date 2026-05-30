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
import com.ultimatejw.mjcn.databinding.FragmentThemeDetail3Binding
import com.ultimatejw.mjcn.domain.model.ThemeItem
import com.ultimatejw.mjcn.ui.common.CurrentUser
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ThemeDetail3Fragment : Fragment() {

    private var _binding: FragmentThemeDetail3Binding? = null
    private val binding get() = _binding!!
    private val viewModel: ThemeDetailViewModel by viewModels()
    private var questionBar: ThemeQuestionBarController? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentThemeDetail3Binding.inflate(inflater, container, false)
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

        renderItemCards(binding.containerChoice, state.contentItems)
        binding.containerEvaluation.removeAllViews()
        binding.containerRecommendation.removeAllViews()

        updateLinkButtons(state.linkItems)
    }

    private fun showFallback() {
        binding.tvAdvice.text =
            "  ${CurrentUser.honorific}은 현재 ${CurrentUser.major} ${CurrentUser.gradeSemesterShort}로 지금 시점에서는 \"해외 경험\"보다 취업 준비 완성도가 더 중요한 단계예요.\n" +
                "  하지만 방향에 따라 교환학생이나 해외 인턴도 충분히 좋은 선택이 될 수 있어요."
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
