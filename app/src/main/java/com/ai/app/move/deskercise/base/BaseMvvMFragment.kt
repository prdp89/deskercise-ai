package com.ai.app.move.deskercise.base

import android.os.Bundle
import android.view.View
import androidx.viewbinding.ViewBinding

abstract class BaseMvvMFragment<T : ViewBinding, VM : BaseViewModel> :
    BaseBindingFragment<T>() {

    protected lateinit var viewModel: VM
    abstract fun bindViewModel(): VM

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = bindViewModel()
        registerEvents(viewModel)
    }

    open fun registerEvents(viewModel: VM) {
        viewModel.messageLiveData.observe(viewLifecycleOwner) { event ->
            when (event) {
                is BaseViewModel.MessageEvent.ResourceMessage -> showToast(event.message)
                is BaseViewModel.MessageEvent.StringMessage -> showToast(event.message)
            }
        }
    }
}
