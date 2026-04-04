package com.ultimatejw.mjcn.ui.main.notice

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
import com.ultimatejw.mjcn.data.local.MjcnDatabase
import com.ultimatejw.mjcn.data.repository.NoticeRepository
import com.ultimatejw.mjcn.databinding.FragmentNoticeBinding

class NoticeFragment : Fragment() {

    private var _binding: FragmentNoticeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NoticeViewModel by viewModels {
        val db = MjcnDatabase.getInstance(requireContext())
        NoticeViewModelFactory(NoticeRepository(db.noticeDao()))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNoticeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvNotices.layoutManager = LinearLayoutManager(requireContext())
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.allNotices.observe(viewLifecycleOwner) { notices ->
            // TODO: adapter.submitList(notices)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
