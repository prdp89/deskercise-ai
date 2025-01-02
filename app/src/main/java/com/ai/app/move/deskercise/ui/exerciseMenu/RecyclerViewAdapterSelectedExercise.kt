package com.ai.app.move.deskercise.ui.exerciseMenu

import android.annotation.SuppressLint
import android.content.res.AssetManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.badge.BadgeUtils
import com.ai.app.move.deskercise.R
import com.ai.app.move.deskercise.vision.Move

/**
 * The RecyclerViewAdapterSelectedExercise is used to manage the logic of displaying the selected exercises
 */
class RecyclerViewAdapterSelectedExercise(private var list: ArrayList<Move>) :
    RecyclerView.Adapter<RecyclerViewAdapterSelectedExercise.SelectedExerciseViewHolder>() {
    companion object;

    private lateinit var parentViewGroup: ViewGroup

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectedExerciseViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.recycler_view_item_selected_exercise,
            parent,
            false,
        )

        parentViewGroup = parent

        return SelectedExerciseViewHolder(itemView)
    }

    @SuppressLint("UnsafeOptInUsageError")
    override fun onBindViewHolder(holder: SelectedExerciseViewHolder, position: Int) {
        val currentItem = list[position]
        val assetManager: AssetManager = parentViewGroup.resources.assets

        currentItem.imagesFolderPath.let {
//            Commons.displayGifOnImageView(
//                it,
//                holder.iv_circle_gif,
//                assetManager
//            )
        }

        val badge = BadgeDrawable.create(parentViewGroup.context)
        BadgeUtils.attachBadgeDrawable(badge, holder.iv_circle_gif, holder.fl_badge)
        badge.number = position + 1
        badge.verticalOffset = 30
        badge.horizontalOffset = -30
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class SelectedExerciseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val iv_circle_gif: ImageView = itemView.findViewById(R.id.iv_circle_gif)
        val fl_badge: FrameLayout = itemView.findViewById(R.id.ll_box_1)
    }
}
