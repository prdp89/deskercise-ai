package com.ai.app.move.deskercise.base

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.viewbinding.ViewBinding

abstract class BaseBindingAdapter<T : ViewBinding> : Adapter<BaseBindingAdapter<T>.VH>() {
    inner class VH(val binding: T) : ViewHolder(binding.root)

    abstract fun getItemBinding(parent: ViewGroup, viewType: Int): T
    abstract fun bindItem(binding: T, position: Int)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(getItemBinding(parent, viewType))
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        bindItem(holder.binding, position)
    }
}
