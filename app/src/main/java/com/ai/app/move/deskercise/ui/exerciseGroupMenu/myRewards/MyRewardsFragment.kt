package com.ai.app.move.deskercise.ui.exerciseGroupMenu.myRewards

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayoutMediator
import com.ai.app.move.deskercise.base.BaseBindingFragment
import com.ai.app.move.deskercise.databinding.FragmentMyRewardsBinding

class MyRewardsFragment : BaseBindingFragment<FragmentMyRewardsBinding>() {

    private val myRewardPagerAdapter by lazy {
        MyRewardPagerAdapter(this)
    }

    override fun getViewBinding(): FragmentMyRewardsBinding {
        return FragmentMyRewardsBinding.inflate(layoutInflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initTabLayout()
        initEventListener()
    }

    private fun initEventListener() {
        binding.ivBack.setOnClickListener {
            activity?.onBackPressed()
        }
    }

    private fun initTabLayout() {
        binding.vpReward.adapter = myRewardPagerAdapter
        binding.vpReward.offscreenPageLimit = 2
        (binding.vpReward.getChildAt(0) as RecyclerView).overScrollMode =
            RecyclerView.OVER_SCROLL_NEVER
        TabLayoutMediator(binding.tlReward, binding.vpReward) { tabLayout, position ->
            tabLayout.text =
                getString(MyRewardPagerAdapter.TabType.getTabByPosition(position).tabNameRes)
        }.attach()
    }
}