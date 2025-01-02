package com.ai.app.move.deskercise.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.viewbinding.ViewBinding
import com.ai.app.move.deskercise.utils.LoadingDialog
import timber.log.Timber

abstract class BaseBindingDialogFragment<T : ViewBinding> : DialogFragment() {
    protected lateinit var binding: T

    abstract fun getViewBinding(): T

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = getViewBinding()
        return binding.root
    }
    private val loadingDialog by lazy { LoadingDialog(requireContext()) }
    private val toaster by lazy { Toast.makeText(requireContext(), "", Toast.LENGTH_SHORT) }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setOnClickListener {
            // just do nothing
        }
    }

    override fun onResume() {
        super.onResume()
        Timber.d(">> onResume ${this.javaClass.simpleName}")
    }

    override fun onPause() {
        super.onPause()
        Timber.d(">> onPause ${this.javaClass.simpleName}")
    }

    fun showToast(message: String) {
        toaster.cancel()
        toaster.setText(message)
        toaster.show()
    }

    fun showToast(message: Int) {
        toaster.cancel()
        toaster.setText(message)
        toaster.show()
    }
    fun showLoading(cancellable: Boolean = false) {
        if (loadingDialog.isShowing) {
            loadingDialog.dismiss()
        }
        loadingDialog.setCancelable(cancellable)
        loadingDialog.show()
    }

    fun hideLoading() {
        if (loadingDialog.isShowing) {
            loadingDialog.dismiss()
        }
    }
}
