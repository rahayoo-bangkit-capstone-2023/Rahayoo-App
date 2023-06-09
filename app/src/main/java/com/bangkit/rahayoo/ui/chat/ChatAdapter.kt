package com.bangkit.rahayoo.ui.chat

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bangkit.rahayoo.R
import com.bangkit.rahayoo.data.model.Chat
import com.bangkit.rahayoo.databinding.LayoutItemChatBinding

class ChatAdapter: ListAdapter<Chat, ChatAdapter.ChatViewHolder>(ChatDiffUtilCallback()) {

    inner class ChatViewHolder(private val binding: LayoutItemChatBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(chat: Chat) {
            binding.tvChatMessage.text = chat.message

            if (chat.sender.equals("rahayoo.ai")) {
                binding.ivChatProfile.setImageResource(R.drawable.avatar_placeholder)
            } else binding.ivChatProfile.setImageResource(R.drawable.ic_logo_non_transparent)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        return ChatViewHolder(
            LayoutItemChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        Log.d("ChatAdapter", "onBindViewHolder: $itemCount")
        holder.bind(getItem(position))
    }
}

class ChatDiffUtilCallback: DiffUtil.ItemCallback<Chat>() {
    override fun areItemsTheSame(oldItem: Chat, newItem: Chat): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Chat, newItem: Chat): Boolean {
        return oldItem.message == newItem.message
    }

}