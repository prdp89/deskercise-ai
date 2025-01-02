package com.ai.app.move.deskercise.ui.myPoints.adapters

import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.ai.app.move.deskercise.base.BaseBindingAdapter
import com.ai.app.move.deskercise.data.AvailableReward
import com.ai.app.move.deskercise.databinding.ItemAvailableRewardBinding
import com.ai.app.move.deskercise.databinding.ItemLoadMoreBinding
import com.ai.app.move.deskercise.utils.getLayoutInflater
import com.ai.app.move.deskercise.utils.loadImage

sealed class AvailableRewardList {
    class AvailableRewardItem(val reward: AvailableReward) : AvailableRewardList()
    object Loading : AvailableRewardList()
}
class AvailableRewardsAdapter : BaseBindingAdapter<ViewBinding>(){

    private val items = arrayListOf<AvailableRewardList>(AvailableRewardList.Loading)
    var listener: ((AvailableReward) -> Unit)? = null
    var onLoadMore: (() -> Unit)? = null
    private var canLoadMore = true

    fun updateData(data: List<AvailableReward>, forceUpdate: Boolean = true) {
        if (forceUpdate) {
            val currentSize = items.size
            items.clear()
            notifyItemRangeRemoved(0, currentSize)
        }
        val currentSize = items.size
        items.addAll(currentSize, data.map { AvailableRewardList.AvailableRewardItem(it) })
        notifyItemRangeInserted(currentSize, data.size)
    }

    fun setCanLoadMore(isCanLoad: Boolean) {
        canLoadMore = isCanLoad
        if (!canLoadMore) {
            items.lastOrNull { it is AvailableRewardList.Loading }?.let {
                val index = items.indexOf(it)
                items.remove(it)
                notifyItemRemoved(index)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (items[position] is AvailableRewardList.Loading) 2 else 1
    }

    override fun getItemBinding(parent: ViewGroup, viewType: Int): ViewBinding {
        return if (viewType == 2) {
            ItemLoadMoreBinding.inflate(
                parent.getLayoutInflater(),
                parent,
                false,
            )
        } else {
            ItemAvailableRewardBinding.inflate(parent.getLayoutInflater(), parent, false)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun bindItem(binding: ViewBinding, position: Int) {
        if (binding is ItemAvailableRewardBinding) {
            val item = (items[position] as AvailableRewardList.AvailableRewardItem).reward

            binding.tvAvailableReward.text = item.name
            binding.ivAvailableReward.loadImage(item.thumbnailImgUrl)
            binding.root.setOnClickListener {
                listener?.invoke(item)
            }
        }
        if (binding is ItemLoadMoreBinding) {
            onLoadMore?.invoke()
        }
    }
}