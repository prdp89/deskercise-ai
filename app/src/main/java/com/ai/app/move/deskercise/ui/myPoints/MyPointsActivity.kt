package com.ai.app.move.deskercise.ui.myPoints

import com.ai.app.move.deskercise.base.BaseBindingActivity
import com.ai.app.move.deskercise.databinding.ActivityMyPointsBinding

class MyPointsActivity : BaseBindingActivity<ActivityMyPointsBinding>() {
    override fun getViewBinding(): ActivityMyPointsBinding {
        return ActivityMyPointsBinding.inflate(layoutInflater)
    }
}