package com.ultimatejw.mjcn.ui.main.settings

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ultimatejw.mjcn.R
import com.ultimatejw.mjcn.databinding.FragmentSignupStep4Binding
import com.ultimatejw.mjcn.ui.auth.signup.CHAPEL_COURSE_NAME
import com.ultimatejw.mjcn.ui.auth.signup.Course
import com.ultimatejw.mjcn.ui.auth.signup.CourseAdapter
import com.ultimatejw.mjcn.ui.auth.signup.SelectedCourse
import com.ultimatejw.mjcn.ui.auth.signup.SelectedCourseAdapter
import com.ultimatejw.mjcn.ui.common.ListPickerBottomSheet
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileEditCourseFragment : Fragment() {

    private var _binding: FragmentSignupStep4Binding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileEditViewModel by viewModels()

    private lateinit var courseAdapter: CourseAdapter
    private lateinit var chipAdapter: SelectedCourseAdapter

    private val selectedCourses = mutableListOf<SelectedCourse>()

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

        binding.tvTitle.text = "수강 이력 변경"
        binding.tvDesc.text = "수강한 과목을 선택해주세요"
        binding.btnNext.text = "저장"

        // Course selection is optional, always enable save
        binding.btnNext.isEnabled = true
        binding.btnNext.setBackgroundResource(R.drawable.bg_btn_primary)
        binding.btnNext.setTextColor(requireContext().getColor(R.color.white))

        hidePrevButton()
        setupRecyclers()
        setupSearch()
        setupButtons()
        refreshLists()
    }

    private fun setupRecyclers() {
        courseAdapter = CourseAdapter(
            onAddClick = { course -> toggleCourse(course) },
            onGradeClick = { course -> showGradePicker(course) },
            selectionProvider = { name -> findSelectedCourse(name) }
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

    private fun hidePrevButton() {
        binding.btnPrev.visibility = View.GONE
        val cs = ConstraintSet()
        cs.clone(binding.root as ConstraintLayout)
        cs.connect(R.id.btn_next, ConstraintSet.START, ConstraintLayout.LayoutParams.PARENT_ID, ConstraintSet.START, 0)
        cs.connect(R.id.rv_courses, ConstraintSet.BOTTOM, R.id.btn_next, ConstraintSet.TOP, 0)
        cs.applyTo(binding.root as ConstraintLayout)
    }

    private fun setupButtons() {
        binding.btnNext.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun findSelectedCourse(name: String): SelectedCourse? =
        selectedCourses.firstOrNull { it.name == name }

    private fun toggleCourse(course: Course) {
        if (findSelectedCourse(course.name) == null) {
            selectedCourses.add(SelectedCourse(name = course.name, meta = course.meta))
        } else {
            selectedCourses.removeAll { it.name == course.name }
        }
        refreshLists()
    }

    private fun removeCourse(name: String) {
        selectedCourses.removeAll { it.name == name }
        refreshLists()
    }

    private fun showGradePicker(course: Course) {
        val isChapel = course.name == CHAPEL_COURSE_NAME
        val items = if (isChapel) {
            listOf("1", "2", "3", "4")
        } else {
            listOf("A+", "A0", "B+", "B0", "C+", "C0", "D+", "D0", "F", "P")
        }
        val title = getString(
            if (isChapel) R.string.picker_chapel_count
            else R.string.picker_grade_select
        )
        val current = findSelectedCourse(course.name)?.grade
        ListPickerBottomSheet.newInstance(
            title = title,
            items = items,
            selectedItem = current
        ) { selected ->
            findSelectedCourse(course.name)?.grade = selected
            refreshCourseList()
            updateNextButton()
        }.show(childFragmentManager, "grade_picker")
    }

    private fun refreshLists() {
        refreshChipList()
        refreshCourseList()
        updateNextButton()
    }

    private fun updateNextButton() {
        val chapel = findSelectedCourse(CHAPEL_COURSE_NAME)
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

    private fun refreshChipList() {
        val chips = selectedCourses.toList()
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
            // 전공
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
            // 교양
            Course(CHAPEL_COURSE_NAME, liberalArtsMeta),
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
        super.onDestroyView()
        _binding = null
    }
}
