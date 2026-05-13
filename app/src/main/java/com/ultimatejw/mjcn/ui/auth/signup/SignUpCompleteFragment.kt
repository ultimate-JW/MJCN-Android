package com.ultimatejw.mjcn.ui.auth.signup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ultimatejw.mjcn.databinding.FragmentSignupCompleteBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * 회원가입 완료 화면. (추후 메인화면으로 연결).
 */
@AndroidEntryPoint
class SignUpCompleteFragment : Fragment() {

    private var _binding: FragmentSignupCompleteBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignupCompleteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnStart.setOnClickListener {
            // TODO: 메인 화면 진입 등 후속 동작 연결.
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
