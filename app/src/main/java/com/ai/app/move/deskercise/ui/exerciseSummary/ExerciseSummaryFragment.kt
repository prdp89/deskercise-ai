package com.ai.app.move.deskercise.ui.exerciseSummary

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.ai.app.move.deskercise.R
import com.ai.app.move.deskercise.base.BaseBindingFragment
import com.ai.app.move.deskercise.base.State
import com.ai.app.move.deskercise.data.Exercise
import com.ai.app.move.deskercise.databinding.FragmentExerciseSummaryBinding
import com.ai.app.move.deskercise.services.vasScaleImage
import com.ai.app.move.deskercise.ui.exerciseGroupMenu.StartProgramViewModel
import com.ai.app.move.deskercise.ui.exerciseVision.MainActivity.Companion.movesList
import com.ai.app.move.deskercise.ui.leaderboard.LeaderBoardActivity
import com.ai.app.move.deskercise.vision.Move
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import timber.log.Timber

/**
 * ExerciseSummaryFragment contains the logic to handle the summary results after all the exercises are completed
 */
class ExerciseSummaryFragment : BaseBindingFragment<FragmentExerciseSummaryBinding>() {

    override fun getViewBinding(): FragmentExerciseSummaryBinding {
        return FragmentExerciseSummaryBinding.inflate(layoutInflater)
    }

    private lateinit var moveModels: ArrayList<Move>
    private lateinit var exerciseList: ArrayList<Exercise>
    private val sendResultsViewModel by activityViewModel<StartProgramViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            exerciseList = (it.getSerializable("exerciseList") as ArrayList<Exercise>?)!!
            moveModels = (it.getSerializable("moveList") as ArrayList<Move>?)!!
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        registerEvents()

        binding.ivNext.setOnClickListener {
            Timber.d(">> clicked")
            startActivity(Intent(requireContext(), LeaderBoardActivity::class.java))
//            activity?.finish()
        }
        val feedback_result = vasScaleImage()
        val rvExerciseSummaryResults = binding.rvExerciseSummaryResults
        rvExerciseSummaryResults.layoutManager = LinearLayoutManager(activity)
        rvExerciseSummaryResults.adapter = RecyclerViewAdapterExerciseSummaryResults(movesList)
        val textView_feedback = binding.exerciseSummaryPanelFeedback
        val textView_comments = binding.exerciseSummaryPanelComments

//        val textView_comments = fragmentView.findViewById<View>(R.id.exercise_summary_panel_comments) as TextView
        textView_comments.text = feedback_result.comments

        val points = if (sendResultsViewModel.pointLiveData.value == null) {
            0
        } else {
            sendResultsViewModel.pointLiveData.value
        }
        binding.tvDescription.text =
            getString(R.string.you_have_completed_d_reps, feedback_result.totalReps)
//
//        for (i in exerciseList) {
//            points += i.countGood * 2 + i.countBad
//        }
        updatePoints(points ?: 0)
        sendResultsViewModel.updateProgramID(moveModels.size)
        sendResultsViewModel.updateExercises(exerciseList)
        sendResultsViewModel.sendResults()
        var totalreps: Float = 0F
        var goodreps: Float = 0F
        var compreps: Float = 0F
        for (i in exerciseList) {
            totalreps += i.totalReps.toFloat()
            goodreps += i.countGood.toFloat()
            compreps += i.completedReps.toFloat()
        }
        var percentageGood = if (moveModels.size <= 5) {
            (goodreps / 20)
        } else if (moveModels.size <= 8) {
            (goodreps / 38)
        } else {
            goodreps / 56
        }

        val category: Int = when {
            percentageGood > 0.9F -> {
                1
            }

            percentageGood > 0.7F -> {
                2
            }

            percentageGood > 0.5F -> {
                3
            }

            percentageGood > 0.3F -> {
                4
            }

            else -> {
                5
            }
        }
        when ((category)) {
            1 -> {
                textView_comments.text = "Great job!"
                textView_feedback.text = "You have over 80% good repetitions!"
            }

            2 -> {
                textView_comments.text = "Great job!"
                textView_feedback.text = "Good job! You have over 60% good repetitions!"
            }

            3 -> {
                textView_comments.text = "Try harder"
                textView_feedback.text = "You have over 40% good repetitions!"
            }

            4 -> {
                textView_comments.text = "Try harder"
                textView_feedback.text = "You have less than 40% good repetitions"
            }

            5 -> {
                textView_comments.text = "Try harder"
                textView_feedback.text = "You have less than 20% good repetitions"
            }

            else -> {
                textView_feedback.text = ""
                textView_comments.text = ""
            }
        }

        binding.tvDescription.text =

            getString(R.string.you_have_completed_d_reps, compreps.toInt().toString())
    }
    private fun updatePoints(points: Int){
        binding.tvYourPoints.text = getString(R.string.points_summary, points)
    }
    private fun registerEvents() {
        sendResultsViewModel.sendResultsLiveData.observe(viewLifecycleOwner) { state ->
            when (state) {
                is State.Error -> {
                    hideLoading()
                    println(state.toString())
                }

                is State.Starting -> {
                    showLoading()
                }

                is State.Success -> {
                    hideLoading()
                    updatePoints(state.data)
                }
            }
        }
    }
}
