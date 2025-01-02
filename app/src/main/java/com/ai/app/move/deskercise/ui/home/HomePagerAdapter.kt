package com.ai.app.move.deskercise.ui.home

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.ai.app.move.deskercise.R
import com.ai.app.move.deskercise.ui.exerciseGroupMenu.ExerciseGroupMenuFragment
import com.ai.app.move.deskercise.ui.leaderboard.LeaderboardFragment
import com.ai.app.move.deskercise.ui.myPoints.MyPointsFragment
import com.ai.app.move.deskercise.ui.profile.MyProfileFragment

class HomePagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    companion object {
        val items = listOf(
            mapOf(
                "title" to "Home",
                "icon" to R.drawable.home,
            ),
            mapOf(
                "title" to "Leaderboard",
                "icon" to R.drawable.ic_leaderboard,
            ),
            mapOf(
                "title" to "Profile",
                "icon" to R.drawable.profile_icon,
            ),
            mapOf(
                "title" to "My Points",
                "icon" to R.drawable.ic_star
            )
        )
    }

    private val programFragment by lazy { ExerciseGroupMenuFragment() }
    private val leaderBoardFragment by lazy { LeaderboardFragment() }
    private val myProfileFragment by lazy { MyProfileFragment() }
    private val myPointsFragment = MyPointsFragment.newInstance(true)

    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> programFragment
            1 -> leaderBoardFragment
            2 -> myProfileFragment
            else -> myPointsFragment
        }
    }
}
