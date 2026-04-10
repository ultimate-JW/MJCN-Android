package com.ultimatejw.mjcn.ui.auth.signup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.ultimatejw.mjcn.R
import com.ultimatejw.mjcn.databinding.FragmentSignupStep2Binding
import com.ultimatejw.mjcn.ui.common.ListPickerBottomSheet

/**
 * 회원가입 Step2 - 전공 선택
 * [수정] 피그마 디자인 기준으로 전면 재작성
 * - 기존 이메일/비밀번호 입력 화면 → 전공 선택 화면으로 변경
 * - 대학 → 학부/학과 → 전공 순서로 캐스케이딩 선택
 * - 이전/다음 버튼 구조
 */
class SignUpStep2Fragment : Fragment() {

    private var _binding: FragmentSignupStep2Binding? = null
    private val binding get() = _binding!!

    private val viewModel: SignUpViewModel by activityViewModels()

    // [추가] 선택된 값 저장용 변수
    private var selectedCollege: String? = null
    private var selectedDepartment: String? = null
    private var selectedMajor: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignupStep2Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupPickers()
        observeViewModel()
        setupButtons()
    }

    /**
     * [추가] 대학/학부/전공 바텀시트 피커 설정
     * - 대학 선택 시 → 해당 대학의 학부/학과 목록 활성화
     * - 학부/학과 선택 시 → 해당 학부의 전공 목록 활성화
     * - 전공이 없는 학부(=학과)는 학과명을 전공으로 자동 설정
     */
    private fun setupPickers() {
        // 대학 피커
        binding.tvCollege.setOnClickListener {
            val colleges = getCollegeList()
            ListPickerBottomSheet.newInstance(
                title = getString(R.string.picker_college),
                items = colleges,
                selectedItem = selectedCollege
            ) { selected ->
                selectedCollege = selected
                binding.tvCollege.text = selected
                binding.tvCollege.setBackgroundResource(R.drawable.bg_input_field_active)

                // [추가] 대학 변경 시 하위 선택값 초기화
                selectedDepartment = null
                selectedMajor = null
                binding.tvDepartment.text = ""
                binding.tvDepartment.hint = getString(R.string.department_hint)
                binding.tvDepartment.setBackgroundResource(R.drawable.bg_input_field)
                binding.tvMajor.text = ""
                binding.tvMajor.hint = getString(R.string.major_hint)
                binding.tvMajor.setBackgroundResource(R.drawable.bg_input_field)

                updateValidity()
            }.show(childFragmentManager, "college_picker")
        }

        // 학부/학과 피커
        binding.tvDepartment.setOnClickListener {
            val college = selectedCollege ?: return@setOnClickListener
            val departments = getDepartmentList(college)
            ListPickerBottomSheet.newInstance(
                title = getString(R.string.picker_department),
                items = departments,
                selectedItem = selectedDepartment
            ) { selected ->
                selectedDepartment = selected
                binding.tvDepartment.text = selected
                binding.tvDepartment.setBackgroundResource(R.drawable.bg_input_field_active)

                // [추가] 학부 변경 시 전공 초기화
                selectedMajor = null
                binding.tvMajor.text = ""
                binding.tvMajor.setBackgroundResource(R.drawable.bg_input_field)

                // 전공 목록이 있는지 확인
                val majors = getMajorList(college, selected)
                if (majors.isEmpty()) {
                    // 전공 없는 학과 → 학과명을 전공으로 자동 설정
                    selectedMajor = selected
                    binding.tvMajor.text = selected
                    binding.tvMajor.setBackgroundResource(R.drawable.bg_input_field_active)
                }

                updateValidity()
            }.show(childFragmentManager, "department_picker")
        }

        // 전공 피커
        binding.tvMajor.setOnClickListener {
            val college = selectedCollege ?: return@setOnClickListener
            val department = selectedDepartment ?: return@setOnClickListener
            val majors = getMajorList(college, department)
            if (majors.isEmpty()) return@setOnClickListener // 전공 세분화 없는 학과

            ListPickerBottomSheet.newInstance(
                title = getString(R.string.picker_major),
                items = majors,
                selectedItem = selectedMajor
            ) { selected ->
                selectedMajor = selected
                binding.tvMajor.text = selected
                binding.tvMajor.setBackgroundResource(R.drawable.bg_input_field_active)
                updateValidity()
            }.show(childFragmentManager, "major_picker")
        }
    }

    /** ViewModel step2Valid 관찰 → 다음 버튼 활성/비활성 */
    private fun observeViewModel() {
        viewModel.step2Valid.observe(viewLifecycleOwner) { valid ->
            binding.btnNext.isEnabled = valid
            binding.btnNext.alpha = if (valid) 1f else 0.6f
        }
    }

    /** [추가] 이전/다음 버튼 설정 */
    private fun setupButtons() {
        // 이전 버튼 → Step1로 돌아가기
        binding.btnPrev.setOnClickListener {
            findNavController().navigateUp()
        }

        // 다음 버튼 → Step3으로 이동
        binding.btnNext.setOnClickListener {
            findNavController().navigate(R.id.action_step2_to_step3)
        }
    }

    /** [수정] 유효성 종합 검사 → ViewModel에 전달 */
    private fun updateValidity() {
        viewModel.onStep2Changed(
            selectedCollege ?: "",
            selectedDepartment ?: "",
            selectedMajor ?: ""
        )
    }

    // ====================================================
    // [추가] 대학/학부/전공 데이터 (피그마 기준)
    // ====================================================

    /** 대학 목록 */
    private fun getCollegeList(): List<String> {
        return listOf(
            "화학·생명과학대학",
            "스마트시스템공과대학",
            "반도체·ICT대학",
            "스포츠예술대학",
            "건축대학",
            "아너칼리지"
        )
    }

    /** 대학별 학부/학과 목록 */
    private fun getDepartmentList(college: String): List<String> {
        return when (college) {
            "반도체·ICT대학" -> listOf(
                "반도체공학부",
                "전기전자공학부",
                "전기전자공학부",
                "컴퓨터정보통신공학부",
                "산업경영공학과"
            )
            "화학·생명과학대학" -> listOf(
                "화학과",
                "생명과학과",
                "식품영양학과",
                "화학공학과"
            )
            "스마트시스템공과대학" -> listOf(
                "기계공학과",
                "산업공학과",
                "스마트자동차공학과"
            )
            "스포츠예술대학" -> listOf(
                "체육학부",
                "예술학부",
                "바둑학과"
            )
            "건축대학" -> listOf(
                "건축학부",
                "공간디자인학과"
            )
            "아너칼리지" -> listOf(
                "아너칼리지"
            )
            else -> emptyList()
        }
    }

    /**
     * 학부별 전공 목록
     * 전공이 세분화되지 않은 학과는 빈 리스트 반환
     * → 학과명이 곧 전공명
     */
    private fun getMajorList(college: String, department: String): List<String> {
        return when {
            college == "반도체·ICT대학" && department == "컴퓨터정보통신공학부" -> listOf(
                "컴퓨터공학전공",
                "정보통신공학전공"
            )
            college == "반도체·ICT대학" && department == "전기전자공학부" -> listOf(
                "전기공학전공",
                "전자공학전공"
            )
            else -> emptyList()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
