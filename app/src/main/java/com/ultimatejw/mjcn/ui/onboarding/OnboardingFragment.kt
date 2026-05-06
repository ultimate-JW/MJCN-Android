package com.ultimatejw.mjcn.ui.onboarding
import dagger.hilt.android.AndroidEntryPoint

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ultimatejw.mjcn.R
import com.ultimatejw.mjcn.databinding.FragmentOnboardingBinding
import com.ultimatejw.mjcn.ui.main.MainActivity

@AndroidEntryPoint
class OnboardingFragment : Fragment() {

    private var _binding: FragmentOnboardingBinding? = null
    private val binding get() = _binding!!

    private val features: List<Pair<String, Int>> = listOf(
        Pair("🤖", R.string.onboarding_feature1),
        Pair("🔔", R.string.onboarding_feature2),
        Pair("📅", R.string.onboarding_feature3),
        Pair("🎓", R.string.onboarding_feature4),
        Pair("🏆", R.string.onboarding_feature5)
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboardingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupFeatures()
        binding.btnStart.setOnClickListener {
            val intent = Intent(requireContext(), MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun setupFeatures() {
        val featureViews = listOf(
            binding.feature1,
            binding.feature2,
            binding.feature3,
            binding.feature4,
            binding.feature5
        )
        featureViews.forEachIndexed { index, featureView ->
            val (icon, textRes) = features[index]
            featureView.tvIcon.text = icon
            featureView.tvFeature.setText(textRes)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
