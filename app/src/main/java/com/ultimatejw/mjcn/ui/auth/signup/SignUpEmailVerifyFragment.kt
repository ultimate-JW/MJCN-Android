package com.ultimatejw.mjcn.ui.auth.signup
import dagger.hilt.android.AndroidEntryPoint

import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.ultimatejw.mjcn.R
import com.ultimatejw.mjcn.databinding.FragmentSignupEmailVerifyBinding

@AndroidEntryPoint
class SignUpEmailVerifyFragment : Fragment() {

    private var _binding: FragmentSignupEmailVerifyBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SignUpViewModel by activityViewModels()

    // 임시 인증 코드 : 1234
    private val VALID_CODE = "1234"

    private var countDownTimer: CountDownTimer? = null
    private var isTimerExpired = false

    companion object {
        private const val TIMER_MILLIS = 30 * 1000L  // 30초 타이머
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignupEmailVerifyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        startTimer()
    }

    private fun setupListeners() {
        binding.etCode.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val code = s.toString().trim()
                val isValid = !isTimerExpired && code == VALID_CODE
                updateNextButton(isValid)
                when {
                    code.isEmpty() -> hideCodeError()
                    isTimerExpired -> {
                        showCodeError("인증 코드가 만료되었습니다. 다시 요청해주세요.")
                        setInputErrorStyle()
                    }
                    code != VALID_CODE -> {
                        showCodeError("인증 코드가 일치하지 않습니다.")
                        setInputErrorStyle()
                    }
                    else -> hideCodeError()
                }
            }
        })

        binding.tvResend.setOnClickListener {
            binding.etCode.text.clear()
            hideCodeError()
            isTimerExpired = false
            updateNextButton(false)
            startTimer()
        }

        binding.btnNext.setOnClickListener {
            findNavController().navigate(R.id.action_signUpEmailVerify_to_step1)
        }
    }

    private fun startTimer() {
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(TIMER_MILLIS, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = millisUntilFinished / 1000 / 60
                val seconds = millisUntilFinished / 1000 % 60
                binding.tvTimer.text = String.format("%d:%02d", minutes, seconds)
            }

            override fun onFinish() {
                binding.tvTimer.text = "0:00"
                isTimerExpired = true
                // 타이머 만료 시 코드가 입력되어 있으면 에러
                val code = binding.etCode.text.toString().trim()
                if (code.isNotEmpty()) {
                    showCodeError("인증 코드가 만료되었습니다. 다시 요청해주세요.")
                    setInputErrorStyle()
                }
            }
        }.start()
    }

    private fun updateNextButton(hasInput: Boolean) {
        binding.btnNext.isEnabled = hasInput
        if (hasInput) {
            binding.btnNext.setBackgroundResource(R.drawable.bg_btn_primary)
            binding.btnNext.setTextColor(requireContext().getColor(R.color.white))
        } else {
            binding.btnNext.setBackgroundResource(R.drawable.bg_btn_disabled)
            binding.btnNext.setTextColor(requireContext().getColor(R.color.text_disabled))
        }
    }

    private fun showCodeError(msg: String) {
        binding.tvCodeError.text = msg
        binding.tvCodeError.visibility = View.VISIBLE
    }

    private fun hideCodeError() {
        binding.tvCodeError.visibility = View.GONE
        binding.etCode.setBackgroundResource(R.drawable.bg_input_field)
    }

    private fun setInputErrorStyle() {
        binding.etCode.setBackgroundResource(R.drawable.bg_input_field_error)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        countDownTimer?.cancel()
        countDownTimer = null
        _binding = null
    }
}