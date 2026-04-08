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
import com.ultimatejw.mjcn.databinding.FragmentSignupIdPwBinding

class SignUpIdPwFragment : Fragment() {

    private var _binding: FragmentSignupIdPwBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SignUpViewModel by activityViewModels()

    // 임시 중복 이메일 (mock)
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
        viewModel.step2Valid.observe(viewLifecycleOwner) { valid ->
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

    private fun setupListeners() {
        // 아이디(이메일) 입력 감지
        binding.etEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                validateAndNotify()
            }
        })

        // 아이디 포커스 아웃 시 유효성 검사
        binding.etEmail.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) validateEmail()
        }

        // 비밀번호 입력 감지
        binding.etPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                validateAndNotify()
            }
        })

        binding.etPassword.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) validatePassword()
        }

        // 비밀번호 확인 입력 감지
        binding.etPasswordConfirm.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                validateAndNotify()
            }
        })

        binding.etPasswordConfirm.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) validatePasswordConfirm()
        }

        // 비밀번호 표시 토글
        binding.btnTogglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            setPasswordVisibility(
                binding.etPassword,
                isPasswordVisible,
                binding.btnTogglePassword
            )
        }

        // 비밀번호 확인 표시 토글
        binding.btnTogglePasswordConfirm.setOnClickListener {
            isPasswordConfirmVisible = !isPasswordConfirmVisible
            setPasswordVisibility(
                binding.etPasswordConfirm,
                isPasswordConfirmVisible,
                binding.btnTogglePasswordConfirm
            )
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

    private fun validateEmail(): Boolean {
        val email = binding.etEmail.text.toString().trim()
        return when {
            !isEmailFormatValid(email) && email.isNotEmpty() -> {
                showEmailError("올바른 이메일 형식으로 입력해주세요.")
                false
            }
            email == DUPLICATE_EMAIL -> {
                showEmailError("이미 사용 중인 이메일입니다. 다른 이메일을 입력해주세요.")
                false
            }
            else -> {
                hideEmailError()
                true
            }
        }
    }

    private fun validatePassword(): Boolean {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()
        return when {
            password.isNotEmpty() && !isPasswordFormatValid(password) -> {
                showPasswordError("영문 + 숫자 포함 8자~20자 이내로 입력해주세요.")
                false
            }
            password.isNotEmpty() && isPasswordSameAsEmail(password, email) -> {
                showPasswordError("이메일과 동일한 비밀번호는 사용할 수 없습니다.")
                false
            }
            else -> {
                hidePasswordError()
                true
            }
        }
    }

    private fun validatePasswordConfirm(): Boolean {
        val password = binding.etPassword.text.toString()
        val passwordConfirm = binding.etPasswordConfirm.text.toString()
        return when {
            passwordConfirm.isNotEmpty() && password != passwordConfirm -> {
                showPasswordConfirmError("비밀번호가 일치하지 않습니다.")
                false
            }
            else -> {
                hidePasswordConfirmError()
                true
            }
        }
    }

    private fun isFormValid(): Boolean {
        val emailValid = validateEmail()
        val passwordValid = validatePassword()
        val confirmValid = validatePasswordConfirm()
        return emailValid && passwordValid && confirmValid
    }

    // ------- 헬퍼 -------

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
        binding.etEmail.setBackgroundResource(R.drawable.bg_input_field_error)
        binding.tvEmailError.text = msg
        binding.tvEmailError.visibility = View.VISIBLE
    }

    private fun hideEmailError() {
        binding.etEmail.setBackgroundResource(R.drawable.bg_input_field)
        binding.tvEmailError.visibility = View.GONE
    }

    private fun showPasswordError(msg: String) {
        binding.flPassword.setBackgroundResource(R.drawable.bg_input_field_error)
        binding.tvPasswordError.text = msg
        binding.tvPasswordError.visibility = View.VISIBLE
    }

    private fun hidePasswordError() {
        binding.flPassword.setBackgroundResource(R.drawable.bg_input_field)
        binding.tvPasswordError.visibility = View.GONE
    }

    private fun showPasswordConfirmError(msg: String) {
        binding.flPasswordConfirm.setBackgroundResource(R.drawable.bg_input_field_error)
        binding.tvPasswordConfirmError.text = msg
        binding.tvPasswordConfirmError.visibility = View.VISIBLE
    }

    private fun hidePasswordConfirmError() {
        binding.flPasswordConfirm.setBackgroundResource(R.drawable.bg_input_field)
        binding.tvPasswordConfirmError.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
