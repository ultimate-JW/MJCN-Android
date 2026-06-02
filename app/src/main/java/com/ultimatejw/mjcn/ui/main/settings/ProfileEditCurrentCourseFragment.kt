package com.ultimatejw.mjcn.ui.main.settings

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ultimatejw.mjcn.R
import com.ultimatejw.mjcn.databinding.FragmentSignupStep5Binding
import com.ultimatejw.mjcn.ui.auth.signup.CourseAdapter
import com.ultimatejw.mjcn.ui.auth.signup.SelectedCourseAdapter
import com.ultimatejw.mjcn.ui.auth.signup.applyTabStyle
import com.ultimatejw.mjcn.ui.auth.signup.filterByTab
import com.ultimatejw.mjcn.ui.common.LoadingDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileEditCurrentCourseFragment : Fragment() {

    private var _binding: FragmentSignupStep5Binding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileEditViewModel by viewModels()

    private lateinit var courseAdapter: CourseAdapter
    private lateinit var chipAdapter: SelectedCourseAdapter

    private var isMajorTab = true
    private var fullOfferingList: List<com.ultimatejw.mjcn.ui.auth.signup.Course> = emptyList()

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

        binding.tvTitle.text = "현재 수강 과목 변경"
        binding.tvDesc.text = "현재 수강 중인 과목을 선택해주세요"
        binding.btnNext.text = "저장"
        binding.btnNext.isEnabled = true
        binding.btnNext.setBackgroundResource(R.drawable.bg_btn_primary)
        binding.btnNext.setTextColor(requireContext().getColor(R.color.white))

        hidePrevButton()
        setupRecyclers()
        setupSearch()
        setupButtons()
        setupTabs()
        observeViewModel()
        refreshLists()

        viewModel.onOfferingQueryChanged("")
    }

    private fun setupTabs() {
        applyTabStyle(
            majorTab = binding.tabMajor, majorText = binding.tvTabMajor, majorIndicator = binding.indicatorMajor,
            liberalTab = binding.tabLiberal, liberalText = binding.tvTabLiberal, liberalIndicator = binding.indicatorLiberal,
            isMajorSelected = isMajorTab,
            onMajor = { isMajorTab = true; setupTabs(); applyFilter() },
            onLiberal = { isMajorTab = false; setupTabs(); applyFilter() }
        )
    }

    private fun applyFilter() {
        courseAdapter.submit(fullOfferingList.filterByTab(isMajorTab))
    }

    private fun hidePrevButton() {
        binding.btnPrev.visibility = View.GONE
        val cs = ConstraintSet()
        cs.clone(binding.root as ConstraintLayout)
        cs.connect(R.id.btn_next, ConstraintSet.START, ConstraintLayout.LayoutParams.PARENT_ID, ConstraintSet.START, 0)
        cs.connect(R.id.rv_courses, ConstraintSet.BOTTOM, R.id.btn_next, ConstraintSet.TOP, 0)
        cs.applyTo(binding.root as ConstraintLayout)
    }

    private fun setupRecyclers() {
        courseAdapter = CourseAdapter(
            onAddClick = { course ->
                viewModel.toggleCurrentCourse(course.name, course.meta, course.offeringId)
                refreshLists()
            },
            selectionProvider = { course ->
                if (course.offeringId != null) viewModel.findCurrentCourseByOfferingId(course.offeringId)
                else viewModel.findCurrentCourse(course.name)
            },
            disabledProvider = { course ->
                course.offeringId != null &&
                viewModel.selectedCurrentCourses.any { it.name == course.name } &&
                viewModel.findCurrentCourseByOfferingId(course.offeringId) == null
            },
            showGradeOnSelect = false
        )
        binding.rvCourses.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCourses.adapter = courseAdapter

        chipAdapter = SelectedCourseAdapter(
            onRemove = { selected ->
                if (selected.offeringId != null)
                    viewModel.toggleCurrentCourse(selected.name, selected.meta, selected.offeringId)
                else
                    viewModel.toggleCurrentCourse(selected.name, selected.meta, null)
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

    private fun setupButtons() {
        binding.btnNext.setOnClickListener {
            viewModel.saveCurrentCourses()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.offeringSearchResults.collect { list ->
                        fullOfferingList = list
                        applyFilter()
                    }
                }
                launch {
                    viewModel.isSaving.collect { saving ->
                        if (saving) LoadingDialog.show(childFragmentManager)
                        else LoadingDialog.hide(childFragmentManager)
                    }
                }
                launch {
                    viewModel.saveResult.collect { result ->
                        when (result) {
                            is ProfileSaveResult.Success -> findNavController().popBackStack()
                            is ProfileSaveResult.Failure -> Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
                        }
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
        LoadingDialog.hide(childFragmentManager)
        super.onDestroyView()
        _binding = null
    }
}
