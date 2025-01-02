package com.ai.app.move.deskercise.ui.myPoints

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.esafirm.imagepicker.view.GridSpacingItemDecoration
import com.ai.app.move.deskercise.app.UserManager
import com.ai.app.move.deskercise.base.BaseBindingFragment
import com.ai.app.move.deskercise.databinding.FragmentMyPointsBinding
import com.ai.app.move.deskercise.ui.exerciseGroupMenu.myRewards.MyRewardActivity
import com.ai.app.move.deskercise.ui.myPoints.adapters.AvailableRewardsAdapter
import com.ai.app.move.deskercise.ui.rewards.detail.RewardDetailActivity
import com.ai.app.move.deskercise.ui.rewards.detail.RewardDetailFragment
import com.ai.app.move.deskercise.utils.gone
import com.ai.app.move.deskercise.utils.simplyObserver
import com.ai.app.move.deskercise.utils.visible
import org.koin.android.ext.android.inject

class MyPointsFragment : BaseBindingFragment<FragmentMyPointsBinding>() {

    companion object {
        const val IS_HIDE_BACK_BUTTON = "is-hide-back-button"

        fun newInstance(isHideBackButton: Boolean = false): MyPointsFragment {
            val args = Bundle()
            args.putBoolean(IS_HIDE_BACK_BUTTON, isHideBackButton)
            val fragment = MyPointsFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private val viewModel by inject<MyPointsViewModel>()

    private val availableRewardsAdapter by lazy {
        AvailableRewardsAdapter()
    }

    override fun getViewBinding(): FragmentMyPointsBinding {
        return FragmentMyPointsBinding.inflate(layoutInflater)
    }

    private val userManager: UserManager by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val isHideBackButton = arguments?.getBoolean(IS_HIDE_BACK_BUTTON) ?: false
        binding.ivBack.isVisible = !isHideBackButton

        viewModel.fetchAvailableReward()
        observerViewModel()
        initRecyclerView()
        initEventListener()
    }

    override fun onResume() {
        super.onResume()

        updateUserInfoUI()
    }

    private fun updateUserInfoUI() {
        userManager.getUserInfo()?.let { user ->
            binding.tvPointsForRedemption.text = user.totalScore.toString()
        }
    }

    private fun observerViewModel() {
        simplyObserver(viewModel.availableRewardsLiveData,
            success = { state ->
                binding.tvEmptyList.isVisible = state.data.isEmpty()
                if (availableRewardsAdapter.itemCount < 20) binding.rvAvailableReward.scrollToPosition(0)
                binding.rvAvailableReward.isVisible = state.data.isNotEmpty()
                availableRewardsAdapter.updateData(state.data, forceUpdate = true)
                availableRewardsAdapter.setCanLoadMore(state.data.size == 20)
            }, loading = {
                //nothing
            }, error = {
                binding.tvEmptyList.visible()
                binding.rvAvailableReward.gone()
            })
    }

    private fun initEventListener() {
        binding.ivBack.setOnClickListener {
            activity?.onBackPressed()
        }
        binding.llHowToGetPoints.setOnClickListener {
            val token = userManager.getAccessToken()
            val url = "https://develop.d2js7o0cnl90h5.amplifyapp.com/?token=$token"
            val uri = Uri.parse(url)
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        }
        binding.tvMyReward.setOnClickListener {
            startActivity(Intent(requireContext(), MyRewardActivity::class.java))
        }
        availableRewardsAdapter.listener = { availableReward ->
            RewardDetailFragment.rewardId = availableReward.pk
            startActivity(Intent(requireContext(), RewardDetailActivity::class.java))
        }
    }

    private fun initRecyclerView() {
        binding.rvAvailableReward.apply {
            adapter = availableRewardsAdapter
            layoutManager =
                GridLayoutManager(requireContext(), 3, LinearLayoutManager.VERTICAL, false)
            addItemDecoration(GridSpacingItemDecoration(3, 5, false))
        }

        availableRewardsAdapter.onLoadMore = {
            viewModel.fetchAvailableReward()
        }
    }
}