package com.ultimatejw.mjcn.ui.main.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ultimatejw.mjcn.R
import com.ultimatejw.mjcn.databinding.FragmentChatBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ChatViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupListeners()
    }

    private fun setupRecyclerView() {
        binding.rvChatHistory.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupListeners() {
        binding.btnSend.setOnClickListener {
            findNavController().navigate(R.id.action_chat_to_detail, bundleOf("sessionId" to ""))
        }
        binding.tvSuggestion1.setOnClickListener {
            findNavController().navigate(R.id.action_chat_to_detail, bundleOf("sessionId" to ""))
        }
        binding.tvSuggestion2.setOnClickListener {
            findNavController().navigate(R.id.action_chat_to_detail, bundleOf("sessionId" to ""))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
