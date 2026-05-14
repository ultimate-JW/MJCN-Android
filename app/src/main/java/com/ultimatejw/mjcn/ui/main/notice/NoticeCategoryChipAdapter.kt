package com.ultimatejw.mjcn.ui.main.notice

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.ultimatejw.mjcn.R
import com.ultimatejw.mjcn.databinding.ItemNoticeChipBinding

class NoticeCategoryChipAdapter(
    private val categories: List<String>,
    private val onSelected: (String) -> Unit = {}
) : RecyclerView.Adapter<NoticeCategoryChipAdapter.ViewHolder>() {

    private var selectedIndex = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemNoticeChipBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(categories[position], position == selectedIndex)
    }

    override fun getItemCount() = categories.size

    inner class ViewHolder(private val binding: ItemNoticeChipBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(label: String, selected: Boolean) {
            binding.tvChip.text = label
            binding.tvChip.isSelected = selected
            binding.tvChip.typeface = ResourcesCompat.getFont(
                binding.root.context,
                if (selected) R.font.pretendard_semibold else R.font.pretendard_medium
            )
            binding.tvChip.setOnClickListener {
                val pos = adapterPosition
                if (pos == RecyclerView.NO_POSITION) return@setOnClickListener
                val prev = selectedIndex
                selectedIndex = pos
                notifyItemChanged(prev)
                notifyItemChanged(selectedIndex)
                onSelected(label)
            }
        }
    }
}
