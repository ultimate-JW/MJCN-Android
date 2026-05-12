package com.ultimatejw.mjcn.ui.main.chat
import dagger.hilt.android.AndroidEntryPoint

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.ultimatejw.mjcn.databinding.FragmentChatDetailBinding

@AndroidEntryPoint
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
        binding.tvTitle.text = if (sessionId.isBlank()) "새 대화" else "AI 채팅"
        setupListeners()
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
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
