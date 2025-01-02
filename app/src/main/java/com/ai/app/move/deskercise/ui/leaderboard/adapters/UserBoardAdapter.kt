package com.ai.app.move.deskercise.ui.leaderboard.adapters

import android.view.ViewGroup
import com.ai.app.move.deskercise.R
import com.ai.app.move.deskercise.app.UserManager
import com.ai.app.move.deskercise.base.BaseBindingAdapter
import com.ai.app.move.deskercise.databinding.RecyclerViewItemLeaderboardBinding
import com.ai.app.move.deskercise.network.responses.PersonalBoardResponse
import com.ai.app.move.deskercise.utils.getLayoutInflater
import com.ai.app.move.deskercise.utils.loadImage

class UserBoardAdapter(
    userManager: UserManager,
) : BaseBindingAdapter<RecyclerViewItemLeaderboardBinding>() {
    private val currentUserId = userManager.getUserInfo()?.id ?: -1

    private val items = arrayListOf<PersonalBoardResponse>()

    fun setData(data: List<PersonalBoardResponse>) {
        val currentSize = items.size
        items.clear()
        notifyItemRangeRemoved(0, currentSize)
        items.addAll(data)
        notifyItemRangeInserted(0, data.size)
    }

    override fun getItemBinding(
        parent: ViewGroup,
        viewType: Int,
    ): RecyclerViewItemLeaderboardBinding {
        return RecyclerViewItemLeaderboardBinding.inflate(parent.getLayoutInflater(), parent, false)
    }

    override fun getItemCount(): Int = items.size

    override fun bindItem(binding: RecyclerViewItemLeaderboardBinding, position: Int) {
        val item = items[position]
        binding.ivAvatar.loadImage(item.avatar)
        binding.leaderboardUsername.text = item.name
        binding.leaderboardPoints.text = item.score.toString()
        binding.leaderboardPosition.text = item.currentRank.toString()
        val statusResId = when {
            item.currentRank == item.beforeRank -> {
                0
            }
            item.currentRank < item.beforeRank -> {
                R.drawable.up
            }
            else -> {
                R.drawable.ic_down
            }
        }
        binding.ivStatus.setImageResource(statusResId)
        binding.cardConstraintLayout.setBackgroundResource(
            if (currentUserId == item.id) R.color.deskLightShadeGreen else R.color.white,
        )
    }
}
