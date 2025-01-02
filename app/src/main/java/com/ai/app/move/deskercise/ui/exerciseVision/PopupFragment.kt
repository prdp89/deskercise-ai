package com.ai.app.move.deskercise.ui.exerciseVision

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.ai.app.move.deskercise.databinding.FragmentPopupBinding

class PopupFragment : DialogFragment() {
    private lateinit var binding: FragmentPopupBinding
    private lateinit var viewPager: ViewPager2
    var currentPage = 0
    var hasCalledDismiss = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        dialog!!.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        binding = FragmentPopupBinding.inflate(inflater, container, false)
        viewPager = binding.popUpViewPager

        val activity = (activity as FragmentActivity).supportFragmentManager
        val pagerAdapter = ViewPagerAdapter(activity)
        viewPager.adapter = pagerAdapter

        // Inflate the layout for this fragment
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> {
                        currentPage = 0
                    }

                    1 -> {
                        currentPage = 1
                        binding.next.text = "Next"
                    }

                    2 -> {
                        currentPage = 2
                        binding.next.text = "End"
                    }
                }
                super.onPageSelected(position)
            }
        })

        binding.next.setOnClickListener {
            if (currentPage < 2) {
                currentPage++
                when (currentPage) {
                    1 -> {
                        viewPager.currentItem = 1
                    }

                    2 -> {
                        viewPager.currentItem = 2
                    }
                }
            } else {
                hasCalledDismiss = true
                dismissNow()
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
                    FragmentPopupOne()
                }

                1 -> {
                    FragmentPopupTwo()
                }

                else -> {
                    FragmentPopupThree()
                }
            }
        }
    }
}
