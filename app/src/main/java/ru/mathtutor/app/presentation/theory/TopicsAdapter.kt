package ru.mathtutor.app.presentation.theory

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.mathtutor.app.R
import ru.mathtutor.app.databinding.ItemTopicBinding
import ru.mathtutor.app.domain.model.Topic
import ru.mathtutor.app.domain.model.TopicStatus

class TopicsAdapter(
    private val progressMap: Map<String, TopicStatus> = emptyMap(),
    private val onClick: (Topic) -> Unit
) : ListAdapter<Topic, TopicsAdapter.VH>(DiffCB()) {

    inner class VH(private val b: ItemTopicBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(topic: Topic) {
            b.tvTopicTitle.text = topic.title
            val status = progressMap[topic.id] ?: TopicStatus.NOT_STARTED
            val (dotColor, statusText) = when (status) {
                TopicStatus.DONE        -> Pair(R.color.status_done,     "✅ Изучено")
                TopicStatus.IN_PROGRESS -> Pair(R.color.status_partial,  "⚡ В процессе")
                TopicStatus.NOT_STARTED -> Pair(R.color.status_not_started, "○ Не начато")
            }
            b.viewStatusDot.setBackgroundColor(
                ContextCompat.getColor(b.root.context, dotColor)
            )
            b.tvTopicStatus.text = statusText
            b.root.setOnClickListener { onClick(topic) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        ItemTopicBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    class DiffCB : DiffUtil.ItemCallback<Topic>() {
        override fun areItemsTheSame(a: Topic, b: Topic) = a.id == b.id
        override fun areContentsTheSame(a: Topic, b: Topic) = a == b
    }
}
