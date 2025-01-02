package com.ai.app.move.deskercise.ui.exerciseMenu

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import com.ai.app.move.deskercise.R
import com.ai.app.move.deskercise.databinding.FragmentExerciseMenuBinding
import com.ai.app.move.deskercise.ui.exerciseGroupMenu.ExerciseGroupMenuFragment
import com.ai.app.move.deskercise.ui.exerciseMenu.RecyclerViewAdapterAvailableExercise.Companion.listOfRepsMap
import com.ai.app.move.deskercise.ui.exerciseMenu.RecyclerViewAdapterAvailableExercise.Companion.listOfSelectedMoves
import com.ai.app.move.deskercise.ui.exerciseMenu.RecyclerViewAdapterAvailableExercise.Companion.listOfSelectedMovesShared
import com.ai.app.move.deskercise.ui.exerciseVision.MainActivity
import com.ai.app.move.deskercise.vision.Move
import com.ai.app.move.deskercise.vision.Moves.Stretch.DiaphragmaticBreathing.DbreathingModel
import com.ai.app.move.deskercise.vision.Moves.Stretch.ForwardStretch.ForwardStretchModel
import com.ai.app.move.deskercise.vision.Moves.Stretch.HzShoulderStretch.HzShoulderStretchModel
import com.ai.app.move.deskercise.vision.Moves.Stretch.NeckStretch.NeckStretchModel
import com.ai.app.move.deskercise.vision.Moves.Stretch.OverheadShoulderStretch.OverheadShoulderStretchModel
import com.ai.app.move.deskercise.vision.Moves.Stretch.OverheadTricepsStretch.OverheadTricepsStretchModel
import com.ai.app.move.deskercise.vision.Moves.Stretch.PiriformisStretch.PiriformisStretchModel
import com.ai.app.move.deskercise.vision.Moves.Stretch.ShoulderShrugStretch.ShoulderShrugStretchModel
import com.ai.app.move.deskercise.vision.Moves.Stretch.SideStretch.SideStretchModel
import com.ai.app.move.deskercise.vision.Moves.Stretch.TorsoStretch.TorsoStretchModel
import com.ai.app.move.deskercise.vision.Moves.Stretch.UpperTrapStretch.UpperTrapStretchModel

/**
 * The exercise menu fragment which is the default destination in navigation.
 * Users will be to select exercises and trigger the start of the exercise from this fragment
 */
class ExerciseMenuFragment : Fragment() {

    private var _binding: FragmentExerciseMenuBinding? = null
    private val exercise_group_menu_fragment_tag = "EXERCISE_GROUP_FRAGMENT"
    private val exercise_menu_fragment_tag = "EXERCISE_FRAGMENT"

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var availableMoves: ArrayList<Move>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ADD ALL EXERCISES TO BE DISPLAYED IN MENU
        availableMoves = ArrayList()
        availableMoves.add(DbreathingModel())
        availableMoves.add(SideStretchModel())
        availableMoves.add(NeckStretchModel())
        availableMoves.add(ForwardStretchModel())
        availableMoves.add(PiriformisStretchModel())
        availableMoves.add(OverheadShoulderStretchModel())
        availableMoves.add(TorsoStretchModel())
        availableMoves.add(UpperTrapStretchModel())
        availableMoves.add(ShoulderShrugStretchModel())
        availableMoves.add(OverheadTricepsStretchModel())
        availableMoves.add(HzShoulderStretchModel())

        _binding?.rvAvailableExercises?.layoutManager = LinearLayoutManager(activity)
        _binding?.rvSelectedExercises?.layoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)

        _binding?.rvAvailableExercises?.adapter = RecyclerViewAdapterAvailableExercise(
            availableMoves,
            _binding!!,
        )
        _binding?.rvSelectedExercises?.adapter =
            RecyclerViewAdapterSelectedExercise(listOfSelectedMoves)

        binding.startButton.setOnClickListener {
            val intent = Intent(activity, MainActivity::class.java)

            listOfSelectedMovesShared = listOfSelectedMoves.clone() as ArrayList<Move>
            listOfSelectedMoves = ArrayList()
            listOfRepsMap = mutableMapOf()
            // hide clear and start button
            binding.btnClearSelected.visibility = View.INVISIBLE
            binding.startButton.visibility = View.INVISIBLE
            startActivity(intent)
        }

        binding.displayExerciseGroupButton.setOnClickListener {
            val exerciseGroupMenuFragment = ExerciseGroupMenuFragment()
            var ft: FragmentTransaction =
                requireActivity().supportFragmentManager.beginTransaction()
            ft.replace(
                R.id.content_program_page,
                ExerciseMenuFragment(),
                exercise_menu_fragment_tag,
            )
            ft.commit()

            ft = requireActivity().supportFragmentManager.beginTransaction()
            ft.replace(
                R.id.content_program_page,
                exerciseGroupMenuFragment,
                exercise_group_menu_fragment_tag,
            )
            ft.addToBackStack(this.exercise_group_menu_fragment_tag)
            ft.commit()
        }

        binding.btnClearSelected.setOnClickListener {
            listOfSelectedMoves = arrayListOf()
            _binding?.rvSelectedExercises?.adapter =
                RecyclerViewAdapterSelectedExercise(listOfSelectedMoves)
            _binding?.rvAvailableExercises?.adapter = RecyclerViewAdapterAvailableExercise(
                availableMoves,
                _binding!!,
            )
            binding.btnClearSelected.visibility = View.INVISIBLE
            binding.startButton.visibility = View.INVISIBLE
            listOfRepsMap = mutableMapOf()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentExerciseMenuBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        _binding?.rvSelectedExercises?.adapter =
            RecyclerViewAdapterSelectedExercise(listOfSelectedMoves)
        _binding?.rvAvailableExercises?.adapter = RecyclerViewAdapterAvailableExercise(
            availableMoves,
            _binding!!,
        )
        if (listOfSelectedMoves.isNotEmpty()) {
            binding.btnClearSelected.visibility = View.VISIBLE
            binding.startButton.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
