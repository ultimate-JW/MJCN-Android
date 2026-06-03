package com.ultimatejw.mjcn.ui.auth.signup

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.ultimatejw.mjcn.R

/**
 * 수강 이력 / 현재 수강 과목 리스트 어댑터.
 *
 * 카드 상태:
 *  - 선택됨  : primary 테두리, 버튼 ic_minus_circle
 *  - 비활성화 : 같은 과목명의 다른 분반이 선택된 경우 → alpha 낮춰 탁하게 표시
 *  - 기본    : normal 테두리, 버튼 ic_plus_circle
 *
 * [showGradeOnSelect] true  → 선택 시 연도·학기·성적 드롭다운 노출 (수강이력).
 *                    false → 선택 표시만 (현재 수강과목).
 */
class CourseAdapter(
    private val onAddClick: (Course) -> Unit,
    private val onYearClick: (Course) -> Unit = {},
    private val onSemesterClick: (Course) -> Unit = {},
    private val onGradeClick: (Course) -> Unit = {},
    private val selectionProvider: (Course) -> SelectedCourse?,
    private val disabledProvider: (Course) -> Boolean = { false },
    private val showGradeOnSelect: Boolean = true
) : RecyclerView.Adapter<CourseAdapter.ViewHolder>() {

    private val items = mutableListOf<Course>()

    fun submit(list: List<Course>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val card: ConstraintLayout = itemView as ConstraintLayout
        val tvName: TextView = itemView.findViewById(R.id.tv_course_name)
        val tvMeta: TextView = itemView.findViewById(R.id.tv_course_meta)
        val btnAdd: ImageView = itemView.findViewById(R.id.btn_add_course)
        val tvYear: TextView = itemView.findViewById(R.id.tv_year_select)
        val tvSemester: TextView = itemView.findViewById(R.id.tv_semester_select)
        val tvGrade: TextView = itemView.findViewById(R.id.tv_grade_select)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_course, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val course = items[position]
        holder.tvName.text = if (course.code.isEmpty()) course.name else "${course.code} ${course.name}"
        holder.tvMeta.text = course.meta

        val selection = selectionProvider(course)
        val isSelected = selection != null
        val isDisabled = !isSelected && disabledProvider(course)

        // 카드 선택 상태 (테두리)
        holder.card.isSelected = isSelected
        // 비활성화 시 탁하게 + 터치 불가
        holder.itemView.alpha = if (isDisabled) 0.35f else 1.0f
        holder.itemView.isClickable = !isDisabled
        holder.btnAdd.isEnabled = !isDisabled

        // + / - 아이콘 전환
        holder.btnAdd.setImageResource(
            if (isSelected) R.drawable.ic_minus_circle else R.drawable.ic_plus_circle
        )

        holder.btnAdd.setOnClickListener { if (!isDisabled) onAddClick(course) }

        // 연도·학기·성적 드롭다운 (수강이력 모드)
        if (isSelected && showGradeOnSelect) {
            holder.tvYear.visibility = View.VISIBLE
            holder.tvSemester.visibility = View.VISIBLE
            holder.tvGrade.visibility = View.VISIBLE

            bindDropdown(holder.tvYear, selection?.year?.toString(),
                holder.itemView.context.getString(R.string.course_year_hint))
            bindDropdown(holder.tvSemester,
                when (selection?.semester) { 1 -> "1학기"; 2 -> "2학기"; else -> null },
                holder.itemView.context.getString(R.string.course_semester_hint))

            val isChapel = course.name == CHAPEL_COURSE_NAME
            val gradeHint = holder.itemView.context.getString(
                if (isChapel) R.string.signup_chapel_count_hint else R.string.signup_course_grade_hint
            )
            if (isChapel && selection?.grade.isNullOrEmpty()) {
                holder.tvGrade.text = ""
                holder.tvGrade.hint = gradeHint
                holder.tvGrade.setHintTextColor(holder.itemView.context.getColor(R.color.error))
                holder.tvGrade.setBackgroundResource(R.drawable.bg_search_field)
            } else {
                bindDropdown(holder.tvGrade, selection?.grade, gradeHint)
            }

            holder.tvYear.setOnClickListener { onYearClick(course) }
            holder.tvSemester.setOnClickListener { onSemesterClick(course) }
            holder.tvGrade.setOnClickListener { onGradeClick(course) }
        } else {
            holder.tvYear.visibility = View.GONE
            holder.tvSemester.visibility = View.GONE
            holder.tvGrade.visibility = View.GONE
            holder.tvYear.setOnClickListener(null)
            holder.tvSemester.setOnClickListener(null)
            holder.tvGrade.setOnClickListener(null)
        }
    }

    private fun bindDropdown(view: TextView, value: String?, hint: String) {
        if (value.isNullOrEmpty()) {
            view.text = ""
            view.hint = hint
            view.setHintTextColor(0xFFCCCCCC.toInt())
            view.setBackgroundResource(R.drawable.bg_search_field)
        } else {
            view.text = value
            view.setBackgroundResource(R.drawable.bg_grade_select)
        }
    }

    override fun getItemCount(): Int = items.size
}
