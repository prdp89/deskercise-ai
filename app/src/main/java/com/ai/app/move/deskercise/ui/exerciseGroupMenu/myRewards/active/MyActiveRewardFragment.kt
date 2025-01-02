package com.ai.app.move.deskercise.ui.exerciseGroupMenu.myRewards.active

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.ai.app.move.deskercise.base.BaseBindingFragment
import com.ai.app.move.deskercise.databinding.FragmentActiveRewardsBinding
import com.ai.app.move.deskercise.ui.exerciseGroupMenu.myRewards.adapter.MyActiveRewardAdapter
import com.ai.app.move.deskercise.ui.rewards.detail.RewardDetailActivity
import com.ai.app.move.deskercise.ui.rewards.detail.RewardDetailFragment
import com.ai.app.move.deskercise.utils.addVerticalSpacing
import com.ai.app.move.deskercise.utils.gone
import com.ai.app.move.deskercise.utils.simplyObserver
import com.ai.app.move.deskercise.utils.visible
import org.koin.android.ext.android.inject

class MyActiveRewardFragment : BaseBindingFragment<FragmentActiveRewardsBinding>() {

    companion object {
        fun newInstance(): MyActiveRewardFragment {
            return MyActiveRewardFragment()
        }
    }

    private val viewModel by inject<MyActiveRewardViewModel>()

    private val myActiveRewardAdapter by lazy {
        MyActiveRewardAdapter()
    }

    override fun getViewBinding(): FragmentActiveRewardsBinding {
        return FragmentActiveRewardsBinding.inflate(layoutInflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.fetchRedeemReward()
        observerViewModel()
        initRecyclerView()
        initEventListener()
    }

    private fun initEventListener() {
        myActiveRewardAdapter.listener = { redeemRewards ->
            RewardDetailFragment.rewardId = redeemRewards.rewardId
            startActivity(Intent(requireContext(), RewardDetailActivity::class.java))
        }
    }

    private fun observerViewModel() {
        simplyObserver(viewModel.redeemRewardsLiveData,
            success = { state ->
                binding.tvEmptyList.isVisible = state.data.isEmpty()
                if (myActiveRewardAdapter.itemCount < 20) binding.rvActiveReward.scrollToPosition(0)
                binding.rvActiveReward.isVisible = state.data.isNotEmpty()
                myActiveRewardAdapter.updateData(state.data, forceUpdate = false)
                myActiveRewardAdapter.setCanLoadMore(state.data.size == 20)
            }, loading = {
                //nothing
            }, error = {
                binding.tvEmptyList.visible()
                binding.rvActiveReward.gone()
            })
    }

    private fun initRecyclerView() {
        binding.rvActiveReward.apply {
            adapter = myActiveRewardAdapter
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            addVerticalSpacing(5f)
        }
    }
}