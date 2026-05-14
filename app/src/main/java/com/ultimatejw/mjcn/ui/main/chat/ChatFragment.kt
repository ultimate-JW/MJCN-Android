package com.ultimatejw.mjcn.ui.main.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.ultimatejw.mjcn.R
import com.ultimatejw.mjcn.databinding.FragmentChatBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ChatViewModel by viewModels()
    private lateinit var adapter: ChatSessionAdapter

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
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        setupRecyclerView()
        setupListeners()
        observeUiState()
    }

    private fun setupRecyclerView() {
        adapter = ChatSessionAdapter { session ->
            findNavController().navigate(R.id.action_chat_to_detail, bundleOf("sessionId" to session.id))
        }
        binding.rvChatHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.rvChatHistory.adapter = adapter
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
        binding.chipGroupCategory.setOnCheckedStateChangeListener { group, checkedIds ->
            val category = checkedIds.firstOrNull()
                ?.let { group.findViewById<Chip>(it)?.text?.toString() }
                ?: "전체"
            viewModel.selectCategory(category)
        }
    }

    private fun observeUiState() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            val isEmpty = state.sessions.isEmpty()
            binding.scrollChips.isVisible = !isEmpty
            binding.tvChatEmpty.isVisible = isEmpty
            adapter.submitList(state.sessions)
        }
    }

    override fun onResume() {
        super.onResume()
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
    }

    override fun onPause() {
        super.onPause()
        @Suppress("DEPRECATION")
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
