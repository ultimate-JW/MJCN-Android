package com.ultimatejw.mjcn.ui.auth.signup

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

    // step4 → step3 복귀처럼 뷰가 재생성될 때 by lazy 가 옛 binding 의 칩 참조를 그대로
    // 들고 있는 문제를 방지하기 위해 매번 현재 binding 에서 새로 만든다.
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

    private val otherLabel = "기타(직접 입력)"

    // 기타 칩과 같은 행(같은 top Y)에 위치한 칩 집합. 초기 레이아웃 후 캐싱.
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
        restoreState()
        observeViewModel()
        setupChipListeners()
        setupListeners()
        setupOtherInput()
        setupKeyboardInsets()
        // 선택 시 BOLD 적용으로 폭이 변하는 것을 막기 위해 BOLD 기준 폭으로 고정
        // 폭 고정으로 줄바꿈이 재계산되므로, 한 단계 더 post하여 같은 행 칩을 캐싱한다.
        binding.interestsContainer.post {
            lockChipWidthsToBold()
            binding.interestsContainer.post { cacheSameRowChips() }
        }
    }

    /** 기타 입력칸의 완료/텍스트 변경 리스너 설정 */
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
                    // textMultiLine 환경에서 IME Enter 키가 눌린 경우 입력 종료로 처리:
                    // 줄바꿈 문자를 제거하고 키보드를 닫는다.
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

    /**
     * 키보드(IME) 표시 상태에 따라 기타 입력칸의 하단 제약을 동적으로 조정한다.
     * - 키보드 표시: 버튼 숨김, EditText 하단을 부모 바닥(=키보드 윗면)에서 19dp 위로 고정.
     *   maxHeight=285dp + bias=1.0으로 285dp까지 채우되 키보드 침범 시 줄어듦.
     * - 키보드 숨김: 버튼 노출, EditText 하단을 btn_prev 위 10dp로 복귀.
     *
     * 다중 트리거로 IME 감지 신뢰성 확보:
     *   1) decorView OnGlobalLayoutListener (시스템 레이아웃 변경)
     *   2) showKeyboard / hideKeyboard 호출 직후 postDelayed
     *   3) EditText 포커스 변경 직후 postDelayed
     */
    private fun setupKeyboardInsets() {
        val decorView = requireActivity().window.decorView
        val listener = ViewTreeObserver.OnGlobalLayoutListener {
            checkKeyboardVisibility()
        }
        keyboardLayoutListener = listener
        decorView.viewTreeObserver.addOnGlobalLayoutListener(listener)

        // 포커스 변경도 트리거로 사용 (사용자가 탭으로 EditText 진입/이탈 시)
        binding.etOtherInterest.setOnFocusChangeListener { _, _ ->
            binding.root.postDelayed({ checkKeyboardVisibility() }, 250)
        }
    }

    private var lastKeyboardTopY: Int = -1

    /** 다양한 트리거에서 호출되는 키보드 가시성 측정/적용 */
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
        // 키보드 표시 여부가 바뀌거나, 키보드 높이가 의미 있게 변한 경우(IME 교체 등) 재적용
        val visibilityChanged = lastImeVisible != imeVisible
        val topChanged = imeVisible && kotlin.math.abs(newKeyboardTopY - lastKeyboardTopY) > 4
        if (visibilityChanged || topChanged) {
            lastImeVisible = imeVisible
            lastKeyboardTopY = newKeyboardTopY
            applyKeyboardLayout(imeVisible, newKeyboardTopY)
        }
    }

    /** 노치/폴더블 등 영향 없는 실제 화면 높이(픽셀) */
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
        // 키보드 표시 시 같은 행 칩만 노출, 숨김 시 전체 노출
        setCompactMode(imeVisible)

        val density = resources.displayMetrics.density
        val params = binding.etOtherInterest.layoutParams as ConstraintLayout.LayoutParams
        val root = binding.root

        if (imeVisible && keyboardTopY > 0) {
            binding.btnPrev.visibility = View.GONE
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
            binding.btnPrev.visibility = View.VISIBLE
            binding.btnNext.visibility = View.VISIBLE
            root.setPadding(root.paddingLeft, root.paddingTop, root.paddingRight, (24 * density).toInt())
            params.height = 0
            params.bottomToBottom = ConstraintLayout.LayoutParams.UNSET
            params.bottomToTop = binding.btnPrev.id
            params.bottomMargin = (10 * density).toInt()
        }
        binding.etOtherInterest.layoutParams = params
        binding.etOtherInterest.requestLayout()
        binding.root.requestLayout()
    }

    private fun showKeyboard(view: View) {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        // 키보드 애니메이션 종료 후 검사(시스템 이벤트가 늦거나 누락되는 경우 대비)
        view.postDelayed({ checkKeyboardVisibility() }, 350)
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
        binding.root.postDelayed({ checkKeyboardVisibility() }, 350)
    }

    /** BOLD 기준 폭으로 칩 너비를 고정해 선택 시 폭 변동 방지 */
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

    /** 기타 칩과 같은 행(같은 top Y)에 위치한 칩들을 캐싱 */
    private fun cacheSameRowChips() {
        val otherChip = binding.chipOther
        if (otherChip.height <= 0) {
            // 아직 레이아웃 미완료 시 한 프레임 더 기다린 뒤 재시도
            otherChip.post { cacheSameRowChips() }
            return
        }
        val otherTop = otherChip.top
        sameRowChips = chipViews.filter { it.top == otherTop }.toSet()
    }

    private fun setCompactMode(compact: Boolean) {
        // 컴팩트 모드: 기타 칩과 같은 행에 있는 칩만 보이도록, 나머지 칩은 GONE
        // 캐시가 아직 준비되지 않은 경우 안전하게 전체 표시로 폴백
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
                viewModel.step3Valid.collect { valid ->
                    // step2 와 동일한 토글 패턴: 배경/글씨색을 함께 스왑
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
            findNavController().navigate(R.id.action_step3_to_step4)
        }
    }

    override fun onDestroyView() {
        keyboardLayoutListener?.let { listener ->
            activity?.window?.decorView?.viewTreeObserver?.removeOnGlobalLayoutListener(listener)
        }
        keyboardLayoutListener = null
        // 뷰 캐시도 함께 리셋: 옛 binding 의 칩 참조가 남아 다음 onViewCreated 의 캐싱과 충돌하는 것을 방지.
        sameRowChips = null
        lastImeVisible = null
        lastKeyboardTopY = -1
        super.onDestroyView()
        _binding = null
    }
}
