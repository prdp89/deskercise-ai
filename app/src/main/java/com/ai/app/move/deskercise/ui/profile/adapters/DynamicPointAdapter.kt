package com.ai.app.move.deskercise.ui.profile.adapters

import android.annotation.SuppressLint
import android.view.ViewGroup
import com.ai.app.move.deskercise.R
import com.ai.app.move.deskercise.base.BaseBindingAdapter
import com.ai.app.move.deskercise.data.Score
import com.ai.app.move.deskercise.data.User
import com.ai.app.move.deskercise.databinding.ItemRankBinding
import com.ai.app.move.deskercise.utils.DateTimeUtils
import com.ai.app.move.deskercise.utils.getLayoutInflater
import com.ai.app.move.deskercise.utils.gone
import com.ai.app.move.deskercise.utils.isVisible
import com.ai.app.move.deskercise.utils.loadImage
import com.ai.app.move.deskercise.utils.setTextColorCompat
import com.ai.app.move.deskercise.utils.visible

class DynamicPointAdapter : BaseBindingAdapter<ItemRankBinding>() {

    private val itemsScore = arrayListOf<Score>()
    private val itemsUser = arrayListOf<User>()

    private var displayScore: Boolean = true

    @SuppressLint("NotifyDataSetChanged")
    fun togglePointsType() {
        displayScore = !displayScore
        notifyDataSetChanged()
    }

    @JvmName("updateDataScore")
    fun updateData(scores: List<Score>, forceUpdate: Boolean = true) {
        if (forceUpdate) {
            val size = itemsScore.size
            itemsScore.clear()
            if (displayScore) notifyItemRangeRemoved(0, size)
        }
        val currentSize = itemsScore.size
        itemsScore.addAll(scores)
        if (displayScore) notifyItemRangeInserted(currentSize, scores.size)
    }

    @JvmName("updateDataUser")
    fun updateData(users: List<User>, forceUpdate: Boolean = true) {
        if (forceUpdate) {
            val size = itemsUser.size
            itemsUser.clear()
            if (!displayScore) notifyItemRangeRemoved(0, size)
        }
        val currentSize = itemsUser.size
        itemsUser.addAll(users)
        if (!displayScore) notifyItemRangeInserted(currentSize, users.size)
    }

    override fun getItemBinding(parent: ViewGroup, viewType: Int): ItemRankBinding {
        return ItemRankBinding.inflate(parent.getLayoutInflater(), parent, false)
    }

    override fun bindItem(binding: ItemRankBinding, position: Int) {
        if (displayScore) {
            val item = itemsScore[position]
            val context = binding.root.context
            binding.tvRank.visible()
            if (item.type == "POINT_REDEMPTION") {
                binding.tvTitle.text = item.name
                binding.tvRank.text = context.getString(R.string.minus_d_points, item.score)
                binding.tvRank.setTextColorCompat(R.color.colorRed)
            } else {
                binding.tvCompleteExercise.isVisible(item.type != "STREAK")
                binding.tvTitle.text = item.name
                binding.tvCompleteExercise.text = context.getString(R.string._d_d_, item.countCompletedExercise, item.countExercise)
                binding.tvRank.text = context.getString(R.string.plus_d_points, item.score)
                binding.tvRank.setTextColorCompat(R.color.greenTheme)
            }

            binding.tvDescription.text = context.getString(R.string._s_utc_plus_, DateTimeUtils.getDateTimeFormat(item.createdAt, DateTimeUtils.dd_MM_YYYY_hh_mm_12))
            binding.ivStatus.gone()
            binding.avatar.setImageResource(
                if (item.type == "PROGRAM") R.drawable.ic_type_exercise else R.drawable.ic_type_streak,
            )
        } else {
            val item = itemsUser[position]
            val context = binding.root.context
            binding.tvTitle.text = item.name
            binding.tvDescription.text =
                context.resources.getQuantityString(R.plurals._points, item.totalScore, item.totalScore)
            binding.ivStatus.gone()
            binding.avatar.loadImage(item.avatar)
            binding.tvRank.gone()
        }
    }

    override fun getItemCount(): Int {
        return if (displayScore) itemsScore.size else itemsUser.size
    }
}
