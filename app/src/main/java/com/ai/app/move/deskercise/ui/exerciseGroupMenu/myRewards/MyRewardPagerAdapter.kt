package com.ai.app.move.deskercise.ui.exerciseGroupMenu.myRewards

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.ai.app.move.deskercise.R
import com.ai.app.move.deskercise.ui.exerciseGroupMenu.myRewards.active.MyActiveRewardFragment
import com.ai.app.move.deskercise.ui.exerciseGroupMenu.myRewards.past.MyPastRewardFragment

class MyRewardPagerAdapter(val fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int {
        return TabType.values().size
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> MyActiveRewardFragment.newInstance()
            else -> MyPastRewardFragment.newInstance()
        }
    }

    enum class TabType(val value: Int, val tabNameRes: Int) {
        ACTIVE(0, R.string.active_rewards),
        PAST(1, R.string.past_rewards);

        companion object {
            fun getTabByPosition(position: Int): TabType {
                return values()[position]
            }
        }
    }
}