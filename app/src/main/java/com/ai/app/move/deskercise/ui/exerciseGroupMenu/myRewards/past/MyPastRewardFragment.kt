package com.ai.app.move.deskercise.ui.exerciseGroupMenu.myRewards.past

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.ai.app.move.deskercise.base.BaseBindingFragment
import com.ai.app.move.deskercise.databinding.FragmentPastRewardsBinding
import com.ai.app.move.deskercise.ui.exerciseGroupMenu.myRewards.adapter.MyPastRewardAdapter
import com.ai.app.move.deskercise.ui.rewards.detail.RewardDetailActivity
import com.ai.app.move.deskercise.ui.rewards.detail.RewardDetailFragment
import com.ai.app.move.deskercise.utils.addVerticalSpacing
import com.ai.app.move.deskercise.utils.gone
import com.ai.app.move.deskercise.utils.simplyObserver
import com.ai.app.move.deskercise.utils.visible
import org.koin.android.ext.android.inject

class MyPastRewardFragment : BaseBindingFragment<FragmentPastRewardsBinding>() {

    companion object {
        fun newInstance(): MyPastRewardFragment {
            return MyPastRewardFragment()
        }
    }

    private val viewModel by inject<MyPastRewardViewModel>()

    private val myPastRewardAdapter by lazy {
        MyPastRewardAdapter()
    }

    override fun getViewBinding(): FragmentPastRewardsBinding {
        return FragmentPastRewardsBinding.inflate(layoutInflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.fetchRedeemReward()
        observerViewModel()
        initRecyclerView()
        initEventListener()
    }

    private fun initEventListener() {
        myPastRewardAdapter.listener = { redeemRewards ->
            RewardDetailFragment.rewardId = redeemRewards.rewardId
            startActivity(Intent(requireContext(), RewardDetailActivity::class.java))
        }
    }

    private fun observerViewModel() {
        simplyObserver(viewModel.redeemRewardsLiveData,
            success = { state ->
                binding.tvEmptyList.isVisible = state.data.isEmpty()
                if (myPastRewardAdapter.itemCount < 20) binding.rvPassReward.scrollToPosition(0)
                binding.rvPassReward.isVisible = state.data.isNotEmpty()
                myPastRewardAdapter.updateData(state.data, forceUpdate = false)
                myPastRewardAdapter.setCanLoadMore(state.data.size == 20)
            }, loading = {
                //nothing
            }, error = {
                binding.tvEmptyList.visible()
                binding.rvPassReward.gone()
            })
    }

    private fun initRecyclerView() {
        binding.rvPassReward.apply {
            adapter = myPastRewardAdapter
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            addVerticalSpacing(5f)
        }
    }
}