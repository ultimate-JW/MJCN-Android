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
import com.ultimatejw.mjcn.databinding.FragmentThemeDetailBinding
import com.ultimatejw.mjcn.domain.model.ThemeItem
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ThemeDetailFragment : Fragment() {

    private var _binding: FragmentThemeDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ThemeDetailViewModel by viewModels()
    private var questionBar: ThemeQuestionBarController? = null

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
        viewModel.load(themeId)

        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            binding.progressLoading.visibility = if (state.isLoading) View.VISIBLE else View.GONE
            if (state.title.isNotBlank()) renderWithApiData(state)
        }

        questionBar = ThemeQuestionBarController(
            root = binding.root,
            inlineInput = binding.inlineInput,
            floatingInput = binding.floatingInput,
            cardInput = binding.cardInput,
            etMessage = binding.etMessage,
        ).also {
            it.onSendMessage = { message ->
                val nav = findNavController()
                nav.popBackStack(R.id.themeDetailFragment, true)
                nav.navigate(R.id.chatFragment)
                nav.navigate(
                    R.id.action_chat_to_detail,
                    androidx.core.os.bundleOf(
                        "sessionId" to "",
                        "initialMessage" to message
                    )
                )
            }
            it.setup()
        }
    }

    private fun renderWithApiData(state: ThemeDetailUiState) {
        binding.tvHeaderTitle.text = state.title
        binding.tvAdvice.text = state.adviceText

        if (state.contentItems.isEmpty()) {
            binding.layoutContentSection.visibility = View.GONE
        } else {
            binding.layoutContentSection.visibility = View.VISIBLE
            renderItemCards(binding.containerContent, state.contentItems)
        }

        updateLinkButtons(state.linkItems)
    }

    private fun updateLinkButtons(links: List<ThemeItem>) {
        if (links.isEmpty()) {
            binding.layoutLinksSection.visibility = View.GONE
            return
        }
        binding.layoutLinksSection.visibility = View.VISIBLE

        fun setup(btnLayout: LinearLayout, item: ThemeItem?) {
            if (item == null) {
                btnLayout.visibility = View.GONE
                return
            }
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

    override fun onDestroyView() {
        questionBar?.cancel()
        questionBar = null
        super.onDestroyView()
        _binding = null
    }
}
