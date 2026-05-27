package com.ultimatejw.mjcn.ui.main.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ultimatejw.mjcn.R
import com.ultimatejw.mjcn.domain.model.ChatMessage

class ChatMessageAdapter : ListAdapter<ChatMessage, ChatMessageAdapter.MessageViewHolder>(DiffCallback) {

    override fun getItemViewType(position: Int): Int =
        if (getItem(position).isFromUser) VIEW_TYPE_USER else VIEW_TYPE_AI

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val layout = if (viewType == VIEW_TYPE_USER) R.layout.item_chat_message_user
                     else R.layout.item_chat_message_ai
        val root = inflater.inflate(layout, parent, false)
        return MessageViewHolder(root)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(getItem(position).content)
    }

    class MessageViewHolder(root: View) : RecyclerView.ViewHolder(root) {
        private val tvMessage: TextView = root.findViewById(R.id.tv_message)
        fun bind(content: String) { tvMessage.text = content }
    }

    companion object {
        private const val VIEW_TYPE_USER = 0
        private const val VIEW_TYPE_AI = 1

        private val DiffCallback = object : DiffUtil.ItemCallback<ChatMessage>() {
            override fun areItemsTheSame(old: ChatMessage, new: ChatMessage) = old.id == new.id
            override fun areContentsTheSame(old: ChatMessage, new: ChatMessage) = old == new
        }
    }
}
