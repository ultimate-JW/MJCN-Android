package com.ultimatejw.mjcn.ui.auth.signup
import dagger.hilt.android.AndroidEntryPoint

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.ultimatejw.mjcn.databinding.FragmentSignupStep1Binding
import com.ultimatejw.mjcn.ui.common.ListPickerBottomSheet

@AndroidEntryPoint
class SignUpStep1Fragment : Fragment() {

    private var _binding: FragmentSignupStep1Binding? = null
    private val binding get() = _binding!!

    private val viewModel: SignUpViewModel by activityViewModels()

    private var selectedGrade: String? = null
    private var selectedSemester: String? = null
    private var selectedEntranceYear: String? = null
    private var selectedGraduation: String? = null

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
        setupPickers()
        observeViewModel()
        setupListeners()
        restoreState()
    }

    // ViewModel에 저장된 값으로 UI 복원
    private fun restoreState() {
        if (viewModel.name.isNotEmpty()) {
            binding.etName.setText(viewModel.name)
            applyNameFieldState(viewModel.name)
        }
        viewModel.selectedGradeText?.let { text ->
            selectedGrade = text
            binding.tvGrade.text = text
            binding.tvGrade.setBackgroundResource(R.drawable.bg_input_field_active)
        }
        viewModel.selectedSemesterText?.let { text ->
            selectedSemester = text
            binding.tvSemester.text = text
            binding.tvSemester.setBackgroundResource(R.drawable.bg_input_field_active)
        }
        viewModel.selectedEntranceYearText?.let { text ->
            selectedEntranceYear = text
            binding.tvEntranceYear.text = text
            binding.tvEntranceYear.setBackgroundResource(R.drawable.bg_input_field_active)
        }
        viewModel.graduationTerm?.let { text ->
            selectedGraduation = text
            binding.tvGraduation.text = text
            binding.tvGraduation.setBackgroundResource(R.drawable.bg_input_field_active)
        }
        updateValidity()
    }

    private fun setupPickers() {
        val grades = listOf("1학년", "2학년", "3학년", "4학년", "5학년")
        val semesters = listOf("1학기", "여름학기", "2학기", "겨울학기")
        val entranceYears = (2026 downTo 2021).map { "${it}" }
        val graduationTerms = listOf(
            "선택 안 함", "2026년 2월", "2026년 8월",
            "2027년 2월", "2027년 8월", "2028년 2월"
        )

        binding.tvGrade.setOnClickListener {
            ListPickerBottomSheet.newInstance(
                title = getString(R.string.picker_grade),
                items = grades,
                selectedItem = selectedGrade
            ) { item ->
                selectedGrade = item
                binding.tvGrade.text = item
                binding.tvGrade.setBackgroundResource(R.drawable.bg_input_field_active)
                viewModel.selectedGradeText = item
                updateValidity()
            }.show(childFragmentManager, "grade_picker")
        }

        binding.tvSemester.setOnClickListener {
            ListPickerBottomSheet.newInstance(
                title = getString(R.string.picker_semester),
                items = semesters,
                selectedItem = selectedSemester
            ) { item ->
                selectedSemester = item
                binding.tvSemester.text = item
                binding.tvSemester.setBackgroundResource(R.drawable.bg_input_field_active)
                viewModel.selectedSemesterText = item
                updateValidity()
            }.show(childFragmentManager, "semester_picker")
        }

        binding.tvEntranceYear.setOnClickListener {
            ListPickerBottomSheet.newInstance(
                title = getString(R.string.picker_entrance_year),
                items = entranceYears,
                selectedItem = selectedEntranceYear
            ) { item ->
                selectedEntranceYear = item
                binding.tvEntranceYear.text = item
                binding.tvEntranceYear.setBackgroundResource(R.drawable.bg_input_field_active)
                viewModel.selectedEntranceYearText = item
                updateValidity()
            }.show(childFragmentManager, "entrance_year_picker")
        }

        binding.tvGraduation.setOnClickListener {
            ListPickerBottomSheet.newInstance(
                title = getString(R.string.picker_graduation),
                items = graduationTerms,
                selectedItem = selectedGraduation
            ) { item ->
                if (item == "선택 안 함") {
                    selectedGraduation = null
                    binding.tvGraduation.text = ""
                    binding.tvGraduation.setBackgroundResource(R.drawable.bg_input_field)
                    viewModel.graduationTerm = null
                } else {
                    selectedGraduation = item
                    binding.tvGraduation.text = item
                    binding.tvGraduation.setBackgroundResource(R.drawable.bg_input_field_active)
                    viewModel.graduationTerm = item
                }
            }.show(childFragmentManager, "graduation_picker")
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.step1Valid.collect { valid ->
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

    private fun setupListeners() {
        binding.etName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val name = s.toString()
                applyNameFieldState(name)
                updateValidity()
            }
        })

        binding.btnNext.setOnClickListener {
            findNavController().navigate(R.id.action_step1_to_step2)
        }
    }

    private fun applyNameFieldState(name: String) {
        when {
            name.isEmpty() -> {
                binding.etName.setBackgroundResource(R.drawable.bg_input_field)
                binding.tvNameError.visibility = View.GONE
            }
            !viewModel.isNameValid(name) -> {
                binding.etName.setBackgroundResource(R.drawable.bg_input_field_error)
                binding.tvNameError.visibility = View.VISIBLE
            }
            else -> {
                binding.etName.setBackgroundResource(R.drawable.bg_input_field_active)
                binding.tvNameError.visibility = View.GONE
            }
        }
    }

    private fun updateValidity() {
        val name = binding.etName.text.toString()
        val gradeIndex = if (selectedGrade != null) {
            selectedGrade!!.replace("학년", "").toIntOrNull() ?: 0
        } else 0
        val semesterIndex = if (selectedSemester != null) 1 else 0
        val entranceYear = selectedEntranceYear?.toIntOrNull() ?: 0

        viewModel.onStep1Changed(name, gradeIndex, semesterIndex, entranceYear)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
