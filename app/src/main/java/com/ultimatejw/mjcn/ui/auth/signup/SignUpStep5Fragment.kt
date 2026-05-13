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
import com.ultimatejw.mjcn.R
import com.ultimatejw.mjcn.databinding.FragmentSignupStep5Binding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignUpStep5Fragment : Fragment() {

    private var _binding: FragmentSignupStep5Binding? = null
    private val binding get() = _binding!!

    private val viewModel: SignUpViewModel by activityViewModels()

    private lateinit var courseAdapter: CourseAdapter
    private lateinit var chipAdapter: SelectedCourseAdapter

    private val allCourses: List<Course> by lazy { buildCourseData() }

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
        refreshLists()
    }

    private fun setupRecyclers() {
        courseAdapter = CourseAdapter(
            onAddClick = { course -> toggleCourse(course) },
            selectionProvider = { name -> viewModel.findCurrentCourse(name) },
            showGradeOnSelect = false
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
            findNavController().navigate(R.id.action_step5_to_complete)
        }
    }

    private fun toggleCourse(course: Course) {
        if (viewModel.findCurrentCourse(course.name) == null) {
            viewModel.addCurrentCourse(course.name)
        } else {
            viewModel.removeCurrentCourse(course.name)
        }
        refreshLists()
    }

    private fun removeCourse(name: String) {
        viewModel.removeCurrentCourse(name)
        refreshLists()
    }

    private fun refreshLists() {
        refreshChipList()
        refreshCourseList()
    }

    private fun refreshChipList() {
        val chips = viewModel.selectedCurrentCourses.toList()
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
        val code = "0727"
        return listOf(
            Course("4차산업혁명과기업가정신", majorMeta, code),
            Course("AI프로그래밍", majorMeta, code),
            Course("객체지향프로그래밍2", majorMeta, code),
            Course("공개SW실무", majorMeta, code),
            Course("그래프신경망과빅데이터", majorMeta, code),
            Course("기계학습", majorMeta, code),
            Course("데이터베이스", majorMeta, code),
            Course("데이터베이스설계", majorMeta, code),
            Course("딥러닝", majorMeta, code),
            Course("모바일프로그래밍", majorMeta, code),
            Course("블록체인", majorMeta, code),
            Course("소프트웨어공학", majorMeta, code)
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
