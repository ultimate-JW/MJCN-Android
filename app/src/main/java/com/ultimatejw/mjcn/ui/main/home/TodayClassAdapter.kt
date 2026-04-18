package com.ultimatejw.mjcn.ui.main.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ultimatejw.mjcn.R
import com.ultimatejw.mjcn.databinding.ItemTodayClassBinding
import com.ultimatejw.mjcn.domain.model.TodayClass
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class TodayClassAdapter : ListAdapter<TodayClass, TodayClassAdapter.ViewHolder>(DiffCallback) {

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTodayClassBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemTodayClassBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TodayClass) {
            val context = binding.root.context
            binding.tvTime.text = "${item.startTime}~${item.endTime}"
            binding.tvClassName.text = "${item.name} · ${item.room}"

            val now = LocalTime.now()
            val start = LocalTime.parse(item.startTime, timeFormatter)
            val end = LocalTime.parse(item.endTime, timeFormatter)

            when {
                now.isAfter(end) -> {
                    // 지난 수업
                    binding.layoutRoot.background = null
                    val color = ContextCompat.getColor(context, R.color.font_color4)
                    binding.tvTime.setTextColor(color)
                    binding.tvClassName.setTextColor(color)
                    binding.viewDot.backgroundTintList =
                        ContextCompat.getColorStateList(context, R.color.font_color4)
                }
                now.isAfter(start) && now.isBefore(end) -> {
                    // 현재 수업 중
                    binding.layoutRoot.background =
                        ContextCompat.getDrawable(context, R.drawable.bg_point_color1_radius18)
                    val white = ContextCompat.getColor(context, R.color.white)
                    binding.tvTime.setTextColor(white)
                    binding.tvClassName.setTextColor(white)
                    binding.viewDot.backgroundTintList =
                        ContextCompat.getColorStateList(context, R.color.white)
                }
                else -> {
                    // 앞으로 수업
                    binding.layoutRoot.background = null
                    val color = ContextCompat.getColor(context, R.color.font_color1)
                    binding.tvTime.setTextColor(color)
                    binding.tvClassName.setTextColor(color)
                    binding.viewDot.backgroundTintList =
                        ContextCompat.getColorStateList(context, R.color.font_color1)
                }
            }
        }
    }

    companion object {
        val DiffCallback = object : DiffUtil.ItemCallback<TodayClass>() {
            override fun areItemsTheSame(oldItem: TodayClass, newItem: TodayClass) =
                oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: TodayClass, newItem: TodayClass) =
                oldItem == newItem
        }
    }
}
