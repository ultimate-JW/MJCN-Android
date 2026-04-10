package com.ultimatejw.mjcn.ui.auth.signup
import dagger.hilt.android.AndroidEntryPoint

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import com.ultimatejw.mjcn.R
import com.ultimatejw.mjcn.databinding.FragmentSignupStep1Binding

@AndroidEntryPoint
class SignUpStep1Fragment : Fragment() {

    private var _binding: FragmentSignupStep1Binding? = null
    private val binding get() = _binding!!

    private val viewModel: SignUpViewModel by activityViewModels()

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
        setupSpinners()
        observeViewModel()
        setupListeners()
    }

    private fun setupSpinners() {
        val grades = listOf("학년 선택", "1학년", "2학년", "3학년", "4학년")
        val semesters = listOf("학기 선택", "1학기", "2학기")
        val years = listOf("연도 선택") + (2024..2030).map { "${it}년" }

        binding.spinnerGrade.adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_item, grades
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        binding.spinnerSemester.adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_item, semesters
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        binding.spinnerGradYear.adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_item, years
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.step1Valid.collect { valid ->
                    binding.btnNext.isEnabled = valid
                    binding.btnNext.alpha = if (valid) 1f else 0.6f
                }
            }
        }
    }

    private fun setupListeners() {
        val updateValidity = {
            val name = binding.etName.text.toString()
            val grade = binding.spinnerGrade.selectedItemPosition
            val semester = binding.spinnerSemester.selectedItemPosition
            viewModel.onStep1Changed(name, grade, semester)
        }

        binding.etName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) { updateValidity() }
        })

        binding.btnNext.setOnClickListener {
            findNavController().navigate(R.id.action_step1_to_step2)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
