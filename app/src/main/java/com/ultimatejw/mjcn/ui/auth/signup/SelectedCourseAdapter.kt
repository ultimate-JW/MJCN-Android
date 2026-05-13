package com.ultimatejw.mjcn.ui.auth.signup

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ultimatejw.mjcn.R

/**
 * 검색바 아래에 가로 스크롤되는 선택된 과목 칩 어댑터.
 * - x 버튼 클릭 시 onRemove 콜백 → 해당 과목 선택을 해제한다.
 */
class SelectedCourseAdapter(
    private val onRemove: (SelectedCourse) -> Unit
) : RecyclerView.Adapter<SelectedCourseAdapter.ViewHolder>() {

    private val items = mutableListOf<SelectedCourse>()

    fun submit(list: List<SelectedCourse>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tv_chip_name)
        val btnRemove: ImageView = itemView.findViewById(R.id.btn_chip_remove)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_selected_course_chip, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvName.text = item.name
        holder.btnRemove.setOnClickListener { onRemove(item) }
    }

    override fun getItemCount(): Int = items.size
}
