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
import com.ultimatejw.mjcn.databinding.FragmentSignupIdPwBinding

@AndroidEntryPoint
class SignUpIdPwFragment : Fragment() {

    private var _binding: FragmentSignupIdPwBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SignUpViewModel by activityViewModels()

    // 임시 중복 이메일
    private val DUPLICATE_EMAIL = "pppp@test.com"

    private var isPasswordVisible = false
    private var isPasswordConfirmVisible = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignupIdPwBinding.inflate(inflater, container, false)
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
        // 아이디(이메일) 입력 감지
        binding.etEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                applyEmailFieldState()
                validateAndNotify()
            }
        })

        binding.etEmail.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) applyEmailFieldState()
        }

        // 비밀번호 입력 감지
        binding.etPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                applyPasswordFieldState()
                applyPasswordConfirmFieldState()
                validateAndNotify()
            }
        })

        binding.etPassword.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) applyPasswordFieldState()
        }

        binding.etPasswordConfirm.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                applyPasswordConfirmFieldState()
                validateAndNotify()
            }
        })

        binding.etPasswordConfirm.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) applyPasswordConfirmFieldState()
        }

        // 비밀번호 표시 토글
        binding.btnTogglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            setPasswordVisibility(binding.etPassword, isPasswordVisible, binding.btnTogglePassword)
        }

        // 비밀번호 확인 표시 토글
        binding.btnTogglePasswordConfirm.setOnClickListener {
            isPasswordConfirmVisible = !isPasswordConfirmVisible
            setPasswordVisibility(binding.etPasswordConfirm, isPasswordConfirmVisible, binding.btnTogglePasswordConfirm)
        }

        // 다음 버튼
        binding.btnNext.setOnClickListener {
            if (isFormValid()) {
                viewModel.email = binding.etEmail.text.toString().trim()
                viewModel.password = binding.etPassword.text.toString()
                findNavController().navigate(R.id.action_signUpIdPw_to_signUpEmailVerify)
            }
        }
    }



    private fun applyEmailFieldState() {
        val email = binding.etEmail.text.toString().trim()
        when {
            email.isEmpty() -> {
                binding.etEmail.setBackgroundResource(R.drawable.bg_input_field)
                hideEmailError()
            }
            !isEmailFormatValid(email) -> {
                binding.etEmail.setBackgroundResource(R.drawable.bg_input_field_error)
                showEmailError("올바른 이메일 형식으로 입력해주세요.")
            }
            email == DUPLICATE_EMAIL -> {
                binding.etEmail.setBackgroundResource(R.drawable.bg_input_field_error)
                showEmailError("이미 사용 중인 이메일입니다. 다른 이메일을 입력해주세요.")
            }
            else -> {
                binding.etEmail.setBackgroundResource(R.drawable.bg_input_field_active)
                hideEmailError()
            }
        }
    }

    private fun applyPasswordFieldState() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()
        when {
            password.isEmpty() -> {
                binding.flPassword.setBackgroundResource(R.drawable.bg_input_field)
                hidePasswordError()
            }
            !isPasswordFormatValid(password) -> {
                binding.flPassword.setBackgroundResource(R.drawable.bg_input_field_error)
                showPasswordError("영문 + 숫자 포함 8자~20자 이내로 입력해주세요.")
            }
            isPasswordSameAsEmail(password, email) -> {
                binding.flPassword.setBackgroundResource(R.drawable.bg_input_field_error)
                showPasswordError("이메일과 동일한 비밀번호는 사용할 수 없습니다.")
            }
            else -> {
                // [수정] 정상 입력 → 보라색 테두리 유지
                binding.flPassword.setBackgroundResource(R.drawable.bg_input_field_active)
                hidePasswordError()
            }
        }
    }

    private fun applyPasswordConfirmFieldState() {
        val password = binding.etPassword.text.toString()
        val passwordConfirm = binding.etPasswordConfirm.text.toString()
        when {
            passwordConfirm.isEmpty() -> {
                binding.flPasswordConfirm.setBackgroundResource(R.drawable.bg_input_field)
                hidePasswordConfirmError()
            }
            password != passwordConfirm -> {
                binding.flPasswordConfirm.setBackgroundResource(R.drawable.bg_input_field_error)
                showPasswordConfirmError("비밀번호가 일치하지 않습니다.")
            }
            else -> {
                binding.flPasswordConfirm.setBackgroundResource(R.drawable.bg_input_field_active)
                hidePasswordConfirmError()
            }
        }
    }


    private fun validateAndNotify() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()
        val passwordConfirm = binding.etPasswordConfirm.text.toString()

        val emailOk = isEmailFormatValid(email) && email != DUPLICATE_EMAIL
        val passwordOk = isPasswordFormatValid(password) && !isPasswordSameAsEmail(password, email)
        val confirmOk = password == passwordConfirm && passwordConfirm.isNotEmpty()

        viewModel.onStep2Changed(
            if (emailOk) email else "",
            if (passwordOk) password else "",
            if (confirmOk) passwordConfirm else ""
        )
    }

    private fun isFormValid(): Boolean {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()
        val passwordConfirm = binding.etPasswordConfirm.text.toString()
        return isEmailFormatValid(email) && email != DUPLICATE_EMAIL
                && isPasswordFormatValid(password) && !isPasswordSameAsEmail(password, email)
                && password == passwordConfirm && passwordConfirm.isNotEmpty()
    }


    private fun isEmailFormatValid(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isPasswordFormatValid(password: String): Boolean {
        val regex = Regex("^(?=.*[a-zA-Z])(?=.*[0-9]).{8,20}$")
        return regex.matches(password)
    }

    private fun isPasswordSameAsEmail(password: String, email: String): Boolean {
        return email.isNotEmpty() && password.equals(email, ignoreCase = true)
    }

    private fun setPasswordVisibility(
        editText: android.widget.EditText,
        visible: Boolean,
        toggleBtn: android.widget.ImageButton
    ) {
        val inputType = if (visible) {
            android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        } else {
            android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        editText.inputType = inputType
        editText.setSelection(editText.text.length)
        toggleBtn.setImageResource(
            if (visible) R.drawable.ic_visibility else R.drawable.ic_visibility_off
        )
    }

    private fun showEmailError(msg: String) {
        binding.tvEmailError.text = msg
        binding.tvEmailError.visibility = View.VISIBLE
    }

    private fun hideEmailError() {
        binding.tvEmailError.visibility = View.GONE
    }

    private fun showPasswordError(msg: String) {
        binding.tvPasswordError.text = msg
        binding.tvPasswordError.visibility = View.VISIBLE
    }

    private fun hidePasswordError() {
        binding.tvPasswordError.visibility = View.GONE
    }

    private fun showPasswordConfirmError(msg: String) {
        binding.tvPasswordConfirmError.text = msg
        binding.tvPasswordConfirmError.visibility = View.VISIBLE
    }

    private fun hidePasswordConfirmError() {
        binding.tvPasswordConfirmError.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}