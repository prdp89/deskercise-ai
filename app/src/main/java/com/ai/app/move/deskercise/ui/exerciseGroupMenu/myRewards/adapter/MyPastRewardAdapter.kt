package com.ai.app.move.deskercise.ui.exerciseGroupMenu.myRewards.adapter

import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.ai.app.move.deskercise.R
import com.ai.app.move.deskercise.base.BaseBindingAdapter
import com.ai.app.move.deskercise.data.RedeemRewards
import com.ai.app.move.deskercise.databinding.ItemLoadMoreBinding
import com.ai.app.move.deskercise.databinding.ItemMyRewardBinding
import com.ai.app.move.deskercise.utils.getLayoutInflater
import com.ai.app.move.deskercise.utils.loadImage
import com.ai.app.move.deskercise.utils.setTextColorCompat

sealed class MyPastRewardList {
    class MyPastRewardItem(val redeemRewards: RedeemRewards) : MyPastRewardList()
    object Loading : MyPastRewardList()
}

class MyPastRewardAdapter : BaseBindingAdapter<ViewBinding>() {

    private val items = arrayListOf<MyPastRewardList>(MyPastRewardList.Loading)
    var listener: ((RedeemRewards) -> Unit)? = null
    var onLoadMore: (() -> Unit)? = null
    private var canLoadMore = true

    fun updateData(data: List<RedeemRewards>, forceUpdate: Boolean = true) {
        if (forceUpdate) {
            val currentSize = items.size
            items.clear()
            notifyItemRangeRemoved(0, currentSize)
        }
        val currentSize = items.size
        items.addAll(currentSize - 1, data.map { MyPastRewardList.MyPastRewardItem(it) })
        notifyItemRangeInserted(currentSize - 1, data.size)
    }

    fun setCanLoadMore(isCanLoad: Boolean) {
        canLoadMore = isCanLoad
        if (!canLoadMore) {
            items.lastOrNull { it is MyPastRewardList.Loading }?.let {
                val index = items.indexOf(it)
                items.remove(it)
                notifyItemRemoved(index)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (items[position] is MyPastRewardList.Loading) 2 else 1
    }

    override fun getItemBinding(parent: ViewGroup, viewType: Int): ViewBinding {
        return if (viewType == 2) {
            ItemLoadMoreBinding.inflate(
                parent.getLayoutInflater(),
                parent,
                false,
            )
        } else {
            ItemMyRewardBinding.inflate(parent.getLayoutInflater(), parent, false)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun bindItem(binding: ViewBinding, position: Int) {
        if (binding is ItemMyRewardBinding) {
            val item = (items[position] as MyPastRewardList.MyPastRewardItem).redeemRewards

            binding.ivRewardThumbnail.loadImage(item.redemptionDetail.rewardThumbnailImgUrl)
            binding.tvRewardName.text = item.redemptionDetail.rewardName
            val validTime = item.redemptionDetail.rewardValidity
            binding.tvValidTime.text = if (validTime != null) {
                "Used on $validTime"
            } else {
                "Used on 26 Jun 2022"
            }
            binding.tvStatus.text = "Used"
            binding.tvStatus.setTextColorCompat(com.esafirm.imagepicker.R.color.ef_grey)
            binding.tvTag1.setTextColorCompat(com.esafirm.imagepicker.R.color.ef_grey)
            binding.tvTag1.setBackgroundResource(R.drawable.bg_grey_12)
            binding.tvTag2.setTextColorCompat(com.esafirm.imagepicker.R.color.ef_grey)
            binding.tvTag2.setBackgroundResource(R.drawable.bg_grey_12)

            binding.root.setOnClickListener {
                listener?.invoke(item)
            }
        }

        if (binding is ItemLoadMoreBinding) {
            onLoadMore?.invoke()
        }
    }

}