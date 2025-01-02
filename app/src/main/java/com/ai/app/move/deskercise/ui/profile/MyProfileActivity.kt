package com.ai.app.move.deskercise.ui.profile

import com.ai.app.move.deskercise.base.BaseBindingActivity
import com.ai.app.move.deskercise.databinding.ActivityMyProfileBinding

class MyProfileActivity : BaseBindingActivity<ActivityMyProfileBinding>() {
    override fun getViewBinding(): ActivityMyProfileBinding {
        return ActivityMyProfileBinding.inflate(layoutInflater)
    }
}