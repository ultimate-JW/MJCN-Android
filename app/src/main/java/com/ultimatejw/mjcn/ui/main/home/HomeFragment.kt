package com.ultimatejw.mjcn.ui.main.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.ultimatejw.mjcn.data.local.MjcnDatabase
import com.ultimatejw.mjcn.data.repository.NoticeRepository
import com.ultimatejw.mjcn.data.repository.UserRepository
import com.ultimatejw.mjcn.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels {
        val db = MjcnDatabase.getInstance(requireContext())
        HomeViewModelFactory(
            UserRepository(requireContext(), db.userDao()),
            NoticeRepository(db.noticeDao())
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerViews()
        observeViewModel()
    }

    private fun setupRecyclerViews() {
        binding.rvNotice.layoutManager = LinearLayoutManager(requireContext())
        binding.rvContest.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun observeViewModel() {
        viewModel.currentUser.observe(viewLifecycleOwner) { user ->
            binding.tvGreeting.text = if (user != null) {
                getString(com.ultimatejw.mjcn.R.string.home_greeting, user.name)
            } else {
                "반가워요!"
            }
        }

        viewModel.courseCount.observe(viewLifecycleOwner) { count ->
            binding.tvCourseCount.text = count.toString()
        }

        viewModel.graduationCredits.observe(viewLifecycleOwner) { credits ->
            binding.tvGraduationCredits.text = credits.toString()
        }

        viewModel.dday.observe(viewLifecycleOwner) { dday ->
            binding.tvDday.text = dday
        }

        viewModel.gradProgress.observe(viewLifecycleOwner) { progress ->
            binding.tvGradProgress.text = progress
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
