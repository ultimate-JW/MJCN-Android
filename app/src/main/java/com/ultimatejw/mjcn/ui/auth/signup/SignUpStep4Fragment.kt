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
import com.ultimatejw.mjcn.databinding.FragmentSignupStep4Binding
import com.ultimatejw.mjcn.ui.common.ListPickerBottomSheet
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SignUpStep4Fragment : Fragment() {

    private var _binding: FragmentSignupStep4Binding? = null
    private val binding get() = _binding!!

    private val viewModel: SignUpViewModel by activityViewModels()

    private lateinit var courseAdapter: CourseAdapter
    private lateinit var chipAdapter: SelectedCourseAdapter

    private var isMajorTab = true
    private var fullCourseList: List<Course> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignupStep4Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclers()
        setupSearch()
        setupButtons()
        setupTabs()
        observeCourseList()
        refreshLists()
        viewModel.onCourseQueryChanged("")
    }

    private fun setupRecyclers() {
        courseAdapter = CourseAdapter(
            onAddClick = { course ->
                if (viewModel.findSelectedCourse(course.name) == null) {
                    viewModel.addSelectedCourse(course.name, course.meta, course.code)
                } else {
                    viewModel.removeSelectedCourse(course.name)
                }
                refreshLists()
            },
            onYearClick = { course -> showYearPicker(course) },
            onSemesterClick = { course -> showSemesterPicker(course) },
            onGradeClick = { course -> showGradePicker(course) },
            selectionProvider = { course -> viewModel.findSelectedCourse(course.name) }
        )
        binding.rvCourses.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCourses.adapter = courseAdapter

        chipAdapter = SelectedCourseAdapter(
            onRemove = { selected ->
                viewModel.removeSelectedCourse(selected.name)
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
                viewModel.onCourseQueryChanged(s?.toString().orEmpty())
            }
        })
    }

    private fun setupButtons() {
        binding.btnPrev.setOnClickListener { findNavController().popBackStack() }
        binding.btnNext.setOnClickListener {
            findNavController().navigate(R.id.action_step4_to_step5)
        }
    }

    private fun setupTabs() {
        applyTabStyle(
            majorTab = binding.tabMajor, majorText = binding.tvTabMajor, majorIndicator = binding.indicatorMajor,
            liberalTab = binding.tabLiberal, liberalText = binding.tvTabLiberal, liberalIndicator = binding.indicatorLiberal,
            isMajorSelected = isMajorTab,
            onMajor = { isMajorTab = true; setupTabs(); courseAdapter.submit(fullCourseList.filterByTab(true)) },
            onLiberal = { isMajorTab = false; setupTabs(); courseAdapter.submit(fullCourseList.filterByTab(false)) }
        )
    }

    private fun observeCourseList() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.courseSearchResults.collect { list ->
                    fullCourseList = list
                    courseAdapter.submit(list.filterByTab(isMajorTab))
                }
            }
        }
    }

    private fun showYearPicker(course: Course) {
        val years = (2026 downTo 2015).map { "$it" }
        val current = viewModel.findSelectedCourse(course.name)?.year?.toString()
        ListPickerBottomSheet.newInstance(
            title = getString(R.string.picker_year_select),
            items = years,
            selectedItem = current
        ) { selected ->
            viewModel.setCourseYear(course.name, selected.toInt())
            courseAdapter.notifyDataSetChanged()
            updateNextButton()
        }.show(childFragmentManager, "year_picker")
    }

    private fun showSemesterPicker(course: Course) {
        val semesters = listOf("1학기", "2학기")
        val current = when (viewModel.findSelectedCourse(course.name)?.semester) {
            1 -> "1학기"; 2 -> "2학기"; else -> null
        }
        ListPickerBottomSheet.newInstance(
            title = getString(R.string.picker_semester_select),
            items = semesters,
            selectedItem = current
        ) { selected ->
            viewModel.setCourseSemester(course.name, if (selected == "1학기") 1 else 2)
            courseAdapter.notifyDataSetChanged()
            updateNextButton()
        }.show(childFragmentManager, "semester_picker")
    }

    private fun showGradePicker(course: Course) {
        val isChapel = course.name == CHAPEL_COURSE_NAME
        val items = if (isChapel) listOf("1", "2", "3", "4")
        else listOf("A+", "A0", "B+", "B0", "C+", "C0", "D+", "D0", "F", "P")
        val title = getString(if (isChapel) R.string.picker_chapel_count else R.string.picker_grade_select)
        val current = viewModel.findSelectedCourse(course.name)?.grade
        ListPickerBottomSheet.newInstance(title, items, current) { selected ->
            viewModel.setCourseGrade(course.name, selected)
            courseAdapter.notifyDataSetChanged()
            updateNextButton()
        }.show(childFragmentManager, "grade_picker")
    }

    private fun refreshLists() {
        val chips = viewModel.selectedCourses.toList()
        binding.rvSelectedCourses.visibility = if (chips.isEmpty()) View.GONE else View.VISIBLE
        chipAdapter.submit(chips)
        courseAdapter.notifyDataSetChanged()
        updateNextButton()
    }

    private fun updateNextButton() {
        val chapel = viewModel.findSelectedCourse(CHAPEL_COURSE_NAME)
        val chapelCountMissing = chapel != null && chapel.grade.isNullOrBlank()
        val enabled = !chapelCountMissing
        binding.btnNext.isEnabled = enabled
        if (enabled) {
            binding.btnNext.setBackgroundResource(R.drawable.bg_btn_primary)
            binding.btnNext.setTextColor(requireContext().getColor(R.color.white))
        } else {
            binding.btnNext.setBackgroundResource(R.drawable.bg_btn_disabled)
            binding.btnNext.setTextColor(requireContext().getColor(R.color.text_disabled))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
