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
        observeStepSave()
    }

    private fun observeStepSave() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
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
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isStepSaveLoading.collect { loading ->
                    binding.btnNext.isEnabled = !loading
                }
            }
        }
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
            // Step1~5 사용자 입력을 일괄 저장 + is_onboarding_completed=true 마킹.
            viewModel.saveAllAndCompleteOnboarding()
        }
    }

    private fun toggleCourse(course: Course) {
        if (viewModel.findCurrentCourse(course.name) == null) {
            viewModel.addCurrentCourse(course.name, course.meta)
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
        val liberalArtsMeta = "자연캠퍼스 교양"
        val code = "0727"
        return listOf(
            // 전공
            Course("기계학습", majorMeta, code),
            Course("캡스톤디자인", majorMeta, code),
            Course("시스템클라우드보안", majorMeta, code),
            Course("데이터베이스", majorMeta, code),
            // 교양
            Course("4차산업혁명과미래사회진로선택", liberalArtsMeta),
            Course("현대사회와기독교윤리", liberalArtsMeta)
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
