package com.kisan.lazyshare.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.Glide
import com.kisan.lazyshare.adapter.ShareItemAdapter.BaseViewHolder
import com.kisan.lazyshare.databinding.AudioItemBinding
import com.kisan.lazyshare.databinding.DocItemBinding
import com.kisan.lazyshare.databinding.ImageItemBinding
import com.kisan.lazyshare.databinding.VideoItemBinding
import com.kisan.lazyshare.model.ShareItem
import com.kisan.lazyshare.utils.Constants


class ShareItemAdapter(
    private val onClick: (shareItem: ShareItem) -> Unit,
) : ListAdapter<ShareItem, BaseViewHolder>(Companion) {
    var tracker: SelectionTracker<String>? = null

    init {
        setHasStableIds(true)
    }

    override fun getItemViewType(position: Int): Int {
        return currentList[position].type
    }

    companion object : DiffUtil.ItemCallback<ShareItem>() {
        override fun areItemsTheSame(oldItem: ShareItem, newItem: ShareItem): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: ShareItem, newItem: ShareItem): Boolean {
            return oldItem.contentUri == newItem.contentUri && oldItem.name == oldItem.name && oldItem.id == oldItem.id
        }

    }


    override fun getItemId(position: Int): Long = currentList[position].id

    inner class ImageViewHolder(private val binding: ImageItemBinding) :
        BaseViewHolder(binding) {
        fun bind(item: ShareItem, selected: Boolean) {
            binding.apply {
                Glide.with(binding.displayIV)
                    .load(item.contentUri)
                    .into(binding.displayIV)
                checkedImage.isVisible = selected
            }
        }
    }

    inner class VideoViewHolder(private val binding: VideoItemBinding) :
        BaseViewHolder(binding) {
        fun bind(item: ShareItem, selected: Boolean) {
            binding.apply {
                Glide.with(binding.displayIV)
                    .load(item.contentUri)
                    .into(binding.displayIV)
                checkedImage.isVisible = selected
            }
        }
    }

    inner class DocViewHolder(private val binding: DocItemBinding) :
        BaseViewHolder(binding) {
        fun bind(item: ShareItem, selected: Boolean) {
            binding.apply {
                displayIV.text = item.name
            }
        }
    }

    inner class AudioViewHolder(private val binding: AudioItemBinding) :
        BaseViewHolder(binding)

    open inner class BaseViewHolder(binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {
        fun getItemDetails(): ItemDetailsLookup.ItemDetails<String> {
            return object : ItemDetailsLookup.ItemDetails<String>() {

                override fun inSelectionHotspot(e: MotionEvent): Boolean {
                    return true
                }

                override fun getPosition(): Int = adapterPosition
                override fun getSelectionKey(): String = currentList[adapterPosition].name
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {

        return when (viewType) {
            Constants.IMAGE_TYPE -> {
                ImageViewHolder(
                    ImageItemBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            Constants.VIDEO_TYPE -> {
                VideoViewHolder(
                    VideoItemBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            Constants.AUDIO_TYPE -> {
                AudioViewHolder(
                    AudioItemBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            Constants.DOC_TYPE -> {
                DocViewHolder(
                    DocItemBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            else -> {
                ImageViewHolder(
                    ImageItemBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val currentItem = currentList[position]

        when (holder) {
            is ImageViewHolder -> {
                tracker?.let {
                    holder.bind(currentItem, it.isSelected(currentItem.name))
                }
            }
            is VideoViewHolder -> {
                tracker?.let {
                    Log.d("DEBUG", it.isSelected(currentItem.name).toString())
                    it.isSelected(currentItem.name)

                    holder.bind(currentItem, it.isSelected(currentItem.name))
                }
            }
            is DocViewHolder -> {
                holder.bind(currentItem, false)
                tracker?.let {
                    Log.d("DEBUG", it.isSelected(currentItem.name).toString())
                    it.isSelected(currentItem.name)

                }
            }
        }
    }
}