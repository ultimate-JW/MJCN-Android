package com.ultimatejw.mjcn.ui.main.theme

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ultimatejw.mjcn.data.model.Theme
import com.ultimatejw.mjcn.databinding.ItemThemeBinding

class ThemeAdapter(
    private val onItemClick: (Theme) -> Unit
) : ListAdapter<Theme, ThemeAdapter.ThemeViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThemeViewHolder {
        val binding = ItemThemeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ThemeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ThemeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ThemeViewHolder(
        private val binding: ItemThemeBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(theme: Theme) {
            binding.tvTitle.text = theme.title
            binding.tvSubtitle.text = theme.subtitle
            binding.ivIcon.setImageResource(theme.iconRes)
            binding.root.setOnClickListener { onItemClick(theme) }
        }
    }

    companion object {
        val DiffCallback = object : DiffUtil.ItemCallback<Theme>() {
            override fun areItemsTheSame(oldItem: Theme, newItem: Theme) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Theme, newItem: Theme) = oldItem == newItem
        }
    }
}
