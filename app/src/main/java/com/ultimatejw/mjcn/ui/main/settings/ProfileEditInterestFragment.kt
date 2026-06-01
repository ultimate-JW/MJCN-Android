package com.ultimatejw.mjcn.ui.main.settings

import android.content.Context
import android.graphics.Point
import android.graphics.Rect
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.ultimatejw.mjcn.R
import com.ultimatejw.mjcn.databinding.FragmentSignupStep3Binding
import com.ultimatejw.mjcn.ui.common.LoadingDialog
import com.ultimatejw.mjcn.utils.gone
import com.ultimatejw.mjcn.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileEditInterestFragment : Fragment() {

    private var _binding: FragmentSignupStep3Binding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileEditViewModel by viewModels()

    private val chipViews: List<TextView>
        get() = listOf(
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

    private val otherLabel = ProfileEditViewModel.OTHER_INTEREST_LABEL

    private var sameRowChips: Set<TextView>? = null

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

        binding.tvTitle.text = "관심 분야 변경"
        binding.tvDesc.text = "관심 분야를 선택해주세요"
        binding.btnNext.text = "저장"

        hidePrevButton()
        restoreState()
        observeViewModel()
        setupChipListeners()
        setupListeners()
        setupOtherInput()
        setupKeyboardInsets()
        binding.interestsContainer.post {
            lockChipWidthsToBold()
            binding.interestsContainer.post { cacheSameRowChips() }
        }
    }

    private fun hidePrevButton() {
        binding.btnPrev.visibility = View.GONE
        val cs = ConstraintSet()
        cs.clone(binding.root as ConstraintLayout)
        cs.connect(R.id.btn_next, ConstraintSet.START, ConstraintLayout.LayoutParams.PARENT_ID, ConstraintSet.START, 0)
        cs.connect(R.id.et_other_interest, ConstraintSet.BOTTOM, R.id.btn_next, ConstraintSet.TOP, 0)
        cs.applyTo(binding.root as ConstraintLayout)
    }

    private fun setupOtherInput() {
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
                if (s != null && s.contains('\n')) {
                    s.replace(0, s.length, s.toString().replace("\n", ""))
                    binding.etOtherInterest.clearFocus()
                    hideKeyboard()
                }
                viewModel.onOtherInterestTextChanged(s?.toString().orEmpty())
            }
        })
    }

    private var keyboardLayoutListener: ViewTreeObserver.OnGlobalLayoutListener? = null
    private var lastImeVisible: Boolean? = null

    private fun setupKeyboardInsets() {
        val decorView = requireActivity().window.decorView
        val listener = ViewTreeObserver.OnGlobalLayoutListener {
            checkKeyboardVisibility()
        }
        keyboardLayoutListener = listener
        decorView.viewTreeObserver.addOnGlobalLayoutListener(listener)

        binding.etOtherInterest.setOnFocusChangeListener { _, _ ->
            binding.root.postDelayed({ checkKeyboardVisibility() }, 250)
        }
    }

    private var lastKeyboardTopY: Int = -1

    private fun checkKeyboardVisibility() {
        if (_binding == null) return
        val activity = activity ?: return
        val decorView = activity.window.decorView
        val rect = Rect()
        decorView.getWindowVisibleDisplayFrame(rect)
        val screenHeight = getRealScreenHeight(activity)
        if (screenHeight <= 0) return
        val keypadHeight = screenHeight - rect.bottom
        val imeVisible = keypadHeight > screenHeight * 0.15
        val newKeyboardTopY = if (imeVisible) rect.bottom else -1
        val visibilityChanged = lastImeVisible != imeVisible
        val topChanged = imeVisible && kotlin.math.abs(newKeyboardTopY - lastKeyboardTopY) > 4
        if (visibilityChanged || topChanged) {
            lastImeVisible = imeVisible
            lastKeyboardTopY = newKeyboardTopY
            applyKeyboardLayout(imeVisible, newKeyboardTopY)
        }
    }

    private fun getRealScreenHeight(activity: android.app.Activity): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            activity.windowManager.maximumWindowMetrics.bounds.height()
        } else {
            val display = activity.windowManager.defaultDisplay
            val realSize = Point()
            @Suppress("DEPRECATION")
            display.getRealSize(realSize)
            realSize.y
        }
    }

    private fun applyKeyboardLayout(imeVisible: Boolean, keyboardTopY: Int = -1) {
        setCompactMode(imeVisible)

        val density = resources.displayMetrics.density
        val params = binding.etOtherInterest.layoutParams as ConstraintLayout.LayoutParams
        val root = binding.root

        if (imeVisible && keyboardTopY > 0) {
            binding.btnNext.visibility = View.GONE
            root.setPadding(root.paddingLeft, root.paddingTop, root.paddingRight, 0)

            val rootLoc = IntArray(2)
            root.getLocationOnScreen(rootLoc)
            val rootTopScreenY = rootLoc[1]
            val guideOffset = (keyboardTopY - (19 * density).toInt() - rootTopScreenY)
                .coerceAtLeast(0)

            val guideParams = binding.guideInputBottom.layoutParams as ConstraintLayout.LayoutParams
            guideParams.guideBegin = guideOffset
            guideParams.guideEnd = -1
            guideParams.guidePercent = -1f
            binding.guideInputBottom.layoutParams = guideParams

            params.bottomToTop = binding.guideInputBottom.id
            params.bottomToBottom = ConstraintLayout.LayoutParams.UNSET
            params.bottomMargin = 0
            params.height = 0
        } else {
            binding.btnNext.visibility = View.VISIBLE
            root.setPadding(root.paddingLeft, root.paddingTop, root.paddingRight, (24 * density).toInt())
            params.height = 0
            params.bottomToBottom = ConstraintLayout.LayoutParams.UNSET
            params.bottomToTop = binding.btnNext.id
            params.bottomMargin = (10 * density).toInt()
        }
        binding.etOtherInterest.layoutParams = params
        binding.etOtherInterest.requestLayout()
        binding.root.requestLayout()
    }

    private fun showKeyboard(view: View) {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        view.postDelayed({ checkKeyboardVisibility() }, 350)
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
        binding.root.postDelayed({ checkKeyboardVisibility() }, 350)
    }

    private fun lockChipWidthsToBold() {
        val widthBuffer = (2 * resources.displayMetrics.density).toInt()
        chipViews.forEach { chip ->
            val originalTypeface = chip.typeface
            chip.typeface = Typeface.DEFAULT_BOLD
            chip.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )
            val fixedWidth = chip.measuredWidth + widthBuffer
            chip.typeface = originalTypeface
            val lp = chip.layoutParams
            lp.width = fixedWidth
            chip.layoutParams = lp
        }
    }

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

    private fun cacheSameRowChips() {
        val otherChip = binding.chipOther
        if (otherChip.height <= 0) {
            otherChip.post { cacheSameRowChips() }
            return
        }
        val otherTop = otherChip.top
        sameRowChips = chipViews.filter { it.top == otherTop }.toSet()
    }

    private fun setCompactMode(compact: Boolean) {
        val rowChips = sameRowChips
        chipViews.forEach { chip ->
            chip.visibility = if (compact && rowChips != null && chip !in rowChips) View.GONE else View.VISIBLE
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
                launch {
                    viewModel.interestValid.collect { valid ->
                        binding.btnNext.isEnabled = valid
                        if (valid) {
                            binding.btnNext.setBackgroundResource(R.drawable.bg_btn_primary)
                            binding.btnNext.setTextColor(requireContext().getColor(R.color.white))
                        } else {
                            binding.btnNext.setBackgroundResource(R.drawable.bg_btn_disabled)
                            binding.btnNext.setTextColor(requireContext().getColor(R.color.text_disabled))
                        }
                    }
                }
                launch {
                    viewModel.isSaving.collect { saving ->
                        if (saving) LoadingDialog.show(childFragmentManager)
                        else LoadingDialog.hide(childFragmentManager)
                    }
                }
                launch {
                    viewModel.saveResult.collect { result ->
                        when (result) {
                            is ProfileSaveResult.Success -> findNavController().popBackStack()
                            is ProfileSaveResult.Failure -> Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun setupChipListeners() {
        chipViews.forEach { chip ->
            chip.setOnClickListener {
                val label = chip.text.toString()
                val willSelect = !chip.isSelected

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
        binding.btnNext.setOnClickListener {
            viewModel.saveInterests()
        }
    }

    override fun onDestroyView() {
        keyboardLayoutListener?.let { listener ->
            activity?.window?.decorView?.viewTreeObserver?.removeOnGlobalLayoutListener(listener)
        }
        keyboardLayoutListener = null
        sameRowChips = null
        lastImeVisible = null
        lastKeyboardTopY = -1
        LoadingDialog.hide(childFragmentManager)
        super.onDestroyView()
        _binding = null
    }
}
