package com.ultimatejw.mjcn.ui.main.theme
import dagger.hilt.android.AndroidEntryPoint

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ultimatejw.mjcn.databinding.FragmentThemeDetailBinding

@AndroidEntryPoint
class ThemeDetailFragment : Fragment() {

    private var _binding: FragmentThemeDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentThemeDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val themeId = arguments?.getString("themeId") ?: return
        // TODO: themeId로 상세 데이터 로드
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
