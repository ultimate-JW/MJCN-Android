package com.ultimatejw.mjcn.ui.auth.signup
import dagger.hilt.android.AndroidEntryPoint

import android.graphics.Paint
import android.os.Bundle
import android.os.CountDownTimer
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.ultimatejw.mjcn.R
import com.ultimatejw.mjcn.databinding.FragmentSignupEmailVerifyBinding

@AndroidEntryPoint
class SignUpEmailVerifyFragment : Fragment() {

    private var _binding: FragmentSignupEmailVerifyBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SignUpViewModel by activityViewModels()

    private var countDownTimer: CountDownTimer? = null
    private var isTimerExpired = false

    companion object {
        // 인증코드 유효 시간: 3분
        private const val TIMER_MILLIS = 3 * 60 * 1000L
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
        observeViewModel()
        startTimer()
    }

    private fun setupListeners() {
        // 입력만 있으면 다음 버튼 활성화 (실제 검증은 다음 클릭 시 서버로)
        binding.etCode.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val code = s.toString().trim()
                // 입력이 바뀌면 이전 에러 상태 초기화
                hideCodeError()
                updateNextButton(code.isNotEmpty() && !viewModel.isVerifyLoading.value)
            }
        })

        binding.tvResend.setOnClickListener {
            flashResendUnderline()
            viewModel.resendVerification()
        }

        binding.btnNext.setOnClickListener {
            val code = binding.etCode.text.toString().trim()
            if (code.isEmpty()) return@setOnClickListener
            if (isTimerExpired) {
                showCodeError("인증 코드가 만료되었습니다. 다시 요청해주세요.")
                setInputErrorStyle()
                return@setOnClickListener
            }
            viewModel.verifyEmail(code)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isVerifyLoading.collect { loading ->
                    val hasInput = binding.etCode.text.toString().trim().isNotEmpty()
                    updateNextButton(hasInput && !loading)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.verifyResult.collect { result ->
                    when (result) {
                        is VerifyEmailResult.Success -> {
                            countDownTimer?.cancel()
                            findNavController().navigate(R.id.action_signUpEmailVerify_to_step1)
                        }
                        is VerifyEmailResult.Failure -> {
                            showCodeError(result.message)
                            setInputErrorStyle()
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.resendResult.collect { result ->
                    when (result) {
                        is ResendResult.Success -> {
                            binding.etCode.text.clear()
                            hideCodeError()
                            isTimerExpired = false
                            updateNextButton(false)
                            startTimer()
                        }
                        is ResendResult.Failure -> {
                            showCodeError(result.message)
                        }
                    }
                }
            }
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
                val code = binding.etCode.text.toString().trim()
                if (code.isNotEmpty()) {
                    showCodeError("인증 코드가 만료되었습니다. 다시 요청해주세요.")
                    setInputErrorStyle()
                }
            }
        }.start()
    }

    private fun flashResendUnderline() {
        val tv = binding.tvResend
        tv.paintFlags = tv.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        viewLifecycleOwner.lifecycleScope.launch {
            delay(300)
            _binding?.tvResend?.let { it.paintFlags = it.paintFlags and Paint.UNDERLINE_TEXT_FLAG.inv() }
        }
    }

    private fun updateNextButton(enabled: Boolean) {
        binding.btnNext.isEnabled = enabled
        if (enabled) {
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
