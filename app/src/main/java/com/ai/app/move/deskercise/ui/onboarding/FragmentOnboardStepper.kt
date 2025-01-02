package com.ai.app.move.deskercise.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.ai.app.move.deskercise.databinding.FragmentOnboardingStepperBinding

class FragmentOnboardStepper : Fragment() {
    private lateinit var binding: FragmentOnboardingStepperBinding
    private lateinit var viewPager: ViewPager2

    var currentPage = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        TabLayoutMediator(binding.viewPagerTab, binding.viewPager) { tab, position -> }.attach()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentOnboardingStepperBinding.inflate(inflater, container, false)

        viewPager = binding.viewPager
        val activity = (activity as FragmentActivity).supportFragmentManager
        val pagerAdapter = ViewPagerAdapter(activity)
        binding.viewPager.adapter = pagerAdapter

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> {
                        binding.skip.visibility = View.VISIBLE
                        binding.next.visibility = View.VISIBLE
                        currentPage = 0
                    }
                    1 -> {
                        binding.skip.visibility = View.VISIBLE
                        binding.next.visibility = View.VISIBLE
                        currentPage = 1
                    }
                    2 -> {
                        binding.skip.visibility = View.GONE
                        binding.next.visibility = View.GONE
                        currentPage = 2
                    }
                }
                super.onPageSelected(position)
            }
        })
        binding.next.setOnClickListener {
            if (currentPage < 2) {
                currentPage++
                when (currentPage) {
                    0 -> {
                        viewPager.currentItem = 0
                    }
                    1 -> {
                        viewPager.currentItem = 1
                        binding.skip.visibility = View.VISIBLE
                    }
                    2 -> {
                        viewPager.currentItem = 2
                        binding.skip.visibility = View.GONE
                    }
                }
            }
        }

        binding.skip.setOnClickListener {
            if (currentPage < 2) {
                viewPager.currentItem = 2
                it.visibility = View.GONE
            }
        }
        return binding.root
    }

    private inner class ViewPagerAdapter(fragmentManager: FragmentManager) :
        FragmentStateAdapter(fragmentManager, lifecycle) {
        override fun getItemCount(): Int {
            return 3
        }

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> {
                    FragmentOnboardOne()
                }
                1 -> {
                    FragmentOnboardTwo()
                }
                else -> {
                    FragmentOnboardThree()
                }
            }
        }
    }
}
