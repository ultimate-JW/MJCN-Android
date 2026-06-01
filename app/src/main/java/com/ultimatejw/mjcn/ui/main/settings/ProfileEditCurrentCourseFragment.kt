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
import com.ultimatejw.mjcn.ui.auth.signup.Course
import com.ultimatejw.mjcn.ui.auth.signup.CourseAdapter
import com.ultimatejw.mjcn.ui.auth.signup.SelectedCourseAdapter
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
        observeViewModel()
        refreshLists()
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
                viewModel.toggleCurrentCourse(course.name, course.meta)
                refreshLists()
            },
            selectionProvider = { name -> viewModel.findCurrentCourse(name) },
            showGradeOnSelect = false
        )
        binding.rvCourses.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCourses.adapter = courseAdapter

        chipAdapter = SelectedCourseAdapter(
            onRemove = { selected ->
                viewModel.toggleCurrentCourse(selected.name, selected.meta)
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
            override fun afterTextChanged(s: Editable?) { refreshCourseList() }
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
        refreshCourseList()
    }

    private fun refreshCourseList() {
        val query = binding.etSearch.text?.toString().orEmpty().trim()
        val filtered = if (query.isEmpty()) allCourses
        else allCourses.filter { it.name.contains(query, ignoreCase = true) }
        courseAdapter.submit(filtered)
    }

    private fun buildCourseData(): List<Course> {
        val majorMeta = "반도체 ICT대학 · 컴퓨터정보통신공학부 · 컴퓨터공학과"
        val liberalArtsMeta = "자연캠퍼스 교양"
        return listOf(
            Course("C언어프로그래밍", majorMeta),
            Course("공학입문설계", majorMeta),
            Course("객체지향프로그래밍1", majorMeta),
            Course("객체지향프로그래밍2", majorMeta),
            Course("컴퓨터하드웨어", majorMeta),
            Course("자료구조", majorMeta),
            Course("웹프로그래밍", majorMeta),
            Course("팀프로젝트1", majorMeta),
            Course("컴퓨터 보안", majorMeta),
            Course("컴퓨터교육론", majorMeta),
            Course("운영체제", majorMeta),
            Course("소프트웨어공학", majorMeta),
            Course("공개SW실무", majorMeta),
            Course("알고리즘", majorMeta),
            Course("시스템프로그래밍", majorMeta),
            Course("프로그래밍언어", majorMeta),
            Course("모바일프로그래밍", majorMeta),
            Course("기계학습", majorMeta),
            Course("캡스톤디자인", majorMeta),
            Course("데이터베이스", majorMeta),
            Course("채플", liberalArtsMeta),
            Course("기초미적분학", liberalArtsMeta),
            Course("영어1", liberalArtsMeta),
            Course("영어2", liberalArtsMeta),
            Course("영어회화1", liberalArtsMeta),
            Course("영어회화2", liberalArtsMeta),
            Course("물리학1", liberalArtsMeta),
            Course("물리학실험1", liberalArtsMeta),
            Course("미적분학1", liberalArtsMeta),
            Course("통계학개론", liberalArtsMeta),
            Course("기독교와문화", liberalArtsMeta),
            Course("이산수학개론", liberalArtsMeta),
            Course("예술과창조성", liberalArtsMeta),
            Course("발표와토의", liberalArtsMeta),
            Course("공학수학1", liberalArtsMeta),
            Course("파이썬프로그래밍입문", liberalArtsMeta),
            Course("파이썬을활용한데이터분석과인공지능", liberalArtsMeta),
            Course("선형대수학개론", liberalArtsMeta),
            Course("세계화와사회변화", liberalArtsMeta),
            Course("성서와인간이해", liberalArtsMeta),
            Course("철학과인간", liberalArtsMeta),
            Course("현대사회와기독교윤리", liberalArtsMeta)
        )
    }

    override fun onDestroyView() {
        LoadingDialog.hide(childFragmentManager)
        super.onDestroyView()
        _binding = null
    }
}
