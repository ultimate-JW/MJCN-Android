package com.ultimatejw.mjcn.ui.main.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.ultimatejw.mjcn.databinding.FragmentSettingsBinding
import com.ultimatejw.mjcn.ui.auth.AuthActivity
import com.ultimatejw.mjcn.ui.main.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SettingsViewModel by viewModels()
    private val mainViewModel: MainViewModel by activityViewModels()

    private var isUpdatingFromViewModel = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToggles()
        observeViewModel()
        binding.tvWithdraw.setOnClickListener {
            lifecycleScope.launch {
                mainViewModel.logout()
                val intent = Intent(requireContext(), AuthActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                requireActivity().finish()
            }
        }
    }

    private fun setupToggles() {
        binding.switchAll.onCheckedChangeListener = { checked ->
            if (!isUpdatingFromViewModel) viewModel.toggleAll(checked)
        }
        binding.switchChat.onCheckedChangeListener = { checked ->
            if (!isUpdatingFromViewModel) viewModel.toggleChat(checked)
        }
        binding.switchNotice.onCheckedChangeListener = { checked ->
            if (!isUpdatingFromViewModel) viewModel.toggleNotice(checked)
        }
        binding.switchContest.onCheckedChangeListener = { checked ->
            if (!isUpdatingFromViewModel) viewModel.toggleContest(checked)
        }
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            isUpdatingFromViewModel = true
            binding.switchAll.isChecked = state.notifAll
            binding.switchChat.isChecked = state.notifChat
            binding.switchNotice.isChecked = state.notifNotice
            binding.switchContest.isChecked = state.notifContest
            isUpdatingFromViewModel = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
