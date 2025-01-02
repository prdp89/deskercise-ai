package com.ai.app.move.deskercise.ui.exerciseGroupMenu

import android.content.res.AssetManager
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ai.app.move.deskercise.R
import com.ai.app.move.deskercise.vision.Move
import java.io.InputStream

/**
 * The RecyclerViewAdapterAvailableExerciseGroupPreview is used to manage the logic of displaying the available exercises
 * and allowing users to select number of reps per exercise and add to the list
 */
class RecyclerViewAdapterAvailableExerciseGroupPreview(
    private var list: ArrayList<Move>,
) :
    RecyclerView.Adapter<RecyclerViewAdapterAvailableExerciseGroupPreview.AvailableExerciseViewHolder>() {
    companion object;

    private lateinit var parentViewGroup: ViewGroup
    private lateinit var dropdownItems: Array<String>

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AvailableExerciseViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.recycler_view_item_available_exercise_group_preview,
            parent,
            false,
        )
        parentViewGroup = parent
        dropdownItems = parentViewGroup.resources.getStringArray(R.array.Gender)

        // Populate the spinner drop down for number of set/reps

        val dropdownItemsArrayList = dropdownItems.toCollection(ArrayList())
        dropdownItemsArrayList.add("")

        return AvailableExerciseViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: AvailableExerciseViewHolder, position: Int) {
        val currentItem = list[position]
        parentViewGroup.resources.assets

        holder.exerciseTitle.text = currentItem.name
        if (currentItem.name == "Relaxation breath") {
            holder.setButton.visibility = View.GONE
        } else {
            holder.tv_exerciseRep_val.text = list[position].getShared().repetitions.toString() + " sets" }

        val image: String = currentItem.getShared().imagesFolderPath

        if (image.filter { !it.isWhitespace() } == "") {
            return
        }

        val assetManager: AssetManager = parentViewGroup.resources.assets
        val animationImagesFolderPath: Array<out String>? = assetManager.list(image)

        for (imageFilePath: String in animationImagesFolderPath!!) {
            val animationImageStream: InputStream = (assetManager.open("$image/$imageFilePath"))

            val d = Drawable.createFromStream(animationImageStream, null)
            holder.iv_square_gif.background = d
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class AvailableExerciseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val iv_square_gif: ImageView = itemView.findViewById(R.id.iv_square_gif)
        val exerciseTitle: TextView = itemView.findViewById(R.id.tv_exerciseTitle)
        val tv_exerciseRep_val: TextView = itemView.findViewById(R.id.tv_exerciseRep_val)
        val setButton: LinearLayout = itemView.findViewById(R.id.setButton)
    }
}
