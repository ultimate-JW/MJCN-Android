package com.ultimatejw.mjcn.ui.auth.signup

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.ultimatejw.mjcn.R
import com.ultimatejw.mjcn.databinding.FragmentSignupStep1Binding
import com.ultimatejw.mjcn.ui.common.ListPickerBottomSheet

/**
 * 회원가입 Step1 - 기본 정보 입력
 * [수정] 피그마 디자인 기준으로 전면 재작성
 * - Spinner → 바텀시트 피커로 변경
 * - 이름 실시간 유효성 검사 추가
 * - 입학 연도, 졸업 희망 시기 필드 추가
 */
class SignUpStep1Fragment : Fragment() {

    private var _binding: FragmentSignupStep1Binding? = null
    private val binding get() = _binding!!

    private val viewModel: SignUpViewModel by activityViewModels()

    // [추가] 선택된 값 저장용 변수
    private var selectedGrade: String? = null
    private var selectedSemester: String? = null
    private var selectedEntranceYear: String? = null
    private var selectedGraduation: String? = null

    // [추가] 이름 유효성 검사 상태
    private var isNameValid = false
    private var hasNameInput = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignupStep1Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupNameValidation()
        setupPickers()
        observeViewModel()
        setupNextButton()
    }

    /**
     * [추가] 이름 실시간 유효성 검사
     * - 한글 또는 영문 2~10자 기준
     * - 입력 중 조건 불충족 시 즉시 에러 표시
     */
    private fun setupNameValidation() {
        binding.etName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val name = s.toString()
                hasNameInput = name.isNotEmpty()
                isNameValid = name.matches(Regex("^[가-힣a-zA-Z]{2,10}$"))

                // 입력값이 있을 때만 유효성 검사 결과 표시
                when {
                    !hasNameInput -> {
                        // 빈 입력: 기본 상태
                        binding.etName.setBackgroundResource(R.drawable.bg_input_field)
                        binding.tvNameError.visibility = View.GONE
                    }
                    !isNameValid -> {
                        // 유효하지 않은 입력: 에러 상태 (빨간 테두리)
                        binding.etName.setBackgroundResource(R.drawable.bg_input_field_error)
                        binding.tvNameError.visibility = View.VISIBLE
                    }
                    else -> {
                        // 유효한 입력: 활성 상태 (보라색 테두리)
                        binding.etName.setBackgroundResource(R.drawable.bg_input_field_active)
                        binding.tvNameError.visibility = View.GONE
                    }
                }

                updateValidity()
            }
        })
    }

    /**
     * [추가] 바텀시트 피커 설정
     * - 학년, 학기, 입학 연도, 졸업 희망 시기 클릭 시 바텀시트 표시
     */
    private fun setupPickers() {
        // 학년 피커 (1~5학년)
        val grades = listOf("1학년", "2학년", "3학년", "4학년", "5학년")
        binding.tvGrade.setOnClickListener {
            ListPickerBottomSheet.newInstance(
                title = getString(R.string.picker_grade),
                items = grades,
                selectedItem = selectedGrade
            ) { selected ->
                selectedGrade = selected
                binding.tvGrade.text = selected
                // [추가] 선택 시 활성 상태 배경 적용
                binding.tvGrade.setBackgroundResource(R.drawable.bg_input_field_active)
                updateValidity()
            }.show(childFragmentManager, "grade_picker")
        }

        // 학기 피커 (1학기, 여름학기, 2학기, 겨울학기)
        // [수정] 기존 2개(1학기, 2학기) → 4개로 확장
        val semesters = listOf("1학기", "여름학기", "2학기", "겨울학기")
        binding.tvSemester.setOnClickListener {
            ListPickerBottomSheet.newInstance(
                title = getString(R.string.picker_semester),
                items = semesters,
                selectedItem = selectedSemester
            ) { selected ->
                selectedSemester = selected
                binding.tvSemester.text = selected
                binding.tvSemester.setBackgroundResource(R.drawable.bg_input_field_active)
                updateValidity()
            }.show(childFragmentManager, "semester_picker")
        }

        // [추가] 입학 연도 피커 (2021~2026)
        val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        val years = (currentYear downTo currentYear - 5).map { it.toString() }
        binding.tvEntranceYear.setOnClickListener {
            ListPickerBottomSheet.newInstance(
                title = getString(R.string.picker_entrance_year),
                items = years,
                selectedItem = selectedEntranceYear
            ) { selected ->
                selectedEntranceYear = selected
                binding.tvEntranceYear.text = selected
                binding.tvEntranceYear.setBackgroundResource(R.drawable.bg_input_field_active)
                updateValidity()
            }.show(childFragmentManager, "entrance_year_picker")
        }

        // [추가] 졸업 희망 시기 피커 (선택 사항)
        val graduationOptions = buildGraduationOptions()
        binding.tvGraduation.setOnClickListener {
            ListPickerBottomSheet.newInstance(
                title = getString(R.string.picker_graduation),
                items = graduationOptions,
                selectedItem = selectedGraduation
            ) { selected ->
                if (selected == getString(R.string.no_selection)) {
                    selectedGraduation = null
                    binding.tvGraduation.text = ""
                    binding.tvGraduation.setBackgroundResource(R.drawable.bg_input_field)
                    viewModel.graduationDate = null
                } else {
                    selectedGraduation = selected
                    binding.tvGraduation.text = selected
                    binding.tvGraduation.setBackgroundResource(R.drawable.bg_input_field_active)
                    viewModel.graduationDate = selected
                }
            }.show(childFragmentManager, "graduation_picker")
        }
    }

    /**
     * [추가] 졸업 희망 시기 옵션 생성
     * 현재 연도 기준으로 향후 졸업 가능 시점 목록 생성
     */
    private fun buildGraduationOptions(): List<String> {
        val options = mutableListOf(getString(R.string.no_selection))
        val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        for (year in currentYear..currentYear + 4) {
            options.add("${year}년 2월")
            options.add("${year}년 8월")
        }
        return options
    }

    /** ViewModel step1Valid 관찰 → 다음 버튼 활성/비활성 */
    private fun observeViewModel() {
        viewModel.step1Valid.observe(viewLifecycleOwner) { valid ->
            binding.btnNext.isEnabled = valid
            binding.btnNext.alpha = if (valid) 1f else 0.6f
        }
    }

    /** 다음 버튼 클릭 → Step2로 이동 */
    private fun setupNextButton() {
        binding.btnNext.setOnClickListener {
            findNavController().navigate(R.id.action_step1_to_step2)
        }
    }

    /**
     * [수정] 유효성 종합 검사 → ViewModel에 전달
     * 학년/학기를 Int로 변환하여 ViewModel에 전달
     */
    private fun updateValidity() {
        val name = binding.etName.text.toString()
        val gradeInt = selectedGrade?.replace("학년", "")?.toIntOrNull() ?: 0
        val semesterInt = when (selectedSemester) {
            "1학기" -> 1
            "여름학기" -> 2
            "2학기" -> 3
            "겨울학기" -> 4
            else -> 0
        }
        val entranceYearInt = selectedEntranceYear?.toIntOrNull()
        viewModel.onStep1Changed(name, gradeInt, semesterInt, entranceYearInt)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
