package com.ai.app.move.deskercise.ui.profile.widgets

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.ai.app.move.deskercise.databinding.WidgetScoreSectionBinding
import com.ai.app.move.deskercise.utils.getLayoutInflater
import com.ai.app.move.deskercise.utils.isVisible

class ScoreSectionWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : FrameLayout(context, attrs) {

    private val binding = WidgetScoreSectionBinding.inflate(getLayoutInflater(), this, true)

    fun enableTeamPoint(isEnable: Boolean){
        binding.tvTeamPoints.isVisible(isEnable)
        binding.tvTeamPointsValue.isVisible(isEnable)
        binding.lineEnd.isVisible(isEnable)
    }
    fun setYourScore(value: Int) {
        binding.tvYourScoreValue.text = value.toString()
    }

    fun setTeamPoint(value: Int) {
        binding.tvTeamPointsValue.text = value.toString()
    }

    fun setSessionScore(value: Int) {
        binding.tvSessionsValue.text = value.toString()
    }
}
