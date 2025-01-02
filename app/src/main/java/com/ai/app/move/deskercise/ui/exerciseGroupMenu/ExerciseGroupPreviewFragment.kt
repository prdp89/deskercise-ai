package com.ai.app.move.deskercise.ui.exerciseGroupMenu

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.ai.app.move.deskercise.R
import com.ai.app.move.deskercise.base.BaseFragment
import com.ai.app.move.deskercise.databinding.FragmentExerciseGroupPreviewBinding
import com.ai.app.move.deskercise.ui.exerciseGroupMenu.programModels.ExerciseGroupProgramModel
import com.ai.app.move.deskercise.vision.Move
import org.koin.androidx.viewmodel.ext.android.activityViewModel

/**
 * The exercise menu fragment which is the default destination in navigation.
 * Users will be to select exercises and trigger the start of the exercise from this fragment
 */

class ExerciseGroupPreviewFragment : BaseFragment() {
    private var _binding: FragmentExerciseGroupPreviewBinding? = null

    companion object {
        const val KEY_PROGRAM_MODEL = "key-program-model"
        fun newInstance(_programModel: ExerciseGroupProgramModel): ExerciseGroupPreviewFragment {
            val args = Bundle()
            args.putParcelable(KEY_PROGRAM_MODEL, _programModel)
            val fragment = ExerciseGroupPreviewFragment()
            fragment.arguments = args
            return fragment
        }
    }

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var availableMoves: ArrayList<Move>
    private val programModel: ExerciseGroupProgramModel? by lazy {
        arguments?.getParcelable(KEY_PROGRAM_MODEL) as? ExerciseGroupProgramModel
    }
    private val startProgramViewModel by activityViewModel<StartProgramViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ADD ALL EXERCISES TO BE DISPLAYED IN MENU
        programModel?.let {
            startProgramViewModel.updateProgramModel(it)
            availableMoves = it.availableMoves
            _binding?.programName?.text = it.programName + " Min \nProgram"

            _binding?.programLength?.text =
                (it.availableMoves.size - 1).toString() + " Exercises"
        }

        _binding?.rvAvailableExercises?.layoutManager = LinearLayoutManager(activity)

        _binding?.rvAvailableExercises?.adapter = RecyclerViewAdapterAvailableExerciseGroupPreview(
            availableMoves,
        )

        binding.backButton.setOnClickListener { handleBackPressed() }
        startProgramViewModel.updateProgramID(programModel?.availableMoves!!.size)

        binding.startExerciseGroupButton.setOnClickListener {
            startProgramViewModel.startProgram(programModel?.isRandomized ?: false)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentExerciseGroupPreviewBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        _binding?.rvAvailableExercises?.adapter = RecyclerViewAdapterAvailableExerciseGroupPreview(
            availableMoves,
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun handleBackPressed() {
        if (programModel?.isRandomized == true) {
            val alert = AlertDialog.Builder(this.context)
            alert.setTitle(getString(R.string.exit_program))
            alert.setMessage(getString(R.string.you_will_lose_this_randomization_if_you_exit_this_page_are_you_sure))
            alert.setNegativeButton(getString(R.string.yes)) { dialogInterface, which ->
                dialogInterface.cancel()
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
            alert.setPositiveButton(getString(R.string.no)) { dialogInterface, which ->
                dialogInterface.cancel()
            }
            alert.show()
        } else {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }
}
