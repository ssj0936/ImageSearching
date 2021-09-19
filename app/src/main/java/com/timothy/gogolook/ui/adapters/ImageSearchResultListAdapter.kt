package com.timothy.gogolook.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.timothy.gogolook.data.model.HitsItem
import com.timothy.gogolook.databinding.RecyclerviewImageSearchResultItemGridBinding
import com.timothy.gogolook.databinding.RecyclerviewImageSearchResultItemLineBinding

const val LAYOUT_TYPE_LINEAR = 1
const val LAYOUT_TYPE_GRID = 2

class ImageSearchResultListAdapter: PagedListAdapter<HitsItem, ImageSearchResultListAdapter.ImageSearchResultListViewHolder>(
    ImageSearchResultListDiffCallback()
) {
    var layout:LayoutType = LayoutType.Linear
        private set

    class ImageSearchResultListViewHolder(
        private val binding: ViewBinding
    ): RecyclerView.ViewHolder(binding.root){
        fun bind(image: HitsItem){
            if(binding is RecyclerviewImageSearchResultItemLineBinding) {
                binding.imageInfo = image
                binding.executePendingBindings()
            }
            else if(binding is RecyclerviewImageSearchResultItemGridBinding) {
                binding.imageInfo = image
                binding.executePendingBindings()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageSearchResultListViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = when(layout){
            is LayoutType.Linear->
                RecyclerviewImageSearchResultItemLineBinding.inflate(layoutInflater,parent,false)

            is LayoutType.Grid->
                RecyclerviewImageSearchResultItemGridBinding.inflate(layoutInflater,parent,false)
        }
        return ImageSearchResultListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageSearchResultListViewHolder, position: Int) {
        getItem(position)?.let {item ->
            holder.bind(item)
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (layout is LayoutType.Grid) {
            return LAYOUT_TYPE_GRID
        } else if (layout is LayoutType.Linear) {
            return LAYOUT_TYPE_LINEAR
        }
        return super.getItemViewType(position)
    }

    fun setLayoutType(layoutType:LayoutType){
        layout = layoutType
    }
}

sealed class LayoutType{
    object Linear:LayoutType()
    object Grid:LayoutType()
}

class ImageSearchResultListDiffCallback: DiffUtil.ItemCallback<HitsItem>() {
    override fun areItemsTheSame(
        oldItem: HitsItem,
        newItem: HitsItem
    ): Boolean = oldItem.id == newItem.id

    override fun areContentsTheSame(
        oldItem: HitsItem,
        newItem: HitsItem
    ): Boolean = oldItem == newItem
}