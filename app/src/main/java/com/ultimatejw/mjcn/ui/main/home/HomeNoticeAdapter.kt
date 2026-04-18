package com.ultimatejw.mjcn.ui.main.home

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ultimatejw.mjcn.R
import com.ultimatejw.mjcn.databinding.ItemHomeNoticeBinding
import com.ultimatejw.mjcn.domain.model.Notice
import com.ultimatejw.mjcn.domain.model.NoticeCategory

class HomeNoticeAdapter(
    private val onItemClick: (Notice) -> Unit = {}
) : ListAdapter<Notice, HomeNoticeAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHomeNoticeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemHomeNoticeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Notice) {
            binding.tvCategory.text = item.category
            binding.tvTitle.text = item.title
            binding.tvDate.text = item.date
            binding.tvTeam.text = item.team
            binding.root.setOnClickListener { onItemClick(item) }
            applyCategoryChip(item.category)
        }

        private fun applyCategoryChip(categoryLabel: String) {
            val context = binding.root.context
            val category = NoticeCategory.from(categoryLabel)
            val bgColorRes = when (category) {
                NoticeCategory.NORMAL             -> R.color.category_normal_bg
                NoticeCategory.ACADEMIC           -> R.color.category_academic_bg
                NoticeCategory.OVERSEAS           -> R.color.category_overseas_bg
                NoticeCategory.CONTEST            -> R.color.category_contest_bg
                NoticeCategory.ACTIVITY           -> R.color.category_activity_bg
                NoticeCategory.CAREER             -> R.color.category_career_bg
                NoticeCategory.CAREER_SIMPLE      -> R.color.category_career_simple_bg
                NoticeCategory.SCHOLARSHIP        -> R.color.category_scholarship_bg
                NoticeCategory.SCHOLARSHIP_SIMPLE -> R.color.category_scholarship_simple_bg
            }
            val textColorRes = when (category) {
                NoticeCategory.NORMAL             -> R.color.category_normal_text
                NoticeCategory.ACADEMIC           -> R.color.category_academic_text
                NoticeCategory.OVERSEAS           -> R.color.category_overseas_text
                NoticeCategory.CONTEST            -> R.color.category_contest_text
                NoticeCategory.ACTIVITY           -> R.color.category_activity_text
                NoticeCategory.CAREER             -> R.color.category_career_text
                NoticeCategory.CAREER_SIMPLE      -> R.color.category_career_simple_text
                NoticeCategory.SCHOLARSHIP        -> R.color.category_scholarship_text
                NoticeCategory.SCHOLARSHIP_SIMPLE -> R.color.category_scholarship_simple_text
            }
            val radius = context.resources.displayMetrics.density * 22
            binding.layoutCategoryChip.background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                setColor(ContextCompat.getColor(context, bgColorRes))
                cornerRadius = radius
            }
            binding.tvCategory.setTextColor(ContextCompat.getColor(context, textColorRes))
        }
    }

    companion object {
        val DiffCallback = object : DiffUtil.ItemCallback<Notice>() {
            override fun areItemsTheSame(oldItem: Notice, newItem: Notice) =
                oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Notice, newItem: Notice) =
                oldItem == newItem
        }
    }
}
