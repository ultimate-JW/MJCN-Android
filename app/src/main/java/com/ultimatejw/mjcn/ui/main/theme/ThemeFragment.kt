package com.ultimatejw.mjcn.ui.main.theme

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
import com.ultimatejw.mjcn.databinding.FragmentThemeBinding

class ThemeFragment : Fragment() {

    private var _binding: FragmentThemeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ThemeViewModel by viewModels()
    private lateinit var adapter: ThemeAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentThemeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = ThemeAdapter { theme ->
            findNavController().navigate(
                R.id.action_theme_to_detail,
                bundleOf("themeId" to theme.id)
            )
        }
        binding.rvThemes.layoutManager = LinearLayoutManager(requireContext())
        binding.rvThemes.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.themes.observe(viewLifecycleOwner) { themes ->
            adapter.submitList(themes)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
