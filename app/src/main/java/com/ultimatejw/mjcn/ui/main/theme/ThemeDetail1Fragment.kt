package com.ultimatejw.mjcn.ui.main.theme

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.ultimatejw.mjcn.R
import com.ultimatejw.mjcn.databinding.FragmentThemeDetail1Binding
import com.ultimatejw.mjcn.ui.common.CurrentUser
import dagger.hilt.android.AndroidEntryPoint

/**
 * 테마 1 수강신청 가이드.
 * AI 가 사용자 맞춤으로 채워줄 더미 데이터로 코스 추천 카드 두 개를 보여준다.
 */
@AndroidEntryPoint
class ThemeDetail1Fragment : Fragment() {

    private var _binding: FragmentThemeDetail1Binding? = null
    private val binding get() = _binding!!
    private var questionBar: ThemeQuestionBarController? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentThemeDetail1Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnBack.setOnClickListener { findNavController().popBackStack() }

        binding.tvHeaderTitle.text = "${CurrentUser.gradeSemester} 수강신청 가이드"

        binding.tvAdvice.text =
            "  ${CurrentUser.honorific}, 이번 학기는 전공 심화 수업이 집중되어 있어 어느 때보다 전략이 필요해요.\n" +
                "  ${CurrentUser.gradeSemester}는 취업 준비의 본격적인 시작점인 만큼, 전공 공부와 대외활동 사이의 균형을 잡는 것이 핵심입니다!"

        renderCourses(
            binding.containerCoursesAi,
            listOf(
                ThemeCourse("AI 프로그래밍 · 전공3 · 0727", "월(11:00~11:50), 수(12:00~13:50)"),
                ThemeCourse("머신러닝 · 전공3 · 0727", "화(13:00~15:50), 금(10:00~11:50)"),
                ThemeCourse("데이터마이닝 · 자전3 · 0727", "월(14:00~16:50), 수(10:00~11:50)"),
            )
        )
        renderCourses(
            binding.containerCoursesPrev,
            listOf(
                ThemeCourse("알고리즘 · 전공3 · 0727", "월(11:00~11:50), 수(12:00~13:50)"),
                ThemeCourse("운영체제 · 전공3 · 0727", "화(10:00~11:50), 목(12:00~13:50)"),
                ThemeCourse("딥러닝 · 전공3 · 0727", "월(14:00~16:50), 수(10:00~11:50)"),
            )
        )

        binding.btnQuick1.setOnClickListener { }
        binding.btnQuick2.setOnClickListener { }
        binding.btnQuick3.setOnClickListener { }

        questionBar = ThemeQuestionBarController(
            root = binding.root,
            inlineInput = binding.inlineInput,
            floatingInput = binding.floatingInput,
            cardInput = binding.cardInput,
            etMessage = binding.etMessage,
        ).also { it.setup() }
    }

    private fun renderCourses(container: LinearLayout, items: List<ThemeCourse>) {
        container.removeAllViews()
        val inflater = LayoutInflater.from(requireContext())
        val gap = (8 * resources.displayMetrics.density).toInt()
        items.forEachIndexed { index, course ->
            val view = inflater.inflate(R.layout.item_theme_course, container, false)
            view.findViewById<TextView>(R.id.tv_course_title).text = course.title
            view.findViewById<TextView>(R.id.tv_course_subtitle).text = course.schedule
            view.setOnClickListener { it.isSelected = !it.isSelected }
            val lp = view.layoutParams as LinearLayout.LayoutParams
            lp.bottomMargin = if (index == items.lastIndex) 0 else gap
            view.layoutParams = lp
            container.addView(view)
        }
    }

    override fun onDestroyView() {
        questionBar?.cancel()
        questionBar = null
        super.onDestroyView()
        _binding = null
    }
}
