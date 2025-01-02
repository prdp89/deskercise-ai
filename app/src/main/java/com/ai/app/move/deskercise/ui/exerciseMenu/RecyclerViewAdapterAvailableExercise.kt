package com.ai.app.move.deskercise.ui.exerciseMenu

import android.content.Context
import android.content.res.AssetManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.ai.app.move.deskercise.R
import com.ai.app.move.deskercise.databinding.FragmentExerciseMenuBinding
import com.ai.app.move.deskercise.services.Commons
import com.ai.app.move.deskercise.vision.Move

/**
 * The RecyclerViewAdapterAvailableExercise is used to manage the logic of displaying the available exercises
 * and allowing users to select number of reps per exercise and add to the list
 */
class RecyclerViewAdapterAvailableExercise(
    private var list: ArrayList<Move>,
    private val exerciseMenuFragmentBinding: FragmentExerciseMenuBinding,
) :
    RecyclerView.Adapter<RecyclerViewAdapterAvailableExercise.AvailableExerciseViewHolder>() {

    lateinit var parentViewGroup: ViewGroup
    private lateinit var dropdownItems: Array<String>
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AvailableExerciseViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.recycler_view_item_available_exercise,
            parent,
            false,
        )
        parentViewGroup = parent
        dropdownItems = parentViewGroup.resources.getStringArray(R.array.Gender)

        // Populate the spinner drop down for number of set/reps
        val spinner: Spinner = itemView.findViewById(R.id.spinner_number_of_sets_per_item)

        val dropdownItemsArrayList = dropdownItems.toCollection(ArrayList())
        dropdownItemsArrayList.add("")

        val arrayAdapter: ArrayAdapter<*> = NoPaddingArrayAdapter(
            itemView.context,
            android.R.layout.simple_list_item_1,
            dropdownItemsArrayList,
        )
        spinner.adapter = arrayAdapter
        spinner.setSelection(arrayAdapter.count)

        return AvailableExerciseViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: AvailableExerciseViewHolder, position: Int) {
        val currentItem = list[position]
        val assetManager: AssetManager = parentViewGroup.resources.assets

        holder.spinnerNumberOfSets.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position_item: Int,
                    id: Long,
                ) {
                    if ((holder.spinnerNumberOfSets.selectedItem.toString()).isNotEmpty() && !skipSpinnerTrigger) {
                        listOfRepsMap[holder.adapterPosition] =
                            holder.spinnerNumberOfSets.selectedItem.toString()
                        // if button text is added don't enable
                        if (holder.addButton.text != parentViewGroup.resources.getString(R.string.added)) {
                            enableAddButton(holder.addButton)
                        }
                    }
                    skipSpinnerTrigger = false
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

        currentItem.imagesFolderPath.let {
//            Commons.displayGifOnImageView(
//                it,
//                holder.iv_square_gif,
//                assetManager,
//                Commons.Direction.LEFT,
//            )
        }
        holder.exerciseTitle.text = currentItem.name

        if ((holder.spinnerNumberOfSets.selectedItem.toString()).isEmpty()) {
            disableAddButton(
                holder.addButton,
                null,
                parentViewGroup.resources.getString(R.string.add),
            )
        }

        holder.addButton.setOnClickListener {
            if (listOfSelectedMoves.any { it::class.java == currentItem::class.java }) {
                // if already added && not disabled
                Toast.makeText(
                    parentViewGroup.context,
                    "${currentItem.name} already selected",
                    Toast.LENGTH_SHORT,
                ).show()
            } else {
                // get current rep count
                currentItem.getShared().remainingRepetitions =
                    Integer.parseInt(holder.spinnerNumberOfSets.selectedItem.toString())

                // add new item
                listOfSelectedMoves.add(currentItem)

                disableAddButton(holder.addButton, holder.clRow)
                disableSpinner(holder.spinnerNumberOfSets)
            }

            // Updated the selected exercise RecycleViewer
            exerciseMenuFragmentBinding.rvSelectedExercises.adapter =
                RecyclerViewAdapterSelectedExercise(listOfSelectedMoves)
            if (listOfSelectedMoves.isNotEmpty()) {
                RecyclerViewAdapterSelectedExercise(listOfSelectedMoves)
                exerciseMenuFragmentBinding.btnClearSelected.visibility = View.VISIBLE
                if (exerciseMenuFragmentBinding.startButton.visibility == View.INVISIBLE) {
                    Commons.crossFade(exerciseMenuFragmentBinding.startButton) // SET VISIBLE WITH ANIMATION
                }
            }
        }

        if (listOfSelectedMoves.any { it::class.java == currentItem::class.java }) {
            disableSpinner(holder.spinnerNumberOfSets)
            disableAddButton(holder.addButton, holder.clRow)
        }

        if (listOfRepsMap.containsKey(position)) {
            // get index of the dropdown string value that matches with the current string value
            holder.spinnerNumberOfSets.setSelection(dropdownItems.indexOf(listOfRepsMap[position]))
        }
    }

    private fun disableAddButton(btn: Button, row: ConstraintLayout? = null, str: String? = null) {
        // disable button
        btn.isEnabled = false
        btn.setBackgroundColor(parentViewGroup.resources.getColor(R.color.lightGray))
        btn.setTextColor(parentViewGroup.resources.getColor(R.color.gray))
        if (str == null) {
            btn.text = parentViewGroup.resources.getString(R.string.added)
        } else {
            btn.text = str
        }
        row?.setBackgroundColor(parentViewGroup.resources.getColor(R.color.lightGray))
    }

    private fun enableAddButton(btn: Button, row: ConstraintLayout? = null, str: String? = null) {
        // disable button
        btn.isEnabled = true
        btn.setBackgroundColor(parentViewGroup.resources.getColor(R.color.deskGreen))
        btn.setTextColor(parentViewGroup.resources.getColor(R.color.colorPrimaryInverse))
        if (str == null) {
            btn.text = parentViewGroup.resources.getString(R.string.add)
        } else {
            btn.text = str
        }
        row?.setBackgroundColor(Color.Transparent.toArgb())
    }

    private fun disableSpinner(spnr: Spinner) {
        // disable dropdown
        spnr.isEnabled = false
    }

    override fun getItemCount(): Int {
        return list.size
    }

    open class NoPaddingArrayAdapter<T>(context: Context, layoutId: Int, items: ArrayList<T>?) :
        ArrayAdapter<T>(
            context,
            layoutId,
            items as ArrayList<T>,
        ) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = super.getView(position, convertView, parent)

            // for displaying blank value as default
            if (position == count) {
                (view.findViewById(android.R.id.text1) as TextView).text = " "
            }
            return view
        }

        override fun getCount(): Int {
            return super.getCount() - 1 // you don't display last item. It is used as hint.
        }
    }

    class AvailableExerciseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val iv_square_gif: ImageView = itemView.findViewById(R.id.iv_square_gif)
        val exerciseTitle: TextView = itemView.findViewById(R.id.tv_exerciseTitle)
        val spinnerNumberOfSets: Spinner =
            itemView.findViewById(R.id.spinner_number_of_sets_per_item)
        val addButton: Button = itemView.findViewById(R.id.addButton)
        val clRow: ConstraintLayout = itemView.findViewById(R.id.cl_row)
    }

    companion object {
        var listOfSelectedMoves: ArrayList<Move> = ArrayList()
        var listOfSelectedMovesShared: ArrayList<Move> = ArrayList()
        var skipSpinnerTrigger = false
        var listOfRepsMap: MutableMap<Int, String> = mutableMapOf()
    }
}
