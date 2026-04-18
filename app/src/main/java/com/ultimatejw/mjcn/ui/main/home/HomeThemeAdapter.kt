package com.ultimatejw.mjcn.ui.main.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ultimatejw.mjcn.databinding.ItemHomeThemeBinding
import com.ultimatejw.mjcn.domain.model.Theme

class HomeThemeAdapter(
    private val onItemClick: (Theme) -> Unit = {}
) : ListAdapter<Theme, HomeThemeAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHomeThemeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemHomeThemeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Theme) {
//            binding.ivIcon.image = item.category
            binding.tvTitle.text = item.title
            binding.tvSubtitle.text = item.subtitle
            binding.root.setOnClickListener { onItemClick(item) }
        }
    }

    companion object {
        val DiffCallback = object : DiffUtil.ItemCallback<Theme>() {
            override fun areItemsTheSame(oldItem: Theme, newItem: Theme) =
                oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Theme, newItem: Theme) =
                oldItem == newItem
        }
    }
}
