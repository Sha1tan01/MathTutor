package ru.mathtutor.app.presentation.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.noties.markwon.Markwon
import ru.mathtutor.app.databinding.ItemMessageAiBinding
import ru.mathtutor.app.databinding.ItemMessageUserBinding
import ru.mathtutor.app.domain.model.ChatMessage
import ru.mathtutor.app.domain.model.MessageRole

class ChatAdapter : ListAdapter<ChatMessage, RecyclerView.ViewHolder>(DiffCB()) {

    companion object {
        private const val TYPE_USER = 0
        private const val TYPE_AI   = 1
    }

    override fun getItemViewType(position: Int) =
        if (getItem(position).role == MessageRole.USER) TYPE_USER else TYPE_AI

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_USER) {
            UserVH(ItemMessageUserBinding.inflate(inflater, parent, false))
        } else {
            AiVH(ItemMessageAiBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg = getItem(position)
        when (holder) {
            is UserVH -> holder.bind(msg)
            is AiVH   -> holder.bind(msg)
        }
    }

    inner class UserVH(private val b: ItemMessageUserBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(msg: ChatMessage) {
            b.tvMessage.text = msg.content
        }
    }

    inner class AiVH(private val b: ItemMessageAiBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(msg: ChatMessage) {
            b.wvMessage.setMarkdownLatex(msg.content)
        }
    }

    class DiffCB : DiffUtil.ItemCallback<ChatMessage>() {
        override fun areItemsTheSame(a: ChatMessage, b: ChatMessage) = a.id == b.id
        override fun areContentsTheSame(a: ChatMessage, b: ChatMessage) = a == b
    }
}
