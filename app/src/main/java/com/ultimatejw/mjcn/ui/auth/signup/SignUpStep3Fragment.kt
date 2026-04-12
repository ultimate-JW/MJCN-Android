package com.ultimatejw.mjcn.ui.auth.signup

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.ultimatejw.mjcn.R
import com.ultimatejw.mjcn.databinding.FragmentSignupStep3Binding
import com.ultimatejw.mjcn.utils.gone
import com.ultimatejw.mjcn.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SignUpStep3Fragment : Fragment() {

    private var _binding: FragmentSignupStep3Binding? = null
    private val binding get() = _binding!!

    private val viewModel: SignUpViewModel by activityViewModels()

    private val chipViews: List<TextView> by lazy {
        listOf(
            binding.chipIt,
            binding.chipDesign,
            binding.chipMarketing,
            binding.chipFinance,
            binding.chipEducation,
            binding.chipPublic,
            binding.chipMedical,
            binding.chipMedia,
            binding.chipArchitecture,
            binding.chipSports,
            binding.chipResearch,
            binding.chipOther
        )
    }

    /** 기타 선택 시 숨길 상위 3행 */
    private val hideableRows: List<LinearLayout> by lazy {
        listOf(binding.row1, binding.row2, binding.row3)
    }

    private val otherLabel = "기타(직접 입력)"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignupStep3Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        restoreState()
        observeViewModel()
        setupChipListeners()
        setupListeners()
        // 레이아웃 측정 후 칩 너비를 볼드 기준으로 고정하고
        // 행이 화면 폭을 넘으면 좌우 패딩을 자동으로 줄임
        binding.interestsContainer.post { normalizeChipSizes() }
    }

    private fun normalizeChipSizes() {
        val container = binding.interestsContainer
        val availableWidth = container.width - container.paddingStart - container.paddingEnd
        if (availableWidth <= 0) return

        val rows = listOf(binding.row1, binding.row2, binding.row3, binding.row4)
        val density = resources.displayMetrics.density
        val minHorizontalPadding = (6 * density).toInt()

        // 현재 패딩 기준으로 각 행의 볼드 시 필요 너비 측정
        var maxRowWidth = 0
        rows.forEach { row ->
            maxRowWidth = maxOf(maxRowWidth, measureRowBoldWidth(row))
        }

        if (maxRowWidth > availableWidth) {
            val overflow = maxRowWidth - availableWidth
            // 한 행에 칩 3개 × 좌우 2면 = 6면으로 분배
            val reductionPerSide = (overflow + 5) / 6
            chipViews.forEach { chip ->
                val newH = (chip.paddingStart - reductionPerSide).coerceAtLeast(minHorizontalPadding)
                chip.setPadding(newH, chip.paddingTop, newH, chip.paddingBottom)
            }
        }

        // 각 칩의 볼드 너비로 고정 → 선택/해제 시 크기 변하지 않음
        // 반올림 오차로 텍스트가 줄바꿈되지 않도록 2dp 버퍼 추가
        val widthBuffer = (2 * density).toInt()
        chipViews.forEach { chip ->
            val originalTypeface = chip.typeface
            chip.typeface = Typeface.DEFAULT_BOLD
            chip.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )
            val boldWidth = chip.measuredWidth + widthBuffer
            chip.typeface = originalTypeface

            val lp = chip.layoutParams
            lp.width = boldWidth
            chip.layoutParams = lp
        }
    }

    /** 행 내부 모든 칩을 볼드로 측정해서 합계 너비(마진 포함)를 반환 */
    private fun measureRowBoldWidth(row: LinearLayout): Int {
        var total = 0
        for (i in 0 until row.childCount) {
            val child = row.getChildAt(i) as? TextView ?: continue
            val originalTypeface = child.typeface
            child.typeface = Typeface.DEFAULT_BOLD
            child.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )
            total += child.measuredWidth
            val lp = child.layoutParams as ViewGroup.MarginLayoutParams
            total += lp.marginStart + lp.marginEnd
            child.typeface = originalTypeface
        }
        return total
    }

    /** 이전 선택 상태 복원 */
    private fun restoreState() {
        chipViews.forEach { chip ->
            val selected = viewModel.selectedInterests.contains(chip.text.toString())
            applyChipState(chip, selected)
        }
        applyOtherMode(viewModel.selectedInterests.contains(otherLabel))
    }

    /** 기타 모드 전환 */
    private fun applyOtherMode(otherSelected: Boolean) {
        hideableRows.forEach { row ->
            row.visibility = if (otherSelected) View.GONE else View.VISIBLE
        }

        val density = resources.displayMetrics.density
        val topMarginDp = if (otherSelected) 28 else 48
        val params = binding.interestsContainer.layoutParams as ConstraintLayout.LayoutParams
        params.topMargin = (topMarginDp * density).toInt()
        binding.interestsContainer.layoutParams = params

        if (otherSelected) binding.etOtherInterest.visible()
        else binding.etOtherInterest.gone()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.step3Valid.collect { valid ->
                    binding.btnNext.isEnabled = valid
                    binding.btnNext.alpha = if (valid) 1f else 0.6f
                }
            }
        }
    }

    private fun setupChipListeners() {
        chipViews.forEach { chip ->
            chip.setOnClickListener {
                val label = chip.text.toString()
                val willSelect = !chip.isSelected

                // 최대 3개 제한: 이미 3개 선택된 상태에서 추가 선택 차단
                if (willSelect && viewModel.selectedInterests.size >= 3) return@setOnClickListener

                applyChipState(chip, willSelect)
                viewModel.onInterestToggled(label, willSelect)

                if (label == otherLabel) {
                    applyOtherMode(willSelect)
                }
            }
        }
    }

    private fun applyChipState(chip: TextView, selected: Boolean) {
        chip.isSelected = selected
        chip.typeface = if (selected) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
    }

    private fun setupListeners() {
        binding.btnPrev.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.btnNext.setOnClickListener {
            findNavController().navigate(R.id.action_step3_to_onboarding)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
