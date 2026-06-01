package com.ultimatejw.mjcn.ui.main.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.ultimatejw.mjcn.databinding.FragmentSignupStep2Binding
import com.ultimatejw.mjcn.ui.common.ListPickerBottomSheet
import com.ultimatejw.mjcn.ui.common.LoadingDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileEditMajorFragment : Fragment() {

    private var _binding: FragmentSignupStep2Binding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileEditViewModel by viewModels()

    private var selectedCollege: String? = null
    private var selectedDepartment: String? = null
    private var selectedMajor: String? = null

    private var prefilled = false

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

        binding.tvTitle.text = "전공 선택 변경"
        binding.tvDesc.text = "변경할 전공 정보를 선택해주세요"
        binding.btnNext.text = "저장"

        viewModel.loadProfile()

        hidePrevButton()
        setupPickers()
        observeViewModel()
        setupButtons()
    }

    private fun hidePrevButton() {
        binding.btnPrev.visibility = View.GONE
        val cs = ConstraintSet()
        cs.clone(binding.root as ConstraintLayout)
        cs.connect(R.id.btn_next, ConstraintSet.START, ConstraintLayout.LayoutParams.PARENT_ID, ConstraintSet.START, 0)
        cs.applyTo(binding.root as ConstraintLayout)
    }

    private fun setupPickers() {
        val colleges = universityData.keys.toList()

        binding.tvCollege.setOnClickListener {
            ListPickerBottomSheet.newInstance(
                title = getString(R.string.picker_college),
                items = colleges,
                selectedItem = selectedCollege
            ) { item ->
                selectedCollege = item
                binding.tvCollege.text = item
                binding.tvCollege.setBackgroundResource(R.drawable.bg_input_field_active)
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
                selectedMajor = null
                binding.tvMajor.text = ""
                binding.tvMajor.hint = getString(R.string.major_hint)
                binding.tvMajor.setBackgroundResource(R.drawable.bg_input_field)
                updateValidity()
            }.show(childFragmentManager, "department_picker")
        }

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

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.majorValid.collect { valid ->
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
                    viewModel.profile.collect { profile ->
                        if (profile != null && !prefilled) {
                            prefilled = true
                            prefillFromProfile(profile)
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

    private fun prefillFromProfile(profile: com.ultimatejw.mjcn.data.remote.dto.profile.ProfileResponse) {
        val majorStr = profile.major ?: return
        val parts = majorStr.split(" · ")
        if (parts.size < 3) return
        val college = parts[0]
        val department = parts[1]
        val major = parts[2]
        // Only pre-fill if the college exists in our data
        if (!universityData.containsKey(college)) return
        selectedCollege = college
        binding.tvCollege.text = college
        binding.tvCollege.setBackgroundResource(R.drawable.bg_input_field_active)
        selectedDepartment = department
        binding.tvDepartment.text = department
        binding.tvDepartment.setBackgroundResource(R.drawable.bg_input_field_active)
        selectedMajor = major
        binding.tvMajor.text = major
        binding.tvMajor.setBackgroundResource(R.drawable.bg_input_field_active)
        updateValidity()
    }

    private fun setupButtons() {
        binding.btnNext.setOnClickListener {
            viewModel.saveMajor()
        }
    }

    private fun updateValidity() {
        viewModel.onMajorChanged(
            selectedCollege ?: "",
            selectedDepartment ?: "",
            selectedMajor ?: ""
        )
    }

    override fun onDestroyView() {
        LoadingDialog.hide(childFragmentManager)
        super.onDestroyView()
        _binding = null
    }
}
