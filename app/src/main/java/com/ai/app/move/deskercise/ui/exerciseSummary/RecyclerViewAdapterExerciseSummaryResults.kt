package com.ai.app.move.deskercise.ui.exerciseSummary

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
import kotlin.math.abs

/**
 * RecyclerViewAdapterExerciseSummaryResults contains the logic to handle the summary results
 * for all the individual selected exercises after all the exercises are completed
 */
class RecyclerViewAdapterExerciseSummaryResults(
    private var list: ArrayList<Move>,
) :
    RecyclerView.Adapter<RecyclerViewAdapterExerciseSummaryResults.ExerciseSummaryResultsViewHolder>() {
    companion object;

    private lateinit var parentViewGroup: ViewGroup

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ExerciseSummaryResultsViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.recycler_view_item_exercise_summary_results,
            parent,
            false,
        )
        parentViewGroup = parent
        println(list)

        return ExerciseSummaryResultsViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ExerciseSummaryResultsViewHolder, position: Int) {
        val currentItem = list[position]
        parentViewGroup.resources.assets

        holder.exercise_summary_exercise_name.text = currentItem.name

        val goodReps: Int = currentItem.getShared().repetitionResults.count { it }
        val badReps: Int = currentItem.getShared().repetitionResults.count { !it }
        val image: String = currentItem.getShared().imagesFolderPath
        val repCounter = goodReps + badReps
        val totalReps = abs(currentItem.getShared().repetitions * 2)

        holder.exercise_summary_exercise_good_rep_value.text = goodReps.toString()
        holder.exercise_summary_exercise_bad_rep_value.text = badReps.toString()
        holder.exercise_summary_current_rep_counter.text = repCounter.toString()

        if (currentItem.name == "Overhead Shoulder Stretch") {
            holder.exercise_summary_total_rep.text =
                abs(currentItem.getShared().repetitions).toString()
        } else if (currentItem.name == "Forward Stretch") {
            holder.exercise_summary_total_rep.text =
                abs(currentItem.getShared().repetitions).toString()
        } else if (currentItem.name == "Shoulder Shrug Stretch") {
            holder.exercise_summary_total_rep.text =
                abs(currentItem.getShared().repetitions).toString()
        } else if (currentItem.name == "Relaxation breath") {
            holder.bad.visibility = View.GONE
            holder.good.visibility = View.GONE
            holder.exercise_summary_total_rep.text =
                abs(currentItem.getShared().repetitions).toString()
        } else {
            holder.bad.visibility = View.VISIBLE
            holder.good.visibility = View.VISIBLE
            holder.exercise_summary_total_rep.text = totalReps.toString()
        }

        if (image.filter { !it.isWhitespace() } == "") {
            return
        }

        val assetManager: AssetManager = parentViewGroup.resources.assets

        val animationImageStream: InputStream =
            (assetManager.open("$image/${image.substring(7)}-3.png"))
        val d = Drawable.createFromStream(animationImageStream, null)
        holder.exercise_summary_exercise_image.background = d
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ExerciseSummaryResultsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val exercise_summary_exercise_name: TextView =
            itemView.findViewById(R.id.exercise_summary_exercise_name)
        val exercise_summary_exercise_good_rep_value: TextView =
            itemView.findViewById(R.id.exercise_summary_exercise_good_rep_value)
        val exercise_summary_exercise_bad_rep_value: TextView =
            itemView.findViewById(R.id.exercise_summary_exercise_bad_rep_value)
        val good: LinearLayout = itemView.findViewById(R.id.greentored)
        val bad: LinearLayout = itemView.findViewById(R.id.greentorednext)
        val exercise_summary_exercise_image: ImageView =
            itemView.findViewById(R.id.exercise_summary_background)
        val exercise_summary_current_rep_counter: TextView =
            itemView.findViewById(R.id.currentRepCounter)
        val exercise_summary_total_rep: TextView = itemView.findViewById(R.id.totalRepCounter)
    }
}
