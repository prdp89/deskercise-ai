package com.ai.app.move.deskercise.base

import android.os.Bundle
import androidx.viewbinding.ViewBinding

abstract class BaseMvvmActivity<T : ViewBinding, VM : BaseViewModel> : BaseBindingActivity<T>() {

    private lateinit var vm: VM
    abstract fun bindViewModel(): VM

    protected fun getViewModel(): VM = vm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = getViewBinding()
        setContentView(binding.root)
        vm = bindViewModel()
        registerEvents(vm)
    }

    open fun registerEvents(viewModel: VM) {
        viewModel.messageLiveData.observe(this) { event ->
            when (event) {
                is BaseViewModel.MessageEvent.ResourceMessage -> showToast(event.message)
                is BaseViewModel.MessageEvent.StringMessage -> showToast(event.message)
            }
        }
    }
}
