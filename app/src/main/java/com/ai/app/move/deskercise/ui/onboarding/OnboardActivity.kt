package com.ai.app.move.deskercise.ui.onboarding

import android.os.Bundle
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.NavHostFragment
import com.ai.app.move.deskercise.R
import com.ai.app.move.deskercise.databinding.ActivityOnboardPageBinding

/**
 * The number of pages (wizard steps) to show in this demo.
 */

class OnboardActivity : FragmentActivity() {
    private lateinit var binding: ActivityOnboardPageBinding
//    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)

        super.onCreate(savedInstanceState)

        binding = ActivityOnboardPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navControllerFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_onboard) as NavHostFragment
        val navController = navControllerFragment.navController

        val inflater = navController.navInflater
        val graph = inflater.inflate(R.navigation.nav_graph_onboard)

        navControllerFragment.navController.graph = graph
    }
}
