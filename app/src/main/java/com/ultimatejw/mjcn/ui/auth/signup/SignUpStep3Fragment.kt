package com.ultimatejw.mjcn.ui.auth.signup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.ultimatejw.mjcn.R
import com.ultimatejw.mjcn.databinding.FragmentSignupStep3Binding
import com.ultimatejw.mjcn.utils.visible
import com.ultimatejw.mjcn.utils.gone

class SignUpStep3Fragment : Fragment() {

    private var _binding: FragmentSignupStep3Binding? = null
    private val binding get() = _binding!!

    private val viewModel: SignUpViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignupStep3Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
        setupChipListeners()
        setupListeners()
    }

    private fun observeViewModel() {
        viewModel.step3Valid.observe(viewLifecycleOwner) { valid ->
            binding.btnNext.isEnabled = valid
            binding.btnNext.alpha = if (valid) 1f else 0.6f
        }
    }

    private fun setupChipListeners() {
        for (i in 0 until binding.chipGroupInterests.childCount) {
            val chip = binding.chipGroupInterests.getChildAt(i) as? Chip ?: continue
            chip.setOnCheckedChangeListener { _, isChecked ->
                val interest = chip.text.toString()
                if (interest == "기타(직접 입력)") {
                    if (isChecked) binding.etOtherInterest.visible()
                    else binding.etOtherInterest.gone()
                }
                viewModel.onInterestToggled(interest, isChecked)
            }
        }
    }

    private fun setupListeners() {
        binding.btnNext.setOnClickListener {
            findNavController().navigate(R.id.action_step3_to_onboarding)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
