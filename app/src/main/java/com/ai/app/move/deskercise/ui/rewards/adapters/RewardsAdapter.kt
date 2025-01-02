package com.ai.app.move.deskercise.ui.rewards.adapters

import android.view.ViewGroup
import android.widget.Button
import androidx.viewbinding.ViewBinding
import com.ai.app.move.deskercise.R
import com.ai.app.move.deskercise.base.BaseBindingAdapter
import com.ai.app.move.deskercise.data.Reward
import com.ai.app.move.deskercise.databinding.ItemLoadMoreBinding
import com.ai.app.move.deskercise.databinding.ItemRewardBinding
import com.ai.app.move.deskercise.utils.getLayoutInflater
import com.ai.app.move.deskercise.utils.loadImage
import com.ai.app.move.deskercise.utils.setSafeClick

sealed class RewardList {
    class RewardItem(val reward: Reward) : RewardList()
    object Loading : RewardList()
}

class RewardsAdapter : BaseBindingAdapter<ViewBinding>() {

    private val items = arrayListOf<RewardList>(RewardList.Loading)
    var listener: ((Reward) -> Unit)? = null
    var onLoadMore: (() -> Unit)? = null
    private var canLoadMore = true
    private var userPoints = 0

    fun updateUserPoint(points: Int) {
        userPoints = points
        notifyItemRangeChanged(0, itemCount - 1)
    }

    fun updateData(data: List<Reward>, forceUpdate: Boolean = true) {
        if (forceUpdate) {
            val currentSize = items.size
            items.clear()
            notifyItemRangeRemoved(0, currentSize)
        }
        val currentSize = items.size
        items.addAll(currentSize - 1, data.map { RewardList.RewardItem(it) })
        notifyItemRangeInserted(currentSize - 1, data.size)
    }

    fun setCanLoadMore(isCanLoad: Boolean) {
        canLoadMore = isCanLoad
        if (!canLoadMore) {
            items.lastOrNull { it is RewardList.Loading }?.let {
                val index = items.indexOf(it)
                items.remove(it)
                notifyItemRemoved(index)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (items[position] is RewardList.Loading) 2 else 1
    }

    override fun getItemBinding(parent: ViewGroup, viewType: Int): ViewBinding {
        return if (viewType == 2) {
            ItemLoadMoreBinding.inflate(
                parent.getLayoutInflater(),
                parent,
                false,
            )
        } else {
            ItemRewardBinding.inflate(parent.getLayoutInflater(), parent, false)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun bindItem(binding: ViewBinding, position: Int) {
        if (binding is ItemRewardBinding) {
            val item = (items[position] as RewardList.RewardItem).reward
            val resource = binding.root.context.resources

            if (item.point > userPoints) {
                disableButton(binding.buyButton)
            } else {
                binding.buyButton.isEnabled = true
                binding.buyButton.isClickable = true
                binding.buyButton.setSafeClick {
                    listener?.invoke(item)
                }
            }

            binding.rewardName.text = item.name
            binding.rewardPoints.text = resource.getQuantityString(R.plurals._points, item.point, item.point)
            binding.rewardItemImage.loadImage(item.image)
        }
        if (binding is ItemLoadMoreBinding) {
            onLoadMore?.invoke()
        }
    }

    private fun disableButton(button: Button) {
        button.isClickable = false
        button.isEnabled = false
        button.setOnClickListener(null)
    }
}
