package com.ai.app.move.deskercise.ui.rewards

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.ai.app.move.deskercise.R
import com.ai.app.move.deskercise.app.UserManager
import com.ai.app.move.deskercise.base.BaseBindingFragment
import com.ai.app.move.deskercise.databinding.FragmentRewardsBinding
import com.ai.app.move.deskercise.ui.rewards.adapters.RewardsAdapter
import com.ai.app.move.deskercise.utils.addVerticalSpacing
import com.ai.app.move.deskercise.utils.simplyObserver
import org.koin.android.ext.android.inject

class RewardFragment : BaseBindingFragment<FragmentRewardsBinding>() {

    private val viewModel by inject<RewardListViewModel>()

    private val userManager: UserManager by inject()

    private val rewardsAdapter by lazy {
        RewardsAdapter()
    }

    override fun getViewBinding(): FragmentRewardsBinding {
        return FragmentRewardsBinding.inflate(layoutInflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.backButtondiff.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        simplyObserver(viewModel.rewardsLiveData, success = { state ->
            if (rewardsAdapter.itemCount < 20) binding.recyclerRewards.scrollToPosition(0)
            rewardsAdapter.updateData(state.data, forceUpdate = false)
            rewardsAdapter.setCanLoadMore(state.data.size == 20)
        }, loading = {
            // just do nothing
        })
        simplyObserver(viewModel.redeemRewardLiveData) {
            showRedeemSuccessPopup()
            updateUserPoint()
        }
        binding.recyclerRewards.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = rewardsAdapter
            addVerticalSpacing(5f)
        }
        rewardsAdapter.onLoadMore = {
            viewModel.fetchReward()
        }
        rewardsAdapter.listener = { buyItem ->
            viewModel.redeemReward(buyItem)
        }
        updateUserPoint()
        viewModel.fetchReward()
    }

    private fun updateUserPoint() {
        val user = userManager.getUserInfo() ?: return
        binding.tvUserPoints.text = resources.getQuantityString(R.plurals._points, user.point, user.point)
        rewardsAdapter.updateUserPoint(user.point)
    }

    private fun showRedeemSuccessPopup() {
        val dialog = RedeemSuccessDialog()
        dialog.show(parentFragmentManager, RedeemSuccessDialog::class.java.simpleName)
    }
}
