package com.ai.app.move.deskercise.ui.rewards.detail

import com.ai.app.move.deskercise.base.BaseBindingActivity
import com.ai.app.move.deskercise.databinding.ActivityRewardDetailBinding

class RewardDetailActivity : BaseBindingActivity<ActivityRewardDetailBinding>() {
    override fun getViewBinding(): ActivityRewardDetailBinding {
        return ActivityRewardDetailBinding.inflate(layoutInflater)
    }
}