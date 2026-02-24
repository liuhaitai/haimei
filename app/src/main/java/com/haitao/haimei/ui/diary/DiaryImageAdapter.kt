package com.haitao.haimei.ui.diary

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.haitao.haimei.databinding.ItemDiaryImageBinding

class DiaryImageAdapter(
    private val onImageClick: (position: Int, uriText: String) -> Unit
) : RecyclerView.Adapter<DiaryImageAdapter.ImageViewHolder>() {
    private val items = mutableListOf<String>()

    fun submit(list: List<String>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    fun removeAt(position: Int) {
        if (position !in items.indices) return
        items.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, itemCount - position)
    }

    fun getItems(): List<String> = items.toList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = ItemDiaryImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ImageViewHolder(private val binding: ItemDiaryImageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(uriText: String) {
            binding.diaryImageView.setImageURI(Uri.parse(uriText))
            binding.root.setOnClickListener {
                onImageClick(bindingAdapterPosition, uriText)
            }
        }
    }
}
