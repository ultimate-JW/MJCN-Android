package com.ultimatejw.mjcn.ui.main.theme

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ultimatejw.mjcn.domain.model.Theme
import com.ultimatejw.mjcn.databinding.ItemThemeBinding
import com.ultimatejw.mjcn.databinding.ItemThemeFooterBinding

class ThemeAdapter(
    private val onItemClick: (Theme) -> Unit
) : ListAdapter<Theme, RecyclerView.ViewHolder>(DiffCallback) {

    override fun getItemViewType(position: Int): Int =
        if (position == super.getItemCount()) VIEW_TYPE_FOOTER else VIEW_TYPE_ITEM

    override fun getItemCount(): Int = super.getItemCount() + 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_FOOTER) {
            FooterViewHolder(
                ItemThemeFooterBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        } else {
            ThemeViewHolder(
                ItemThemeBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ThemeViewHolder) {
            holder.bind(getItem(position))
            val lp = holder.itemView.layoutParams as? ViewGroup.MarginLayoutParams
            if (lp != null) {
                val topMarginDp = if (position == 0) 18 else 4
                lp.topMargin = (topMarginDp * holder.itemView.resources.displayMetrics.density).toInt()
                holder.itemView.layoutParams = lp
            }
        }
    }

    inner class ThemeViewHolder(
        private val binding: ItemThemeBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(theme: Theme) {
            binding.tvTitle.text = theme.title
            binding.tvSubtitle.text = theme.subtitle
            binding.ivIcon.setImageResource(theme.iconRes)
            binding.layoutThemeIcon.backgroundTintList =
                ColorStateList.valueOf(Color.parseColor(theme.iconBgColor))
            binding.root.setOnClickListener { onItemClick(theme) }
        }
    }

    inner class FooterViewHolder(
        binding: ItemThemeFooterBinding
    ) : RecyclerView.ViewHolder(binding.root)

    companion object {
        private const val VIEW_TYPE_ITEM = 0
        private const val VIEW_TYPE_FOOTER = 1

        val DiffCallback = object : DiffUtil.ItemCallback<Theme>() {
            override fun areItemsTheSame(oldItem: Theme, newItem: Theme) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Theme, newItem: Theme) = oldItem == newItem
        }
    }
}
