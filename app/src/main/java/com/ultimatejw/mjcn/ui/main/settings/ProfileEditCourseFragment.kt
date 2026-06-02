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
import com.ultimatejw.mjcn.databinding.FragmentSignupStep4Binding
import com.ultimatejw.mjcn.ui.auth.signup.CHAPEL_COURSE_NAME
import com.ultimatejw.mjcn.ui.auth.signup.Course
import com.ultimatejw.mjcn.ui.auth.signup.CourseAdapter
import com.ultimatejw.mjcn.ui.auth.signup.SelectedCourseAdapter
import com.ultimatejw.mjcn.ui.auth.signup.applyTabStyle
import com.ultimatejw.mjcn.ui.auth.signup.filterByTab
import com.ultimatejw.mjcn.ui.common.ListPickerBottomSheet
import com.ultimatejw.mjcn.ui.common.LoadingDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileEditCourseFragment : Fragment() {

    private var _binding: FragmentSignupStep4Binding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileEditViewModel by viewModels()

    private lateinit var courseAdapter: CourseAdapter
    private lateinit var chipAdapter: SelectedCourseAdapter

    private var isMajorTab = true
    private var fullCourseList: List<com.ultimatejw.mjcn.ui.auth.signup.Course> = emptyList()

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

        binding.tvTitle.text = "수강 이력 변경"
        binding.tvDesc.text = "수강한 과목을 선택해주세요"
        binding.btnNext.text = "저장"

        hidePrevButton()
        setupRecyclers()
        setupSearch()
        setupButtons()
        setupTabs()
        observeViewModel()
        refreshLists()

        viewModel.loadCourseHistory()
        viewModel.onCourseQueryChanged("")
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
        courseAdapter.submit(fullCourseList.filterByTab(isMajorTab))
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
                viewModel.toggleCourseHistory(course.name, course.meta, course.code)
                refreshLists()
            },
            onYearClick = { course -> showYearPicker(course) },
            onSemesterClick = { course -> showSemesterPicker(course) },
            onGradeClick = { course -> showGradePicker(course) },
            selectionProvider = { course -> viewModel.findCourseHistory(course.name) }
        )
        binding.rvCourses.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCourses.adapter = courseAdapter

        chipAdapter = SelectedCourseAdapter(
            onRemove = { selected ->
                viewModel.toggleCourseHistory(selected.name, selected.meta, selected.courseCode)
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
        binding.btnNext.isEnabled = true
        binding.btnNext.setBackgroundResource(R.drawable.bg_btn_primary)
        binding.btnNext.setTextColor(requireContext().getColor(R.color.white))
        binding.btnNext.setOnClickListener {
            viewModel.saveCourseHistory()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.courseSearchResults.collect { list ->
                        fullCourseList = list
                        applyFilter()
                    }
                }
                launch {
                    viewModel.courseHistoryLoaded.collect { loaded ->
                        if (loaded) refreshLists()
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

    private fun showYearPicker(course: Course) {
        val years = (2026 downTo 2015).map { "$it" }
        val current = viewModel.findCourseHistory(course.name)?.year?.toString()
        ListPickerBottomSheet.newInstance(
            title = getString(R.string.picker_year_select),
            items = years,
            selectedItem = current
        ) { selected ->
            viewModel.setCourseHistoryYear(course.name, selected.toInt())
            courseAdapter.notifyDataSetChanged()
            updateNextButton()
        }.show(childFragmentManager, "year_picker")
    }

    private fun showSemesterPicker(course: Course) {
        val semesters = listOf("1학기", "2학기")
        val current = when (viewModel.findCourseHistory(course.name)?.semester) {
            1 -> "1학기"
            2 -> "2학기"
            else -> null
        }
        ListPickerBottomSheet.newInstance(
            title = getString(R.string.picker_semester_select),
            items = semesters,
            selectedItem = current
        ) { selected ->
            viewModel.setCourseHistorySemester(course.name, if (selected == "1학기") 1 else 2)
            courseAdapter.notifyDataSetChanged()
            updateNextButton()
        }.show(childFragmentManager, "semester_picker")
    }

    private fun showGradePicker(course: Course) {
        val isChapel = course.name == CHAPEL_COURSE_NAME
        val items = if (isChapel) listOf("1", "2", "3", "4")
        else listOf("A+", "A0", "B+", "B0", "C+", "C0", "D+", "D0", "F", "P")
        val title = getString(if (isChapel) R.string.picker_chapel_count else R.string.picker_grade_select)
        val current = viewModel.findCourseHistory(course.name)?.grade
        ListPickerBottomSheet.newInstance(title, items, current) { selected ->
            viewModel.setCourseHistoryGrade(course.name, selected)
            courseAdapter.notifyDataSetChanged()
            updateNextButton()
        }.show(childFragmentManager, "grade_picker")
    }

    private fun refreshLists() {
        val chips = viewModel.selectedCourseHistory.toList()
        binding.rvSelectedCourses.visibility = if (chips.isEmpty()) View.GONE else View.VISIBLE
        chipAdapter.submit(chips)
        courseAdapter.notifyDataSetChanged()
        updateNextButton()
    }

    private fun updateNextButton() {
        val allValid = viewModel.selectedCourseHistory.all { course ->
            if (course.name == CHAPEL_COURSE_NAME) !course.grade.isNullOrBlank()
            else course.year != null && course.semester != null
        }
        val enabled = allValid
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
        LoadingDialog.hide(childFragmentManager)
        super.onDestroyView()
        _binding = null
    }
}
