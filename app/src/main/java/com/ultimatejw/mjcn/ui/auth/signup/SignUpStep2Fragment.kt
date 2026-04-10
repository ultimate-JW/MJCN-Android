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
import com.ultimatejw.mjcn.databinding.FragmentSignupStep2Binding

@AndroidEntryPoint
class SignUpStep2Fragment : Fragment() {

    private var _binding: FragmentSignupStep2Binding? = null
    private val binding get() = _binding!!

    private val viewModel: SignUpViewModel by activityViewModels()

    private var isPasswordVisible = false
    private var isPasswordConfirmVisible = false

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
        observeViewModel()
        setupListeners()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.step2Valid.collect { valid ->
                    binding.btnNext.isEnabled = valid
                    binding.btnNext.alpha = if (valid) 1f else 0.6f
                }
            }
        }
    }

    private fun setupListeners() {
        val updateValidity = {
            viewModel.onStep2Changed(
                binding.etEmail.text.toString(),
                binding.etPassword.text.toString(),
                binding.etPasswordConfirm.text.toString()
            )
        }

        listOf(binding.etEmail, binding.etPassword, binding.etPasswordConfirm).forEach { et ->
            et.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) { updateValidity() }
            })
        }

        binding.btnTogglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            binding.etPassword.inputType = if (isPasswordVisible) {
                android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            binding.etPassword.setSelection(binding.etPassword.text.length)
            binding.btnTogglePassword.setImageResource(
                if (isPasswordVisible) R.drawable.ic_visibility else R.drawable.ic_visibility_off
            )
        }

        binding.btnTogglePasswordConfirm.setOnClickListener {
            isPasswordConfirmVisible = !isPasswordConfirmVisible
            binding.etPasswordConfirm.inputType = if (isPasswordConfirmVisible) {
                android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            binding.etPasswordConfirm.setSelection(binding.etPasswordConfirm.text.length)
            binding.btnTogglePasswordConfirm.setImageResource(
                if (isPasswordConfirmVisible) R.drawable.ic_visibility else R.drawable.ic_visibility_off
            )
        }

        binding.btnNext.setOnClickListener {
            findNavController().navigate(R.id.action_step2_to_step3)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
