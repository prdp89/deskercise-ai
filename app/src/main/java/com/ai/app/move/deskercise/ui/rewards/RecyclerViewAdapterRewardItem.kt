package com.ai.app.move.deskercise.ui.rewards

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ai.app.move.deskercise.R
import com.ai.app.move.deskercise.data.Reward

class RecyclerViewAdapterRewardItem(private var list: ArrayList<Reward>) :
    RecyclerView.Adapter<RecyclerViewAdapterRewardItem.AvailableRewardHolder>() {

    private lateinit var parentViewGroup: ViewGroup
    private lateinit var dropdownItems: Array<String>

    class AvailableRewardHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val iv_square_gif: ImageView = itemView.findViewById(R.id.reward_item_image)
        val exerciseTitle: TextView = itemView.findViewById(R.id.reward_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AvailableRewardHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.item_reward,
            parent,
            false,
        )
        parentViewGroup = parent
        dropdownItems = parentViewGroup.resources.getStringArray(R.array.Gender)

        return AvailableRewardHolder(itemView)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: AvailableRewardHolder, position: Int) {
        val currentItem = list[position]
        parentViewGroup.resources.assets
        holder.exerciseTitle.text = currentItem.name
        val image: String = currentItem.image
        if (image.filter { !it.isWhitespace() } == "") {
            return
        }
    }
}
