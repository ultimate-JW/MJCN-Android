package com.ultimatejw.mjcn.ui.auth.signup
import dagger.hilt.android.AndroidEntryPoint

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import com.ultimatejw.mjcn.R
import com.ultimatejw.mjcn.databinding.FragmentSignupStep2Binding
// [추가] BottomSheet 피커 import
import com.ultimatejw.mjcn.ui.common.ListPickerBottomSheet

@AndroidEntryPoint
class SignUpStep2Fragment : Fragment() {

    private var _binding: FragmentSignupStep2Binding? = null
    private val binding get() = _binding!!

    private val viewModel: SignUpViewModel by activityViewModels()

    private var selectedCollege: String? = null
    private var selectedDepartment: String? = null
    private var selectedMajor: String? = null

    private val universityData: Map<String, Map<String, List<String>>> = mapOf(
        "화학·생명과학대학" to mapOf(
            "화학과" to listOf("화학전공"),
            "생명과학과" to listOf("생명과학전공"),
            "식품영양학과" to listOf("식품영양학전공")
        ),
        "스마트시스템공과대학" to mapOf(
            "기계공학과" to listOf("기계공학전공"),
            "산업공학과" to listOf("산업공학전공"),
            "교통공학과" to listOf("교통공학전공")
        ),
        "반도체·ICT대학" to mapOf(
            "반도체공학부" to listOf("반도체공학전공"),
            "전기전자공학부" to listOf("전기전자공학전공"),
            "컴퓨터정보통신공학부" to listOf("컴퓨터공학전공", "정보통신공학전공"),
            "산업경영공학과" to listOf("산업경영공학전공")
        ),
        "스포츠예술대학" to mapOf(
            "체육학부" to listOf("체육학전공"),
            "예술학부" to listOf("예술학전공")
        ),
        "건축대학" to mapOf(
            "건축학과" to listOf("건축학전공"),
            "건축공학과" to listOf("건축공학전공")
        ),
        "아너칼리지" to mapOf(
            "아너칼리지학부" to listOf("아너칼리지전공")
        )
    )

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

    // [추가] BottomSheet 피커 클릭 리스너 설정
    private fun setupPickers() {
        val colleges = universityData.keys.toList()

        // 대학 클릭 → BottomSheet
        binding.tvCollege.setOnClickListener {
            ListPickerBottomSheet.newInstance(
                title = getString(R.string.picker_college),
                items = colleges,
                selectedItem = selectedCollege
            ) { item ->
                selectedCollege = item
                binding.tvCollege.text = item
                binding.tvCollege.setBackgroundResource(R.drawable.bg_input_field_active)
                // [추가] 대학 변경 시 하위 선택 초기화
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

        // 학부/학과 클릭 → BottomSheet (대학 선택 후에만 동작)
        binding.tvDepartment.setOnClickListener {
            val college = selectedCollege ?: return@setOnClickListener
            val departments = universityData[college]?.keys?.toList() ?: return@setOnClickListener

            ListPickerBottomSheet.newInstance(
                title = getString(R.string.picker_department),
                items = departments,
                selectedItem = selectedDepartment
            ) { item ->
                selectedDepartment = item
                binding.tvDepartment.text = item
                binding.tvDepartment.setBackgroundResource(R.drawable.bg_input_field_active)
                // [추가] 학부/학과 변경 시 전공 초기화
                selectedMajor = null
                binding.tvMajor.text = ""
                binding.tvMajor.hint = getString(R.string.major_hint)
                binding.tvMajor.setBackgroundResource(R.drawable.bg_input_field)
                updateValidity()
            }.show(childFragmentManager, "department_picker")
        }

        // 전공 클릭 → BottomSheet (학부/학과 선택 후에만 동작)
        binding.tvMajor.setOnClickListener {
            val college = selectedCollege ?: return@setOnClickListener
            val department = selectedDepartment ?: return@setOnClickListener
            val majors = universityData[college]?.get(department) ?: return@setOnClickListener

            ListPickerBottomSheet.newInstance(
                title = getString(R.string.picker_major),
                items = majors,
                selectedItem = selectedMajor
            ) { item ->
                selectedMajor = item
                binding.tvMajor.text = item
                binding.tvMajor.setBackgroundResource(R.drawable.bg_input_field_active)
                updateValidity()
            }.show(childFragmentManager, "major_picker")
        }
    }

    // [수정] majorStepValid StateFlow 관찰 (기존 step2Valid 대신)
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.majorStepValid.collect { valid ->
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

    // [추가] 이전/다음 버튼 리스너
    private fun setupButtons() {
        // 이전 버튼 → 뒤로 가기
        binding.btnPrev.setOnClickListener {
            findNavController().popBackStack()
        }

        // 다음 버튼 → Step3로 이동
        binding.btnNext.setOnClickListener {
            findNavController().navigate(R.id.action_step2_to_step3)
        }
    }

    // [추가] ViewModel에 전공 선택 유효성 전달
    private fun updateValidity() {
        viewModel.onMajorStepChanged(
            selectedCollege ?: "",
            selectedDepartment ?: "",
            selectedMajor ?: ""
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
