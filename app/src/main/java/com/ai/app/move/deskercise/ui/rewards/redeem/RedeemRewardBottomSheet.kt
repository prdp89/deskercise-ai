package com.ai.app.move.deskercise.ui.rewards.redeem

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ai.app.move.deskercise.R
import com.ai.app.move.deskercise.data.RewardDetail
import com.ai.app.move.deskercise.databinding.BottomSheetRedeemRewardBinding

class RedeemRewardBottomSheet(
    private val rewardDetail: RewardDetail,
    private val onUseNowListener : (RewardDetail) -> Unit,
    private val onUseLaterListener : (RewardDetail) -> Unit
) : BottomSheetDialogFragment() {

    lateinit var binding: BottomSheetRedeemRewardBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BottomSheetRedeemRewardBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvPoint.text = context?.getString(R.string.points, rewardDetail.points)
        binding.ivClose.setOnClickListener {
            dismiss()
        }
        binding.tvUseNow.setOnClickListener {
            onUseNowListener.invoke(rewardDetail)
            dismiss()
        }
        binding.tvUseLater.setOnClickListener {
            onUseLaterListener.invoke(rewardDetail)
            dismiss()
        }
    }

}