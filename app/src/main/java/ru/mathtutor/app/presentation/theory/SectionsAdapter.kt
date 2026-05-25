package ru.mathtutor.app.presentation.theory

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.mathtutor.app.R
import ru.mathtutor.app.databinding.ItemSectionBinding
import ru.mathtutor.app.domain.model.Section

class SectionsAdapter(
    private val onClick: (Section) -> Unit
) : ListAdapter<Section, SectionsAdapter.VH>(DiffCB()) {

    inner class VH(private val b: ItemSectionBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(section: Section) {
            b.tvSectionIcon.text = section.icon
            b.tvSectionTitle.text = section.title
            b.tvSectionDescription.text = section.description
            b.tvTopicCount.text = "${section.topicCount} тем"

            val bgColor = when (section.colorTag) {
                "teal"   -> R.color.icon_bg_teal
                "purple" -> R.color.icon_bg_purple
                "green"  -> R.color.icon_bg_green
                "orange" -> R.color.icon_bg_orange
                "red"    -> R.color.icon_bg_red
                else     -> R.color.icon_bg_blue
            }
            b.tvSectionIcon.setBackgroundResource(bgColor)
            b.root.setOnClickListener { onClick(section) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        ItemSectionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    class DiffCB : DiffUtil.ItemCallback<Section>() {
        override fun areItemsTheSame(a: Section, b: Section) = a.id == b.id
        override fun areContentsTheSame(a: Section, b: Section) = a == b
    }
}
