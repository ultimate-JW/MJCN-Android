package com.ultimatejw.mjcn.ui.auth.signup

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ultimatejw.mjcn.R
import com.ultimatejw.mjcn.databinding.FragmentSignupStep5Binding
import com.ultimatejw.mjcn.utils.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SignUpStep5Fragment : Fragment() {

    private var _binding: FragmentSignupStep5Binding? = null
    private val binding get() = _binding!!

    private val viewModel: SignUpViewModel by activityViewModels()

    private lateinit var courseAdapter: CourseAdapter
    private lateinit var chipAdapter: SelectedCourseAdapter

    private var isMajorTab = true
    private var fullOfferingList: List<Course> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignupStep5Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclers()
        setupSearch()
        setupButtons()
        setupTabs()
        observeViewModel()
        refreshLists()
        viewModel.onOfferingQueryChanged("")
    }

    private fun setupRecyclers() {
        courseAdapter = CourseAdapter(
            onAddClick = { course ->
                val id = course.offeringId ?: return@CourseAdapter
                val isSelected = viewModel.findCurrentCourseByOfferingId(id) != null
                if (!isSelected) {
                    val sameCourseSelected = viewModel.selectedCurrentCourses
                        .any { it.name == course.name && it.offeringId != null }
                    if (sameCourseSelected || viewModel.hasTimeConflict(id)) return@CourseAdapter
                }
                if (viewModel.findCurrentCourseByOfferingId(id) == null) {
                    viewModel.addCurrentCourse(course.name, course.meta, id)
                } else {
                    viewModel.removeCurrentCourse(course.name, id)
                }
                refreshLists()
            },
            selectionProvider = { course ->
                if (course.offeringId != null) viewModel.findCurrentCourseByOfferingId(course.offeringId)
                else viewModel.findCurrentCourse(course.name)
            },
            disabledProvider = { course ->
                val id = course.offeringId ?: return@CourseAdapter false
                val isSelected = viewModel.findCurrentCourseByOfferingId(id) != null
                if (isSelected) false
                else {
                    val sameCourseSelected = viewModel.selectedCurrentCourses
                        .any { it.name == course.name && it.offeringId != null }
                    sameCourseSelected || viewModel.hasTimeConflict(id)
                }
            },
            showGradeOnSelect = false
        )
        binding.rvCourses.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCourses.adapter = courseAdapter

        chipAdapter = SelectedCourseAdapter(
            onRemove = { selected ->
                viewModel.removeCurrentCourse(selected.name, selected.offeringId)
                refreshLists()
            }
        )
        binding.rvSelectedCourses.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvSelectedCourses.adapter = chipAdapter
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.onOfferingQueryChanged(s?.toString().orEmpty())
            }
        })
    }

    private fun setupTabs() {
        applyTabStyle(
            majorTab = binding.tabMajor, majorText = binding.tvTabMajor, majorIndicator = binding.indicatorMajor,
            liberalTab = binding.tabLiberal, liberalText = binding.tvTabLiberal, liberalIndicator = binding.indicatorLiberal,
            isMajorSelected = isMajorTab,
            onMajor = { isMajorTab = true; setupTabs(); courseAdapter.submit(fullOfferingList.filterByTab(true)) },
            onLiberal = { isMajorTab = false; setupTabs(); courseAdapter.submit(fullOfferingList.filterByTab(false)) }
        )
    }

    private fun setupButtons() {
        binding.btnPrev.setOnClickListener { findNavController().popBackStack() }
        binding.btnNext.setOnClickListener {
            viewModel.saveAllAndCompleteOnboarding()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.offeringSearchResults.collect { list ->
                        fullOfferingList = list
                        courseAdapter.submit(list.filterByTab(isMajorTab))
                    }
                }
                launch {
                    viewModel.stepSaveResult.collect { result ->
                        when (result) {
                            is StepSaveResult.Success -> if (result.step == 5) {
                                findNavController().navigate(R.id.action_step5_to_complete)
                            }
                            is StepSaveResult.Failure -> if (result.step == 5) {
                                showToast(result.message)
                                binding.btnNext.isEnabled = true
                            }
                        }
                    }
                }
                launch {
                    viewModel.isStepSaveLoading.collect { loading ->
                        binding.btnNext.isEnabled = !loading
                    }
                }
            }
        }
    }

    private fun refreshLists() {
        val chips = viewModel.selectedCurrentCourses.toList()
        binding.rvSelectedCourses.visibility = if (chips.isEmpty()) View.GONE else View.VISIBLE
        chipAdapter.submit(chips)
        courseAdapter.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
