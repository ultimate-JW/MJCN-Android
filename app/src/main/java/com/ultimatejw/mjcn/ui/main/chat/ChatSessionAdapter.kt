package com.ultimatejw.mjcn.ui.main.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ultimatejw.mjcn.databinding.ItemChatSessionBinding
import com.ultimatejw.mjcn.domain.model.ChatSession

class ChatSessionAdapter(
    private val onClick: (ChatSession) -> Unit
) : ListAdapter<ChatSession, ChatSessionAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(private val binding: ItemChatSessionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(session: ChatSession) {
            binding.tvTitle.text = session.title
            binding.tvLastMessage.text = session.lastMessage
            binding.root.setOnClickListener { onClick(session) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemChatSessionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<ChatSession>() {
        override fun areItemsTheSame(oldItem: ChatSession, newItem: ChatSession) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: ChatSession, newItem: ChatSession) =
            oldItem == newItem
    }
}
