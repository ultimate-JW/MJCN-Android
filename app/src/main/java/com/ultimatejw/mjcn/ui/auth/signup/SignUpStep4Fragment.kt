package com.ultimatejw.mjcn.ui.auth.signup

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ultimatejw.mjcn.databinding.FragmentSignupStep4Binding
import com.ultimatejw.mjcn.ui.common.ListPickerBottomSheet
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignUpStep4Fragment : Fragment() {

    private var _binding: FragmentSignupStep4Binding? = null
    private val binding get() = _binding!!

    private val viewModel: SignUpViewModel by activityViewModels()

    private lateinit var courseAdapter: CourseAdapter
    private lateinit var chipAdapter: SelectedCourseAdapter

    // 전체 과목 데이터 (디자인 기준 샘플)
    private val allCourses: List<Course> by lazy { buildCourseData() }

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
        refreshLists()
    }

    private fun setupRecyclers() {
        courseAdapter = CourseAdapter(
            onAddClick = { course -> toggleCourse(course) },
            onGradeClick = { course -> showGradePicker(course) },
            selectionProvider = { name -> viewModel.findSelectedCourse(name) }
        )
        binding.rvCourses.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCourses.adapter = courseAdapter

        chipAdapter = SelectedCourseAdapter(
            onRemove = { selected -> removeCourse(selected.name) }
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
                refreshCourseList()
            }
        })
    }

    private fun setupButtons() {
        binding.btnPrev.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.btnNext.setOnClickListener {
            findNavController().navigate(com.ultimatejw.mjcn.R.id.action_step4_to_step5)
        }
    }

    /** + 버튼 클릭 시: 미선택이면 추가, 선택 상태면 해제 */
    private fun toggleCourse(course: Course) {
        if (viewModel.findSelectedCourse(course.name) == null) {
            viewModel.addSelectedCourse(course.name)
        } else {
            viewModel.removeSelectedCourse(course.name)
        }
        refreshLists()
    }

    private fun removeCourse(name: String) {
        viewModel.removeSelectedCourse(name)
        refreshLists()
    }

    private fun showGradePicker(course: Course) {
        val grades = listOf("A+", "A0", "B+", "B0", "C+", "C0", "D+", "D0", "F", "P")
        val currentGrade = viewModel.findSelectedCourse(course.name)?.grade
        ListPickerBottomSheet.newInstance(
            title = getString(com.ultimatejw.mjcn.R.string.picker_grade_select),
            items = grades,
            selectedItem = currentGrade
        ) { selected ->
            viewModel.setCourseGrade(course.name, selected)
            refreshCourseList()
        }.show(childFragmentManager, "grade_picker")
    }

    private fun refreshLists() {
        refreshChipList()
        refreshCourseList()
    }

    private fun refreshChipList() {
        val chips = viewModel.selectedCourses.toList()
        binding.rvSelectedCourses.visibility = if (chips.isEmpty()) View.GONE else View.VISIBLE
        chipAdapter.submit(chips)
    }

    private fun refreshCourseList() {
        val query = binding.etSearch.text?.toString().orEmpty().trim()
        val filtered = if (query.isEmpty()) {
            allCourses
        } else {
            allCourses.filter { it.name.contains(query, ignoreCase = true) }
        }
        courseAdapter.submit(filtered)
    }

    private fun buildCourseData(): List<Course> {
        val majorMeta = "반도체 ICT대학 · 컴퓨터정보통신공학부 · 컴퓨터공학과"
        val liberalArtsMeta = "자연캠퍼스 교양"
        return listOf(
            Course("4차산업혁명과기업가정신", majorMeta),
            Course("AI프로그래밍", majorMeta),
            Course("객체지향프로그래밍2", majorMeta),
            Course("공개SW실무", majorMeta),
            Course("그래프신경망과빅데이터", majorMeta),
            Course("기계학습", majorMeta),
            Course("데이터베이스", majorMeta),
            Course("데이터베이스설계", majorMeta),
            Course("딥러닝", majorMeta),
            Course("모바일프로그래밍", majorMeta),
            Course("블록체인", majorMeta),
            Course("소프트웨어공학", majorMeta),
            Course("영어 1", liberalArtsMeta),
            Course("영어 2", liberalArtsMeta),
            Course("영어회화 1", liberalArtsMeta),
            Course("영어회화 2", liberalArtsMeta)
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
