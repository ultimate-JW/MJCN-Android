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
        // 서버 저장은 Step5에서 한 번에 처리. 여기는 단순 화면 전환만.
        binding.btnNext.setOnClickListener {
            findNavController().navigate(com.ultimatejw.mjcn.R.id.action_step4_to_step5)
        }
    }

    /** + 버튼 클릭 시: 미선택이면 추가, 선택 상태면 해제 */
    private fun toggleCourse(course: Course) {
        if (viewModel.findSelectedCourse(course.name) == null) {
            // meta(전공/교양 분류)를 같이 저장 → Step5 일괄 저장 시 category 결정에 사용.
            viewModel.addSelectedCourse(course.name, course.meta)
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
        val isChapel = course.name == CHAPEL_COURSE_NAME
        val items = if (isChapel) {
            listOf("1", "2", "3", "4")
        } else {
            listOf("A+", "A0", "B+", "B0", "C+", "C0", "D+", "D0", "F", "P")
        }
        val title = getString(
            if (isChapel) com.ultimatejw.mjcn.R.string.picker_chapel_count
            else com.ultimatejw.mjcn.R.string.picker_grade_select
        )
        val current = viewModel.findSelectedCourse(course.name)?.grade
        ListPickerBottomSheet.newInstance(
            title = title,
            items = items,
            selectedItem = current
        ) { selected ->
            viewModel.setCourseGrade(course.name, selected)
            refreshCourseList()
            updateNextButton()
        }.show(childFragmentManager, "grade_picker")
    }

    private fun refreshLists() {
        refreshChipList()
        refreshCourseList()
        updateNextButton()
    }

    /**
     * 채플이 선택되었지만 이수 횟수가 입력되지 않은 경우에만 다음 버튼을 비활성화한다.
     * 그 외의 모든 상태(미선택 / 선택+횟수 입력됨)에서는 활성.
     */
    private fun updateNextButton() {
        val chapel = viewModel.findSelectedCourse(CHAPEL_COURSE_NAME)
        val chapelCountMissing = chapel != null && chapel.grade.isNullOrBlank()
        val enabled = !chapelCountMissing
        binding.btnNext.isEnabled = enabled
        if (enabled) {
            binding.btnNext.setBackgroundResource(com.ultimatejw.mjcn.R.drawable.bg_btn_primary)
            binding.btnNext.setTextColor(requireContext().getColor(com.ultimatejw.mjcn.R.color.white))
        } else {
            binding.btnNext.setBackgroundResource(com.ultimatejw.mjcn.R.drawable.bg_btn_disabled)
            binding.btnNext.setTextColor(requireContext().getColor(com.ultimatejw.mjcn.R.color.text_disabled))
        }
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
