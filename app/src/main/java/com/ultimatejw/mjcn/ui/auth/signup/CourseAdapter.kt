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
 * - 카드 형태. 선택 시 primary 테두리.
 * - [showGradeOnSelect] true → 선택 시 성적 선택칸도 노출 (step4).
 *                       false → 테두리만 변경, 성적칸 미노출 (step5).
 */
class CourseAdapter(
    private val onAddClick: (Course) -> Unit,
    private val onGradeClick: (Course) -> Unit = {},
    private val selectionProvider: (String) -> SelectedCourse?,
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
        val tvGrade: TextView = itemView.findViewById(R.id.tv_grade_select)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_course, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val course = items[position]
        // 학수번호가 있으면 "코드 과목명" 형태로, 없으면 과목명만 표시
        holder.tvName.text = if (course.code.isEmpty()) course.name else "${course.code} ${course.name}"
        holder.tvMeta.text = course.meta

        val selection = selectionProvider(course.name)
        val isSelected = selection != null
        holder.card.isSelected = isSelected

        if (isSelected && showGradeOnSelect) {
            holder.tvGrade.visibility = View.VISIBLE
            val grade = selection?.grade
            val context = holder.itemView.context
            val isChapel = course.name == CHAPEL_COURSE_NAME
            if (grade.isNullOrEmpty()) {
                holder.tvGrade.text = ""
                if (isChapel) {
                    // 채플은 성적이 아닌 이수 횟수를 입력해야 하므로 빨간 필수 힌트를 노출
                    holder.tvGrade.hint = context.getString(R.string.signup_chapel_count_hint)
                    holder.tvGrade.setHintTextColor(context.getColor(R.color.error))
                } else {
                    holder.tvGrade.hint = context.getString(R.string.signup_course_grade_hint)
                    holder.tvGrade.setHintTextColor(0xFFCCCCCC.toInt())
                }
                holder.tvGrade.setBackgroundResource(R.drawable.bg_search_field)
            } else {
                holder.tvGrade.text = grade
                holder.tvGrade.setBackgroundResource(R.drawable.bg_grade_select)
            }
            holder.tvGrade.setOnClickListener { onGradeClick(course) }
        } else {
            holder.tvGrade.visibility = View.GONE
            holder.tvGrade.setOnClickListener(null)
        }

        holder.btnAdd.setOnClickListener { onAddClick(course) }
    }

    override fun getItemCount(): Int = items.size
}
