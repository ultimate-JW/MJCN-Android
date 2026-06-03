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
import com.ultimatejw.mjcn.ui.auth.signup.SelectedCourse
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
    private var fullOfferingList: List<Course> = emptyList()

    // 최신 상태 캐시 (람다에서 참조)
    private var currentSelectedIds: Set<Int> = emptySet()
    private var currentDisabledIds: Set<Int> = emptySet()

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

        viewModel.loadCurrentCourses()
    }

    private fun hidePrevButton() {
        binding.btnPrev.visibility = View.GONE
        val cs = ConstraintSet()
        cs.clone(binding.root as ConstraintLayout)
        cs.connect(R.id.btn_next, ConstraintSet.START, ConstraintLayout.LayoutParams.PARENT_ID, ConstraintSet.START, 0)
        cs.connect(R.id.rv_courses, ConstraintSet.BOTTOM, R.id.btn_next, ConstraintSet.TOP, 0)
        cs.applyTo(binding.root as ConstraintLayout)
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

    private fun setupRecyclers() {
        courseAdapter = CourseAdapter(
            onAddClick = { course ->
                val id = course.offeringId ?: return@CourseAdapter
                if (id in currentDisabledIds) return@CourseAdapter
                viewModel.toggleOffering(id)
            },
            selectionProvider = { course ->
                val id = course.offeringId ?: return@CourseAdapter null
                if (id in currentSelectedIds)
                    SelectedCourse(name = course.name, meta = course.meta, offeringId = id)
                else null
            },
            disabledProvider = { course ->
                val id = course.offeringId ?: return@CourseAdapter false
                id in currentDisabledIds
            },
            showGradeOnSelect = false
        )
        binding.rvCourses.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCourses.adapter = courseAdapter

        chipAdapter = SelectedCourseAdapter(
            onRemove = { selected ->
                selected.offeringId?.let { viewModel.toggleOffering(it) }
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
                viewModel.onCurrentCourseSearchChanged(s?.toString().orEmpty())
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

                // 전체 과목 목록 (검색 필터 포함)
                launch {
                    viewModel.offeringItems.collect { list ->
                        fullOfferingList = list
                        applyFilter()
                    }
                }

                // 선택 상태 변경 → 어댑터 갱신
                launch {
                    viewModel.selectedOfferingIds.collect { ids ->
                        currentSelectedIds = ids
                        courseAdapter.notifyDataSetChanged()
                    }
                }

                // 비활성화 상태 변경 → 어댑터 갱신
                launch {
                    viewModel.disabledOfferingIds.collect { ids ->
                        currentDisabledIds = ids
                        courseAdapter.notifyDataSetChanged()
                    }
                }

                // 선택된 과목 칩 실시간 업데이트
                launch {
                    viewModel.selectedOfferingItems.collect { items ->
                        binding.rvSelectedCourses.visibility =
                            if (items.isEmpty()) View.GONE else View.VISIBLE
                        chipAdapter.submit(items)
                    }
                }

                // 로딩 상태
                launch {
                    viewModel.currentCoursesLoading.collect { loading ->
                        if (loading) LoadingDialog.show(childFragmentManager)
                        else LoadingDialog.hide(childFragmentManager)
                    }
                }

                // 저장 중
                launch {
                    viewModel.isSaving.collect { saving ->
                        if (saving) LoadingDialog.show(childFragmentManager)
                        else LoadingDialog.hide(childFragmentManager)
                    }
                }

                // 저장 결과
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

    override fun onDestroyView() {
        LoadingDialog.hide(childFragmentManager)
        super.onDestroyView()
        _binding = null
    }
}
