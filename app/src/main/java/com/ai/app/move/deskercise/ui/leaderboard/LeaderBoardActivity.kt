package com.ai.app.move.deskercise.ui.leaderboard

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ai.app.move.deskercise.R
import com.ai.app.move.deskercise.databinding.ActivityLeaderboardBinding

class LeaderBoardActivity : AppCompatActivity(), LeaderBoardBridge {

    private lateinit var binding: ActivityLeaderboardBinding
    override fun shouldShowBackButton(): Boolean {
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLeaderboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val fragmentTransaction = supportFragmentManager.beginTransaction()

        fragmentTransaction.replace(R.id.leaderboard_layout, LeaderboardFragment())
//        fragmentTransaction.addToBackStack("SUMMARY_EXERCISE_FRAGMENT")
        fragmentTransaction.commit()
    }
}
