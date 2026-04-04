package com.ultimatejw.mjcn.ui.main.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ultimatejw.mjcn.databinding.FragmentChatDetailBinding

class ChatDetailFragment : Fragment() {

    private var _binding: FragmentChatDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val sessionId = arguments?.getString("sessionId") ?: ""
        // TODO: sessionId로 기존 대화 불러오기, 새 대화면 빈 화면
        setupSendButton()
    }

    private fun setupSendButton() {
        binding.btnSend.setOnClickListener {
            val message = binding.etMessage.text.toString()
            if (message.isBlank()) return@setOnClickListener
            binding.etMessage.text.clear()
            // TODO: AI API 호출
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
