package com.ai.app.move.deskercise.ui.home

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.analytics.FirebaseAnalytics
import com.ai.app.move.deskercise.R
import com.ai.app.move.deskercise.app.UserManager
import com.ai.app.move.deskercise.base.BaseBindingActivity
import com.ai.app.move.deskercise.databinding.ActivityHomeBinding
import com.ai.app.move.deskercise.utils.setTextColorCompat
import com.ai.app.move.deskercise.utils.setTintColorCompat
import com.permissionx.guolindev.PermissionX
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

interface HomeBridge {
    fun goToHome()

    fun openProfile()
}

class HomeActivity : BaseBindingActivity<ActivityHomeBinding>(), HomeBridge {

    companion object {
        fun startNewTask(context: Context) {
            val intent = Intent(context, HomeActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            context.startActivity(intent)
        }
    }

    private val userManager: UserManager by inject()

    override fun getViewBinding(): ActivityHomeBinding {
        return ActivityHomeBinding.inflate(layoutInflater)
    }

    private val homePagerAdapter by lazy { HomePagerAdapter(this) }
    private val viewModel: HomeViewModel by viewModel()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.viewPager.adapter = homePagerAdapter
        binding.viewPager.isUserInputEnabled = false

        binding.tabLayout.addOnTabSelectedListener(tabLayoutOnPageChangeListener)

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.setCustomView(R.layout.tab_item_home)
            tab.customView?.findViewById<TextView>(R.id.title)?.apply {
                text = HomePagerAdapter.items[position].getValue("title").toString()
            }

            tab.customView?.findViewById<ImageView>(R.id.icon)?.apply {
                setImageResource(HomePagerAdapter.items[position].getValue("icon") as Int)
            }
        }.attach()
        askForNotificationPermission()
        setCrashlyticsUserInfo()
    }

    private fun setCrashlyticsUserInfo() {
        userManager.getUserInfo()?.let { user ->
            FirebaseAnalytics.getInstance(this).setUserId(user.email)
        }
    }

    private val tabLayoutOnPageChangeListener = object : TabLayout.OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab) {
            tab.customView?.findViewById<TextView>(R.id.title)?.apply {
                setTextColorCompat(R.color.deskGreen)
            }
            tab.customView?.findViewById<ImageView>(R.id.icon)?.apply {
                setTintColorCompat(R.color.deskGreen)
            }
        }

        override fun onTabUnselected(tab: TabLayout.Tab) {
            tab.customView?.findViewById<TextView>(R.id.title)?.apply {
                setTextColorCompat(R.color.gray)
            }
            tab.customView?.findViewById<ImageView>(R.id.icon)?.apply {
                setTintColorCompat(R.color.gray)
            }
        }

        override fun onTabReselected(tab: TabLayout.Tab?) {
        }
    }

    override fun goToHome() {
        binding.viewPager.setCurrentItem(0, false)
    }

    override fun openProfile() {
        binding.viewPager.setCurrentItem(2, false)
    }

    private fun askForNotificationPermission() {
        val permissions = arrayListOf(Manifest.permission.POST_NOTIFICATIONS)
        PermissionX.init(this)
            .permissions(permissions)
            .onExplainRequestReason { scope, deniedList ->
                scope.showRequestReasonDialog(
                    deniedList,
                    getString(R.string.permission_explained),
                    getString(R.string.ok),
                    getString(R.string.cancel),
                )
            }
            .onForwardToSettings { scope, deniedList ->
                scope.showForwardToSettingsDialog(
                    deniedList,
                    getString(R.string.permission_enable_manually),
                    getString(R.string.ok),
                    getString(R.string.cancel),
                )
            }
            .request { allGranted, _, _ ->
                if (allGranted) {
                    viewModel.updateFcmToken()
                }
            }
    }
}
