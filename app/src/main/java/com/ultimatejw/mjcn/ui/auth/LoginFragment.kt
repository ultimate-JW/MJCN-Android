package com.ultimatejw.mjcn.ui.auth

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.ultimatejw.mjcn.R
import com.ultimatejw.mjcn.data.local.MjcnDatabase
import com.ultimatejw.mjcn.data.repository.UserRepository
import com.ultimatejw.mjcn.databinding.FragmentLoginBinding
import com.ultimatejw.mjcn.ui.main.MainActivity
import com.ultimatejw.mjcn.utils.showToast

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LoginViewModel by viewModels {
        val db = MjcnDatabase.getInstance(requireContext())
        LoginViewModelFactory(UserRepository(requireContext(), db.userDao()))
    }

    private var isPasswordVisible = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
        setupListeners()
    }

    private fun observeViewModel() {
        viewModel.loginState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is LoginState.Loading -> binding.btnLogin.isEnabled = false
                is LoginState.Success -> navigateToMain()
                is LoginState.Error -> {
                    binding.btnLogin.isEnabled = true
                    showToast(state.message)
                }
            }
        }
    }

    private fun setupListeners() {
        binding.etEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.onEmailChanged(s.toString())
            }
        })

        binding.etPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.onPasswordChanged(s.toString())
            }
        })

        binding.btnTogglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            val inputType = if (isPasswordVisible) {
                android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            binding.etPassword.inputType = inputType
            binding.etPassword.setSelection(binding.etPassword.text.length)
            binding.btnTogglePassword.setImageResource(
                if (isPasswordVisible) R.drawable.ic_visibility else R.drawable.ic_visibility_off
            )
        }

        binding.btnLogin.setOnClickListener {
            navigateToMain()
        }

        binding.btnKakaoLogin.setOnClickListener {
            // TODO: 카카오 로그인 연동
            showToast("카카오 로그인 준비 중입니다.")
        }

        binding.tvSignUp.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_signup)
        }

        binding.tvForgotPassword.setOnClickListener {
            // TODO: 비밀번호 찾기
            showToast("비밀번호 찾기 준비 중입니다.")
        }
    }

    private fun navigateToMain() {
        val intent = Intent(requireContext(), MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
