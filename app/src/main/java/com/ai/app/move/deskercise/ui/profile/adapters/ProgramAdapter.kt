package com.ai.app.move.deskercise.ui.profile.adapters

import android.view.View
import android.view.ViewGroup
import com.ai.app.move.deskercise.base.BaseBindingAdapter
import com.ai.app.move.deskercise.databinding.ItemCardViewProgramBinding
import com.ai.app.move.deskercise.ui.exerciseGroupMenu.programModels.ExerciseGroup10MinProgramModel
import com.ai.app.move.deskercise.ui.exerciseGroupMenu.programModels.ExerciseGroup15MinProgramModel
import com.ai.app.move.deskercise.ui.exerciseGroupMenu.programModels.ExerciseGroup5MinProgramModel
import com.ai.app.move.deskercise.ui.exerciseGroupMenu.programModels.ExerciseGroupProgramModel
import com.ai.app.move.deskercise.utils.getLayoutInflater

class ProgramAdapter(
    private val ids: ArrayList<Int>,
    private val details: (ExerciseGroupProgramModel) -> Unit,
    private val start: (Int, Boolean, ExerciseGroupProgramModel) -> Unit,
) : BaseBindingAdapter<ItemCardViewProgramBinding>() {
    override fun getItemBinding(parent: ViewGroup, viewType: Int): ItemCardViewProgramBinding {
        return ItemCardViewProgramBinding.inflate(parent.getLayoutInflater(), parent, false)
    }

    override fun bindItem(binding: ItemCardViewProgramBinding, position: Int) {
        val id = ids[position]

        val modelRef = getProgramModelById(id)
        val duration = modelRef.programName
        val name = "$duration min"
        binding.tvProgramName.text = name
        binding.tvDuration.text = name
        binding.tvExerciseCount.text = "${modelRef.availableMoves.size - 1} exercises"

        // set the picture
        binding.rlCard.setBackgroundResource(getProgramModelById(id).backgroundImage)

        binding.swRandomize.setOnCheckedChangeListener { _, enable ->
            binding.tvRandom.visibility = if (!enable) View.GONE else View.VISIBLE
        }

        binding.ibProgramDetails.setOnClickListener {
            val isRandom = binding.swRandomize.isChecked
            val programModel = getProgramModelById(id, isRandom)
            details(programModel)
        }

        binding.btnStart.setOnClickListener {
            val isRandom = binding.swRandomize.isChecked
            start(
                id,
                isRandom,
                getProgramModelById(id, isRandom),
            )
        }
    }

    override fun getItemCount(): Int {
        return ids.size
    }

    private fun getProgramModelById(id: Int, isRandom: Boolean = false): ExerciseGroupProgramModel {
        return when (id) {
            4 -> ExerciseGroup5MinProgramModel(isRandom)
            7 -> ExerciseGroup10MinProgramModel(isRandom)
            else -> ExerciseGroup15MinProgramModel(isRandom)
        }
    }
}
