package com.ultimatejw.mjcn.ui.main.info

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ultimatejw.mjcn.R
import com.ultimatejw.mjcn.databinding.ItemHomeInfoBinding
import com.ultimatejw.mjcn.domain.model.Info
import com.ultimatejw.mjcn.domain.model.InfoCategory

class InfoListAdapter(
    private val onItemClick: (Info) -> Unit = {},
    private val onBookmarkClick: (Info) -> Unit = {}
) : ListAdapter<Info, InfoListAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHomeInfoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemHomeInfoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Info) {
            binding.tvCategory.text = item.category
            binding.tvDday.text = "D-${item.dday}"
            binding.tvGroup.text = "${item.team} · ${if (item.isGroup) "팀/개인" else "개인"}"
            binding.tvTitle.text = item.title
            binding.btnBookmark.setBackgroundResource(
                if (item.isBookmarked) R.drawable.ic_bookmark_filled
                else R.drawable.ic_bookmark
            )
            binding.root.setOnClickListener { onItemClick(item) }
            binding.btnBookmark.setOnClickListener { onBookmarkClick(item) }
            applyCategoryChip(item.category)
        }

        private fun applyCategoryChip(categoryLabel: String) {
            val context = binding.root.context
            val category = InfoCategory.from(categoryLabel)
            val bgColorRes = when (category) {
                InfoCategory.BOOTCAMP  -> R.color.category_career_bg
                InfoCategory.CONTEST   -> R.color.category_contest_bg
                InfoCategory.SUPPORT   -> R.color.category_scholarship_simple_bg
                InfoCategory.ACTIVITY  -> R.color.category_activity_bg
                InfoCategory.EDUCATION -> R.color.category_academic_bg
            }
            val textColorRes = when (category) {
                InfoCategory.BOOTCAMP  -> R.color.category_career_text
                InfoCategory.CONTEST   -> R.color.category_contest_text
                InfoCategory.SUPPORT   -> R.color.category_scholarship_simple_text
                InfoCategory.ACTIVITY  -> R.color.category_activity_text
                InfoCategory.EDUCATION -> R.color.category_academic_text
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
        val DiffCallback = object : DiffUtil.ItemCallback<Info>() {
            override fun areItemsTheSame(oldItem: Info, newItem: Info) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Info, newItem: Info) = oldItem == newItem
        }
    }
}
