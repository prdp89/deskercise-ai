package com.ai.app.move.deskercise.ui.profile.widgets

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.ai.app.move.deskercise.R
import com.ai.app.move.deskercise.databinding.WidgetStreakSectionBinding
import com.ai.app.move.deskercise.utils.getLayoutInflater
import com.ai.app.move.deskercise.utils.toPx
import de.hdodenhof.circleimageview.CircleImageView

class StreakSectionWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : FrameLayout(context, attrs) {

    private val binding = WidgetStreakSectionBinding.inflate(getLayoutInflater(), this, true)
    private var currentStreak = 0

//    fun setDayStreaks(items: List<Streak>) {
//        items.firstOrNull { it.day == currentStreak }?.let { streak ->
//            when (streak.day) {
//                3 -> {
//                    setUpTextPosition(binding.iv3Days, streak.score)
//                }
//                7 -> {
//                    setUpTextPosition(binding.iv7Days, streak.score)
//                }
//                14 -> {
//                    setUpTextPosition(binding.iv14Days, streak.score)
//                }
//                21 -> {
//                    setUpTextPosition(binding.iv21Days, streak.score)
//                }
//            }
//        }
//    }

//    private fun setUpTextPosition(followedView: View, value: Int) {
//        binding.tvPoint.visible()
//        binding.tvPoint.text = buildString {
//            append("+")
//            append(context.resources.getQuantityString(R.plurals._points, value, value))
//        }
//        (binding.tvPoint.layoutParams as ConstraintLayout.LayoutParams).let { params ->
//            params.topToTop = followedView.id
//            binding.tvPoint.layoutParams = params
//        }
//    }

    fun setStreak(streak: Int) {
        minimizeAndInactiveBadges()
        if (streak < 3) {
            return
        }
        val activeSize = 76f.toPx(context)
        val badgeResource = R.drawable.ic_streak_fire
        val fireResource = R.drawable.ic_fire_active
        val activeColor = R.color.deskGreen

        fun makeResourceActive(
            view: CircleImageView,
        ) {
            view.setCircleBackgroundColorResource(activeColor)
            view.setImageResource(fireResource)
        }

        fun makeResourceStreak(
            view: CircleImageView,
        ) {
            view.layoutParams.width = activeSize
            view.layoutParams.height = activeSize
            view.setImageResource(badgeResource)
        }

        when (streak) {
            in 3..6 -> {
                currentStreak = 3
                getImageView(3).apply { makeResourceStreak(this) }
            }

            in 7..13 -> {
                currentStreak = 7
                getImageView(3).apply { makeResourceActive(this) }

                getImageView(currentStreak).apply { makeResourceStreak(this) }
            }

            in 14..20 -> {
                currentStreak = 14
                getImageView(3).apply { makeResourceActive(this) }
                getImageView(7).apply { makeResourceActive(this) }

                getImageView(currentStreak).apply { makeResourceStreak(this) }
            }

            in 21..30 -> {
                currentStreak = 21
                getImageView(3).apply { makeResourceActive(this) }
                getImageView(7).apply { makeResourceActive(this) }
                getImageView(14).apply { makeResourceActive(this) }

                getImageView(currentStreak).apply { makeResourceStreak(this) }
            }

            else -> {
                currentStreak = 31
                getImageView(3).apply { makeResourceActive(this) }
                getImageView(7).apply { makeResourceActive(this) }
                getImageView(14).apply { makeResourceActive(this) }
                getImageView(21).apply { makeResourceActive(this) }

                getImageView(currentStreak).apply { makeResourceStreak(this) }
            }
        }
    }

    private fun minimizeAndInactiveBadges() {
        val size = 40f.toPx(context)
        val badgeResource = R.drawable.ic_streak_hidden

        fun minimizeAndMakeInactive(view: CircleImageView) {
            view.layoutParams.width = size
            view.layoutParams.height = size
            view.setImageResource(badgeResource)
        }
        getImageView(3).apply { minimizeAndMakeInactive(this) }
        getImageView(7).apply { minimizeAndMakeInactive(this) }
        getImageView(14).apply { minimizeAndMakeInactive(this) }
        getImageView(21).apply { minimizeAndMakeInactive(this) }
        getImageView(30).apply { minimizeAndMakeInactive(this) }
    }

    private fun getImageView(days: Int): CircleImageView {
        return when (days) {
            3 -> binding.iv3Days
            7 -> binding.iv7Days
            14 -> binding.iv14Days
            21 -> binding.iv21Days
            else -> binding.iv30Days
        }
    }
}
