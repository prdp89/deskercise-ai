package com.ai.app.move.deskercise.ui.splash

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ai.app.move.deskercise.R
import com.ai.app.move.deskercise.ui.home.HomeActivity
import com.ai.app.move.deskercise.ui.onboarding.OnboardActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class SplashActivity : AppCompatActivity() {
    private val viewModel by viewModel<SplashViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        Intent()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen)

        viewModel.userLoggedLiveData.observe(this) { isUserLogged ->
            if (isUserLogged) {
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
            } else {
                val intent = Intent(this, OnboardActivity::class.java)
                startActivity(intent)
            }
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkLogin()
    }
}
