package com.ai.app.move.deskercise.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ai.app.move.deskercise.databinding.FragmentOnboardingTwoBinding

class FragmentOnboardTwo : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val binding = FragmentOnboardingTwoBinding.inflate(inflater, container, false)
        return binding.root
    }
}
