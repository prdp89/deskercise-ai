package com.ai.app.move.deskercise.ui.profile.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.ai.app.move.deskercise.databinding.ItemSpinnerBinding
import com.ai.app.move.deskercise.utils.getLayoutInflater
import com.ai.app.move.deskercise.utils.toPx

class StringSpinnerAdapter(context: Context) :
    ArrayAdapter<String>(context, 0) {
    inner class ViewHolder(val binding: ItemSpinnerBinding) {
        fun bind(item: String) {
            binding.title.text = item
        }
    }

    private val items = arrayListOf<String>()

    fun setData(values: List<String>) {
        items.clear()
        items.addAll(values)
        notifyDataSetChanged()
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val viewHolder: ViewHolder = convertView?.tag as? ViewHolder ?: kotlin.run {
            val view = ItemSpinnerBinding.inflate(parent.getLayoutInflater(), parent, false)
            ViewHolder(view)
        }
        viewHolder.binding.root.apply {
            val padding = 8f.toPx(context)
            setPadding(padding, 0, padding, 0)
        }
        val item = getItem(position)
        viewHolder.bind(item)
        return viewHolder.binding.root
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val viewHolder: ViewHolder = convertView?.tag as? ViewHolder ?: kotlin.run {
            val view = ItemSpinnerBinding.inflate(parent.getLayoutInflater(), parent, false)
            ViewHolder(view)
        }
        val item = getItem(position)
        viewHolder.bind(item)
        return viewHolder.binding.root
    }

    override fun getItem(position: Int): String {
        return items[position]
    }

    override fun getCount(): Int {
        return items.size
    }
}
