package com.haitao.haimei.ui.diary

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.haitao.haimei.data.entity.DiaryEntryEntity
import com.haitao.haimei.databinding.ItemDiaryEntryBinding
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class DiaryListAdapter(
    private val onItemClick: (DiaryEntryEntity) -> Unit
) : ListAdapter<DiaryEntryEntity, DiaryListAdapter.EntryViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EntryViewHolder {
        val binding = ItemDiaryEntryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EntryViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: EntryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class EntryViewHolder(
        private val binding: ItemDiaryEntryBinding,
        private val onItemClick: (DiaryEntryEntity) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

        fun bind(entry: DiaryEntryEntity) {
            val timeText = Instant.ofEpochMilli(entry.time)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
                .format(formatter)
            binding.entryTime.text = timeText
            binding.entryMood.text = moodToEmoji(entry.mood)
            binding.entryTitle.text = entry.title.ifBlank { "(无标题)" }
            binding.entryContent.text = entry.content

            val tagText = entry.tags
                ?.split(",")
                ?.map { it.trim() }
                ?.filter { it.isNotEmpty() }
                ?.joinToString(" ") { "#$it" }
                .orEmpty()
            binding.entryTags.text = tagText

            val hasImages = !entry.imageUris.isNullOrBlank()
            binding.entryImageHint.visibility = if (hasImages) View.VISIBLE else View.GONE

            binding.root.setOnClickListener { onItemClick(entry) }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<DiaryEntryEntity>() {
        override fun areItemsTheSame(oldItem: DiaryEntryEntity, newItem: DiaryEntryEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: DiaryEntryEntity, newItem: DiaryEntryEntity): Boolean {
            return oldItem == newItem
        }
    }
}
