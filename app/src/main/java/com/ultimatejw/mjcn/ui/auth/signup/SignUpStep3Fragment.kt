package com.ultimatejw.mjcn.ui.auth.signup

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
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

    private val rowLayouts = mutableListOf<LinearLayout>()

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
        setupOtherInput()
        // 레이아웃 측정 후 칩을 순서대로 greedy flow 배치
        binding.interestsContainer.post { buildFlowLayout() }
    }

    /** 기타 입력칸의 포커스/완료/텍스트 변경 리스너 설정 */
    private fun setupOtherInput() {
        binding.etOtherInterest.setOnFocusChangeListener { _, hasFocus ->
            // 포커스 획득 → 컴팩트 모드(마지막 행만), 포커스 해제 → 풀 모드(전체 행)
            setCompactMode(hasFocus)
        }
        binding.etOtherInterest.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                v.clearFocus()
                hideKeyboard()
                true
            } else {
                false
            }
        }
        binding.etOtherInterest.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.onOtherInterestTextChanged(s?.toString().orEmpty())
            }
        })
    }

    private fun showKeyboard(view: View) {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }

    /**
     * 칩이 화면 폭 경계를 넘으면 그 칩부터 다음 줄로 내려보냄
     */
    private fun buildFlowLayout() {
        val container = binding.interestsContainer
        val availableWidth = container.width - container.paddingStart - container.paddingEnd
        if (availableWidth <= 0) {
            container.post { buildFlowLayout() }
            return
        }

        val density = resources.displayMetrics.density
        val marginEndPx = (10 * density).toInt()
        val rowBottomPx = (12 * density).toInt()
        val widthBuffer = (2 * density).toInt()

        // 최초 호출 시 XML에 선언된 row_1..row_4를 재사용 대상으로 등록
        if (rowLayouts.isEmpty()) {
            rowLayouts.addAll(
                listOf(binding.row1, binding.row2, binding.row3, binding.row4)
            )
        }

        // 모든 칩을 현재 부모에서 떼어내고 기존 행을 비움
        chipViews.forEach { chip ->
            (chip.parent as? ViewGroup)?.removeView(chip)
        }
        rowLayouts.forEach { it.removeAllViews() }

        // 볼드 기준 칩 너비 측정 (선택 시 너비 변동 방지)
        val chipWidths = IntArray(chipViews.size)
        chipViews.forEachIndexed { i, chip ->
            val originalTypeface = chip.typeface
            chip.typeface = Typeface.DEFAULT_BOLD
            chip.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )
            chipWidths[i] = chip.measuredWidth + widthBuffer
            chip.typeface = originalTypeface
        }

        // Greedy 배치: 가용 폭을 넘기는 순간 해당 칩부터 다음 줄로
        val distribution = mutableListOf<MutableList<Int>>()
        var currentRow = mutableListOf<Int>()
        var currentWidth = 0
        chipViews.indices.forEach { i ->
            val w = chipWidths[i]
            val next = if (currentRow.isEmpty()) w else currentWidth + marginEndPx + w
            if (currentRow.isNotEmpty() && next > availableWidth) {
                distribution.add(currentRow)
                currentRow = mutableListOf()
                currentWidth = 0
            }
            currentRow.add(i)
            currentWidth = if (currentRow.size == 1) w else currentWidth + marginEndPx + w
        }
        if (currentRow.isNotEmpty()) distribution.add(currentRow)

        // 행이 부족하면 추가 생성
        while (rowLayouts.size < distribution.size) {
            val newRow = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = rowBottomPx }
            }
            container.addView(newRow)
            rowLayouts.add(newRow)
        }

        // 분배 결과를 각 행에 주입 (칩 간 간격 10dp, 행 마지막 칩은 0)
        distribution.forEachIndexed { rowIdx, chipIndices ->
            val row = rowLayouts[rowIdx]
            chipIndices.forEachIndexed { pos, chipIdx ->
                val chip = chipViews[chipIdx]
                val lp = LinearLayout.LayoutParams(
                    chipWidths[chipIdx],
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                lp.marginEnd = if (pos < chipIndices.size - 1) marginEndPx else 0
                chip.layoutParams = lp
                row.addView(chip)
            }
        }

        // 사용하지 않는 행은 숨김
        rowLayouts.forEach { row ->
            row.visibility = if (row.childCount == 0) View.GONE else View.VISIBLE
        }
    }

    private fun ViewGroup.containsChild(view: View): Boolean {
        for (i in 0 until childCount) {
            if (getChildAt(i) === view) return true
        }
        return false
    }

    /** 이전 선택 상태 복원 */
    private fun restoreState() {
        chipViews.forEach { chip ->
            val selected = viewModel.selectedInterests.contains(chip.text.toString())
            applyChipState(chip, selected)
        }
        val otherSelected = viewModel.selectedInterests.contains(otherLabel)
        if (otherSelected) {
            binding.etOtherInterest.setText(viewModel.otherInterestText)
            binding.etOtherInterest.visible()
        } else {
            binding.etOtherInterest.gone()
        }
        setCompactMode(false)
    }

    private fun setOtherEnabled(enabled: Boolean) {
        if (enabled) {
            binding.etOtherInterest.visible()
            binding.etOtherInterest.requestFocus()
            showKeyboard(binding.etOtherInterest)
        } else {
            binding.etOtherInterest.clearFocus()
            hideKeyboard()
            binding.etOtherInterest.gone()
            setCompactMode(false)
        }
    }

    private fun setCompactMode(compact: Boolean) {
        // 기타(직접 입력)
        val otherRow = rowLayouts.firstOrNull { it.containsChild(binding.chipOther) }
        rowLayouts.forEach { row ->
            row.visibility = when {
                row.childCount == 0 -> View.GONE
                compact && row !== otherRow -> View.GONE
                else -> View.VISIBLE
            }
        }

        val density = resources.displayMetrics.density
        val topMarginDp = if (compact) 28 else 48
        val params = binding.interestsContainer.layoutParams as ConstraintLayout.LayoutParams
        params.topMargin = (topMarginDp * density).toInt()
        binding.interestsContainer.layoutParams = params
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
                    setOtherEnabled(willSelect)
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
