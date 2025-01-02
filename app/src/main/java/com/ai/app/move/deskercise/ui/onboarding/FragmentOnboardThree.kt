package com.ai.app.move.deskercise.ui.onboarding

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ai.app.move.deskercise.databinding.FragmentOnboardingThreeBinding
import com.ai.app.move.deskercise.ui.login.LoginActivity

class FragmentOnboardThree : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val binding = FragmentOnboardingThreeBinding.inflate(inflater, container, false)

        binding.startBtn.setOnClickListener {
            val intent = Intent(activity, LoginActivity::class.java)
            startActivity(intent)
        }
        return binding.root
    }
}
